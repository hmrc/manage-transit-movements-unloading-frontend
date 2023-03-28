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

package services.P5

import models.UserAnswers
import pages.sections.NavigationDataSection
import pages._

import scala.util.{Success, Try}

object UserAnswersSubmissionService {

  def removeNavigationData(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.remove(NavigationDataSection)

  def setStateOfSeals(userAnswers: UserAnswers): Try[UserAnswers] =
    (userAnswers.get(CanSealsBeReadPage), userAnswers.get(AreAnySealsBrokenPage)) match {
      case (Some(true), Some(false)) => userAnswers.set(StateOfSealsPage, "1")
      case (Some(_), Some(_))        => userAnswers.set(StateOfSealsPage, "0")
      case _                         => Success(userAnswers)
    }

  def userAnswersToSubmission(startUserAnswers: UserAnswers): Try[UserAnswers] =
    for {
      ua1              <- setStateOfSeals(startUserAnswers)
      finalUserAnswers <- removeNavigationData(ua1)
    } yield finalUserAnswers

}
