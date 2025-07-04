/*
 * Copyright 2024 HM Revenue & Customs
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

package utils.transformers

import models.UserAnswers
import pages.QuestionPage
import pages.sections.Section
import play.api.libs.json.{JsObject, Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}

trait PageTransformer {

  def set[T](page: QuestionPage[T], value: Option[T])(implicit writes: Writes[T], reads: Reads[T]): UserAnswers => Future[UserAnswers] =
    userAnswers =>
      value match {
        case Some(t) => set(page, t).apply(userAnswers)
        case None    => Future.successful(userAnswers)
      }

  def set[T](page: QuestionPage[T], t: T)(implicit writes: Writes[T], reads: Reads[T]): UserAnswers => Future[UserAnswers] =
    userAnswers => Future.fromTry(userAnswers.set(page, t))

  def set[T](
    page: QuestionPage[T],
    value: Option[String],
    lookup: String => Future[T]
  )(implicit writes: Writes[T], reads: Reads[T], ec: ExecutionContext): UserAnswers => Future[UserAnswers] =
    userAnswers =>
      value match {
        case Some(t) => set(page, t, lookup).apply(userAnswers)
        case None    => Future.successful(userAnswers)
      }

  def set[T](
    page: QuestionPage[T],
    value: String,
    lookup: String => Future[T]
  )(implicit writes: Writes[T], reads: Reads[T], ec: ExecutionContext): UserAnswers => Future[UserAnswers] =
    userAnswers => lookup(value).flatMap(set(page, _).apply(userAnswers))

  /** @param section
    *   a JsObject within a JsArray
    * @param sequenceNumber
    *   the sequence number as defined in the IE043
    * @return
    *   user answers with the sequence number and a `removed` value of `false`. We set this so we can distinguish between:
    *   - something that has been removed in session and;
    *   - something with no information provided from the IE043
    */
  def setSequenceNumber(section: Section[JsObject], sequenceNumber: BigInt)(implicit ec: ExecutionContext): UserAnswers => Future[UserAnswers] =
    setValue(section, SequenceNumber, sequenceNumber) andThen
      setValue(section, Removed, false)

  def setSequenceNumber(section: Section[JsObject], sequenceNumber: String)(implicit ec: ExecutionContext): UserAnswers => Future[UserAnswers] =
    setSequenceNumber(section, BigInt(sequenceNumber))

  private def setValue[A](section: Section[JsObject], key: String, value: A)(implicit writes: Writes[A]): UserAnswers => Future[UserAnswers] = userAnswers =>
    Future.fromTry(userAnswers.set(section.path \ key, value))
}
