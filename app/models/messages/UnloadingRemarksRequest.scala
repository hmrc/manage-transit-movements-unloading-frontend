/*
 * Copyright 2022 HM Revenue & Customs
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

package models.messages

import cats.syntax.all._
import com.lucidchart.open.xtract.XmlReader.strictReadSeq
import com.lucidchart.open.xtract.{__, XmlReader}
import models.XMLWrites._
import models.{Seals, TraderAtDestination, XMLWrites}

import scala.xml.{Elem, Node, NodeSeq}

case class UnloadingRemarksRequest(
  meta: Meta,
  header: Header,
  traderAtDestination: TraderAtDestination,
  presentationOffice: String,
  unloadingRemark: Remarks,
  resultOfControl: Seq[ResultsOfControl],
  seals: Option[Seals]
)

object UnloadingRemarksRequest {

  val transportIdentityLength    = 27
  val transportIdentityCountry   = 2
  val numberOfItems              = 99999
  val numberOfPackages           = 9999999
  val presentationOfficeLength   = 8
  val newSealNumberMaximumLength = 20
  val vehicleNameMaxLength       = 27
  val alphaNumericRegex          = "^[a-zA-Z0-9 ]*$"
  val grossMassRegex             = "^(\\d{1,11}|(\\d{0,11}\\.{1}\\d{1,3}){1})$"
  val grossMassLength            = 15
  val stringFieldRegex           = "[\\sa-zA-Z0-9&'@/.\\-? ]*"

  implicit def writes: XMLWrites[UnloadingRemarksRequest] = XMLWrites[UnloadingRemarksRequest] {
    unloadingRemarksRequest =>
      val UnloadingRemarksRequest(meta, header, traderAtDestination, presentationOffice, unloadingRemark, resultOfControl, seals) = unloadingRemarksRequest

      val parentNode: Node = <CC044A></CC044A>

      val childNodes: NodeSeq = {
        meta.toXml ++
          header.toXml ++
          traderAtDestination.toXml ++
          <CUSOFFPREOFFRES><RefNumRES1>{presentationOffice}</RefNumRES1></CUSOFFPREOFFRES> ++
          unloadingRemarkNode(unloadingRemark) ++
          resultOfControlNode(resultOfControl) ++
          seals.map(_.toXml).getOrElse(NodeSeq.Empty)
      }

      Elem(parentNode.prefix, parentNode.label, parentNode.attributes, parentNode.scope, parentNode.child.isEmpty, parentNode.child ++ childNodes: _*)
  }

  implicit val reads: XmlReader[UnloadingRemarksRequest] =
    (__.read[Meta],
     (__ \ "HEAHEA").read[Header],
     (__ \ "TRADESTRD").read[TraderAtDestination],
     (__ \ "CUSOFFPREOFFRES" \ "RefNumRES1").read[String],
     (__ \ "UNLREMREM").read[Remarks],
     (__ \ "RESOFCON534").read(strictReadSeq[ResultsOfControl]),
     (__ \ "SEAINFSLI").read[Seals].optional
    ) mapN apply

  private def resultOfControlNode(resultsOfControl: Seq[ResultsOfControl]): NodeSeq =
    resultsOfControl.flatMap {
      case y: ResultsOfControlOther           => y.toXml
      case y: ResultsOfControlDifferentValues => y.toXml
    }

  private def unloadingRemarkNode(unloadingRemark: Remarks): NodeSeq = unloadingRemark match {
    case remarksConform: RemarksConform                   => remarksConform.toXml
    case remarksConformWithSeals: RemarksConformWithSeals => remarksConformWithSeals.toXml
    case remarksNonConform: RemarksNonConform             => remarksNonConform.toXml
  }
}
