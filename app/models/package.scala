/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import generated._
import play.api.libs.json._
import scalaxb.`package`.toScope
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

import java.time.LocalDate
import javax.xml.datatype.XMLGregorianCalendar
import scala.annotation.nowarn
import scala.xml.NodeSeq

package object models {

  implicit class RichSensitiveString(sensitiveString: SensitiveString) {
    def decrypt: JsObject = Json.parse(sensitiveString.decryptedValue).as[JsObject]
  }

  implicit class RichJsObject(jsObject: JsObject) {

    def setObject(path: JsPath, value: JsValue): JsResult[JsObject] =
      jsObject.set(path, value).flatMap(_.validate[JsObject])

    def removeObject(path: JsPath): JsResult[JsObject] =
      jsObject.remove(path).flatMap(_.validate[JsObject])

    def encrypt: SensitiveString = SensitiveString(Json.stringify(jsObject))
  }

  implicit class RichJsValue(jsValue: JsValue) {

    def set(path: JsPath, value: JsValue): JsResult[JsValue] =
      (path.path, jsValue) match {

        case (Nil, _) =>
          JsError("path cannot be empty")

        case ((_: RecursiveSearch) :: _, _) =>
          JsError("recursive search not supported")

        case ((n: IdxPathNode) :: Nil, _) =>
          setIndexNode(n, jsValue, value)

        case ((n: KeyPathNode) :: Nil, _) =>
          setKeyNode(n, jsValue, value)

        case (first :: second :: rest, oldValue) =>
          Reads
            .optionNoError(Reads.at[JsValue](JsPath(first :: Nil)))
            .reads(oldValue)
            .flatMap {
              opt =>
                opt
                  .map(JsSuccess(_))
                  .getOrElse {
                    second match {
                      case _: KeyPathNode =>
                        JsSuccess(Json.obj())
                      case _: IdxPathNode =>
                        JsSuccess(Json.arr())
                      case _: RecursiveSearch =>
                        JsError("recursive search is not supported")
                    }
                  }
                  .flatMap {
                    _.set(JsPath(second :: rest), value).flatMap {
                      newValue =>
                        oldValue.set(JsPath(first :: Nil), newValue)
                    }
                  }
            }
      }

    private def setIndexNode(node: IdxPathNode, oldValue: JsValue, newValue: JsValue): JsResult[JsValue] = {

      val index: Int = node.idx

      oldValue match {
        case oldValue: JsArray if index >= 0 && index <= oldValue.value.length =>
          if (index == oldValue.value.length) {
            JsSuccess(oldValue.append(newValue))
          } else {
            JsSuccess(JsArray(oldValue.value.updated(index, newValue)))
          }
        case oldValue: JsArray =>
          JsError(s"array index out of bounds: $index, $oldValue")
        case _ =>
          JsError(s"cannot set an index on $oldValue")
      }
    }

    private def removeIndexNode(node: IdxPathNode, valueToRemoveFrom: JsArray): JsResult[JsValue] = {
      val index: Int = node.idx

      valueToRemoveFrom match {
        case valueToRemoveFrom: JsArray if index >= 0 && index < valueToRemoveFrom.value.length =>
          val updatedJsArray = valueToRemoveFrom.value.slice(0, index) ++ valueToRemoveFrom.value.slice(index + 1, valueToRemoveFrom.value.size)
          JsSuccess(JsArray(updatedJsArray))
        case valueToRemoveFrom: JsArray => JsError(s"array index out of bounds: $index, $valueToRemoveFrom")
        case _                          => JsError(s"cannot set an index on $valueToRemoveFrom")
      }
    }

    private def setKeyNode(node: KeyPathNode, oldValue: JsValue, newValue: JsValue): JsResult[JsValue] = {

      val key = node.key

      oldValue match {
        case oldValue: JsObject =>
          JsSuccess(oldValue + (key -> newValue))
        case _ =>
          JsError(s"cannot set a key on $oldValue")
      }
    }

    @nowarn("msg=Exhaustivity analysis reached max recursion depth, not all missing cases are reported.")
    @nowarn("msg=match may not be exhaustive")
    // scalastyle:off cyclomatic.complexity
    def remove(path: JsPath): JsResult[JsValue] =
      (path.path, jsValue) match {
        case (Nil, _)                                                                  => JsError("path cannot be empty")
        case ((n: KeyPathNode) :: Nil, value: JsObject) if value.keys.contains(n.key)  => JsSuccess(value - n.key)
        case ((n: KeyPathNode) :: Nil, value: JsObject) if !value.keys.contains(n.key) => JsSuccess(value)
        case ((n: IdxPathNode) :: Nil, value: JsArray)                                 => removeIndexNode(n, value)
        case ((_: KeyPathNode) :: Nil, _)                                              => JsError(s"cannot remove a key on $jsValue")
        case (first :: second :: rest, oldValue) =>
          Reads
            .optionNoError(Reads.at[JsValue](JsPath(first :: Nil)))
            .reads(oldValue)
            .flatMap {
              opt: Option[JsValue] =>
                opt
                  .map(JsSuccess(_))
                  .getOrElse {
                    second match {
                      case _: KeyPathNode =>
                        JsSuccess(Json.obj())
                      case _: IdxPathNode =>
                        JsSuccess(Json.arr())
                      case _: RecursiveSearch =>
                        JsError("recursive search is not supported")
                    }
                  }
                  .flatMap {
                    _.remove(JsPath(second :: rest)).flatMap {
                      newValue =>
                        oldValue.set(JsPath(first :: Nil), newValue)
                    }
                  }
            }
      }
    // scalastyle:on cyclomatic.complexity
  }

  implicit class RichString(string: String) {

    def removeSpaces(): String =
      string.foldLeft("") {
        (acc, c) =>
          acc + c.toString.trim
      }
  }

  implicit class RichCC043CType(value: CC043CType) {

    def toXML: NodeSeq = scalaxb.toXML(value, CC043C.toString, toScope())

    def preparationDateAndTime: LocalDate =
      value.messageSequence1.messagE_1Sequence2.preparationDateAndTime.toLocalDate

    def sealsExist: Boolean =
      value.Consignment.exists(_.TransportEquipment.exists(_.Seal.nonEmpty))

    def hasXIOfficeOfDestination: Boolean = value.CustomsOfficeOfDestinationActual.referenceNumber.startsWith("XI")
  }

  implicit class RichXMLGregorianCalendar(value: XMLGregorianCalendar) {

    def toLocalDate: LocalDate =
      value.toGregorianCalendar.toZonedDateTime.toLocalDate
  }

  implicit class RichJsArray(arr: JsArray) {

    def mapWithIndex[T](f: (JsValue, Index) => T): Seq[T] =
      arr.value.zipWithIndex
        .flatMap {
          case (value, i) =>
            value match {
              case JsObject(underlying) =>
                underlying.keys.toSeq match {
                  case "sequenceNumber" :: Nil => None
                  case _                       => Some((value, i))
                }
              case _ => Some((value, i))
            }
        }
        .zipWithIndex
        .map {
          case ((value, index), _) => f(value, Index(index))
        }
        .toSeq

    def zipWithIndex: List[(JsValue, Index)] = arr.value.toList.zipWithIndex.map(
      x => (x._1, Index(x._2))
    )

    def isEmpty: Boolean = arr.value.isEmpty
  }

  implicit class RichOptionalJsArray(arr: Option[JsArray]) {

    def mapWithIndex[T](f: (JsValue, Index) => T): Seq[T] =
      arr.map(_.mapWithIndex(f)).getOrElse(Nil)

    def flatMapWithIndex[T](f: (JsValue, Index) => Option[T]): Seq[T] =
      mapWithIndex(f).flatten

    def validate[T](implicit rds: Reads[T]): Option[T] =
      arr.flatMap(_.validate[T].asOpt)

    def length: Int = arr.getOrElse(JsArray()).value.length

    def nextIndex: Index = Index(length)
  }

  implicit class RichPreviousDocument(previousDocument: PreviousDocumentType04) {

    def toPreviousDocumentType06: PreviousDocumentType06 = {
      import previousDocument._
      PreviousDocumentType06(sequenceNumber, typeValue, referenceNumber, complementOfInformation)
    }
  }

  implicit class RichPreviousDocuments(previousDocuments: Seq[PreviousDocumentType04]) {

    def toPreviousDocumentType06: Seq[PreviousDocumentType06] =
      previousDocuments.map(_.toPreviousDocumentType06)
  }
}
