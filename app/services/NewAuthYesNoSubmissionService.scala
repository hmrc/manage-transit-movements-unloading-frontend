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
import pages.NewAuthYesNoPage
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformers.IE043Transformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NewAuthYesNoSubmissionService @Inject() (dataTransformer: IE043Transformer) {

  def updateUserAnswers(value: Boolean, userAnswers: UserAnswers)(implicit
    headerCarrier: HeaderCarrier,
    ec: ExecutionContext
  ): Future[UserAnswers] = {
    val userAnswersF: Future[UserAnswers] =
      if (userAnswers.hasAnswerChanged(NewAuthYesNoPage, value) && value) {
        userAnswers.wipeAndTransform(dataTransformer.transform(_))
      } else {
        Future.successful(userAnswers)
      }

    for {
      userAnswers    <- userAnswersF
      updatedAnswers <- Future.fromTry(userAnswers.set(NewAuthYesNoPage, value))
    } yield updatedAnswers
  }
}
