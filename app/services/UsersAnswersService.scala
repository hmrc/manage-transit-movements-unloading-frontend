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
import pages.{DidUserChooseNewProcedurePage, QuestionPage}
import play.api.libs.json.{Json, Reads, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformers.IE043Transformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class UsersAnswersService @Inject() (dataTransformer: IE043Transformer) {

  def retainAndTransform[A](userAnswers: UserAnswers, page: QuestionPage[A])(
    block: UserAnswers => Future[UserAnswers]
  )(implicit rds: Reads[A], writes: Writes[A], ec: ExecutionContext): Future[UserAnswers] =
    for {
      transformedAnswers <- wipeAndTransform(userAnswers, block)
      updatedAnswers <- Future.fromTry {
        userAnswers.get(page).fold(Try(transformedAnswers))(transformedAnswers.set(page, _))
      }
    } yield updatedAnswers

  def wipeAndTransform(userAnswers: UserAnswers, block: UserAnswers => Future[UserAnswers]): Future[UserAnswers] = {
    val wipedAnswers = userAnswers.copy(data = Json.obj())
    block(wipedAnswers)
  }

  def updateConditionalAndWipe(page: QuestionPage[Boolean], value: Boolean, userAnswers: UserAnswers)(implicit
    headerCarrier: HeaderCarrier,
    ec: ExecutionContext
  ): Future[UserAnswers] = {
    val userAnswersF: Future[UserAnswers] =
      if (userAnswers.hasAnswerChanged(page, value) && value) {
        // keep DidUserChooseNewProcedurePage not to lose user's original new procedure answer
        retainAndTransform(userAnswers, DidUserChooseNewProcedurePage)(dataTransformer.transform(_))
      } else {
        Future.successful(userAnswers)
      }

    for {
      userAnswers    <- userAnswersF
      updatedAnswers <- Future.fromTry(userAnswers.set(page, value))
    } yield updatedAnswers
  }
}
