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
import play.api.libs.json.{JsObject, Writes}

import scala.concurrent.Future

trait PageTransformer {

  def set[T](page: QuestionPage[T], value: Option[T])(implicit writes: Writes[T]): UserAnswers => Future[UserAnswers] = userAnswers =>
    value match {
      case Some(t) => set(page, t).apply(userAnswers)
      case None    => Future.successful(userAnswers)
    }

  def set[T](page: QuestionPage[T], t: T)(implicit writes: Writes[T]): UserAnswers => Future[UserAnswers] = userAnswers =>
    Future.fromTry(userAnswers.set(page, t))

  def setSequenceNumber(section: Section[JsObject], sequenceNumber: String): UserAnswers => Future[UserAnswers] =
    setValue(section, "sequenceNumber", sequenceNumber)

  def setPlaceholder(section: Section[JsObject]): UserAnswers => Future[UserAnswers] =
    setValue(section, "addedFromIE043", true)

  private def setValue[A](section: Section[JsObject], key: String, value: A)(implicit writes: Writes[A]): UserAnswers => Future[UserAnswers] = userAnswers =>
    Future.fromTry(userAnswers.set(section.path \ key, value))
}
