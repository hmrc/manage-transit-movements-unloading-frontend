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

package models

import generated.CC043CType
import models.SensitiveFormats.SensitiveWrites
import pages._
import play.api.libs.json._
import queries.Gettable
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import utils.transformers.{Removed, SequenceNumber}

import java.time.Instant
import scala.util.{Failure, Success, Try}

final case class UserAnswers(
  id: ArrivalId,
  mrn: MovementReferenceNumber,
  eoriNumber: EoriNumber,
  ie043Data: CC043CType,
  data: JsObject,
  lastUpdated: Instant
) {

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(path)).reads(data).getOrElse(None)

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    get(page.path)

  def get[A](page: QuestionPage[A])(implicit rds: Reads[A]): Option[A] =
    get(page: Gettable[A])

  def set[A](page: QuestionPage[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] =
    set(page.path, value).flatMap {
      userAnswers => page.cleanup(Some(value), userAnswers)
    }

  def set[A](path: JsPath, value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {
    val updatedData = data.setObject(path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    updatedData.map {
      d =>
        copy(data = d)
    }
  }

  def remove[A](page: QuestionPage[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(None, updatedAnswers)
    }
  }

  def removeDataGroup[A](section: QuestionPage[A]): Try[UserAnswers] =
    removeExceptPaths(section, __ \ SequenceNumber)

  def removeItem[A](section: QuestionPage[A]): Try[UserAnswers] =
    removeExceptPaths(section, __ \ SequenceNumber, __ \ "declarationGoodsItemNumber")

  def removeDocument[A](section: QuestionPage[A]): Try[UserAnswers] =
    removeExceptPaths(section, __ \ SequenceNumber, __ \ "type" \ "type")

  private def removeExceptPaths[A](section: QuestionPage[A], paths: JsPath*): Try[UserAnswers] =
    for {
      obj <- data
        .transform(section.path.json.pick[JsObject])
        .fold(
          errors => Failure(JsResultException(errors)),
          Success(_)
        )
      objWithPathsRetained = paths.foldLeft(Json.obj()) {
        case (acc, path) =>
          (
            for {
              pick <- obj.transform(path.json.pick)
              put  <- acc.transform(__.json.update(path.json.put(pick)))
            } yield put
          ).getOrElse(acc)
      }
      userAnswers <- objWithPathsRetained.fields match {
        case Nil    => remove(section)
        case values => set(section.path, JsObject(values :+ (Removed -> JsBoolean(true))))
      }
    } yield userAnswers
}

object UserAnswers {

  import play.api.libs.functional.syntax._

  implicit def reads(implicit sensitiveFormats: SensitiveFormats): Reads[UserAnswers] =
    (
      (__ \ "_id").read[ArrivalId] and
        (__ \ "mrn").read[MovementReferenceNumber] and
        (__ \ "eoriNumber").read[EoriNumber] and
        (__ \ "ie043Data").read[CC043CType](sensitiveFormats.cc043cReads) and
        (__ \ "data").read[JsObject](sensitiveFormats.jsObjectReads) and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantReads)
    )(UserAnswers.apply _)

  implicit def writes(implicit sensitiveFormats: SensitiveFormats): OWrites[UserAnswers] =
    writes(SensitiveWrites(sensitiveFormats))

  val auditWrites: OWrites[UserAnswers] =
    writes(SensitiveWrites())

  private def writes(sensitiveWrites: SensitiveWrites): OWrites[UserAnswers] =
    (
      (__ \ "_id").write[ArrivalId] and
        (__ \ "mrn").write[MovementReferenceNumber] and
        (__ \ "eoriNumber").write[EoriNumber] and
        (__ \ "ie043Data").write[CC043CType](sensitiveWrites.cc043cTypeWrites) and
        (__ \ "data").write[JsObject](sensitiveWrites.jsObjectWrites) and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantWrites)
    )(unlift(UserAnswers.unapply))

  implicit def format(implicit sensitiveFormats: SensitiveFormats): Format[UserAnswers] =
    Format(reads, writes)
}
