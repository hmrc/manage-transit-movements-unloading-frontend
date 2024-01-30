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
import play.api.libs.json.Writes

import scala.concurrent.Future

trait PageTransformer {

  def set[T](page: QuestionPage[T], t: T)(implicit writes: Writes[T]): UserAnswers => Future[UserAnswers] = userAnswers =>
    Future.fromTry(userAnswers.set(page, t))

  def set[T](page: QuestionPage[T], value: Option[T])(implicit writes: Writes[T]): UserAnswers => Future[UserAnswers] = userAnswers =>
    value match {
      case Some(t) => set(page, t).apply(userAnswers)
      case None    => Future.successful(userAnswers)
    }

}