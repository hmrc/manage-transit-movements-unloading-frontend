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

package navigation

import com.google.inject.Singleton
import models.{Index, NormalMode, UserAnswers}
import pages._
import pages.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferenceNumberYesNoPage, AdditionalReferenceTypePage}
import play.api.mvc.Call

@Singleton
class AdditionalReferenceNavigator extends Navigator {

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {

    case AdditionalReferenceTypePage(referenceIndex) =>
      ua => Some(controllers.additionalReference.index.routes.AdditionalReferenceNumberYesNoController.onPageLoad(ua.id, referenceIndex, NormalMode))
    case AdditionalReferenceNumberPage(referenceIndex) =>
      ua => Some(controllers.additionalReference.index.routes.AddAnotherAdditionalReferenceController.onPageLoad(ua.id, NormalMode))
    case AdditionalReferenceNumberYesNoPage(referenceIndex) => ua => additionalReferenceNumberYesNoRoute(ua, referenceIndex)
  }

  private def additionalReferenceNumberYesNoRoute(ua: UserAnswers, referenceIndex: Index): Option[Call] =
    ua.get(AdditionalReferenceNumberYesNoPage(referenceIndex)) map {
      case true =>
        controllers.additionalReference.index.routes.AdditionalReferenceNumberController.onPageLoad(ua.id, referenceIndex, NormalMode)
      case false =>
        controllers.additionalReference.index.routes.AddAnotherAdditionalReferenceController.onPageLoad(ua.id, NormalMode)
    }

  override protected def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AdditionalReferenceTypePage(_)   => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
    case AdditionalReferenceNumberPage(_) => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))

  }

}
