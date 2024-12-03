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
import models.requests.DataRequest
import pages.NewAuthYesNoPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Call}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformers.IE043Transformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NewAuthYesNoSubmissionService @Inject() (sessionRepository: SessionRepository, dataTransformer: IE043Transformer) {

  def submitNewAuth(value: Boolean, redirectCall: UserAnswers => Call)(implicit
    request: DataRequest[AnyContent],
    headerCarrier: HeaderCarrier,
    ec: ExecutionContext
  ) = {
    val userAnswersF: Future[UserAnswers] =
      if (request.userAnswers.hasAnswerChanged(NewAuthYesNoPage, value) && value) {
        request.userAnswers.wipeAndTransform(dataTransformer.transform(_))
      } else {
        Future.successful(request.userAnswers)
      }

    for {
      userAnswers    <- userAnswersF
      updatedAnswers <- Future.fromTry(userAnswers.set(NewAuthYesNoPage, value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(redirectCall(updatedAnswers))
  }
}
