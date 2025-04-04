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

package services

import models.UserAnswers
import pages.QuestionPage
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformers.IE043Transformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserAnswersService @Inject() (dataTransformer: IE043Transformer) {

  def retainAndTransform(
    userAnswers: UserAnswers,
    pages: QuestionPage[?]*
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[UserAnswers] =
    for {
      transformedAnswers <- wipeAndTransform(userAnswers) {
        dataTransformer.transform(_)
      }
      updatedAnswers = pages.foldLeft(transformedAnswers) {
        case (acc, page) =>
          userAnswers.getAndCopyTo(page.path, acc)
      }
    } yield updatedAnswers

  def wipeAndTransform(userAnswers: UserAnswers)(block: UserAnswers => Future[UserAnswers]): Future[UserAnswers] =
    block(userAnswers.copy(data = Json.obj()))
}
