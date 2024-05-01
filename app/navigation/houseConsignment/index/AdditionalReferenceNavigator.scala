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

package navigation.houseConsignment.index

import models.{Index, NormalMode, UserAnswers}
import navigation.Navigator
import pages.Page
import pages.houseConsignment.index.additionalReference.{
  AddHouseConsignmentAdditionalReferenceNumberYesNoPage,
  HouseConsignmentAdditionalReferenceNumberPage,
  HouseConsignmentAdditionalReferenceTypePage
}
import play.api.mvc.Call

class AdditionalReferenceNavigator extends Navigator {

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {

    case HouseConsignmentAdditionalReferenceTypePage(houseConsignmentIndex, referenceIndex) =>
      ua =>
        Some(
          controllers.houseConsignment.index.additionalReference.routes.AddAdditionalReferenceNumberYesNoController
            .onPageLoad(ua.id, NormalMode, houseConsignmentIndex, referenceIndex)
        )
    case HouseConsignmentAdditionalReferenceNumberPage(houseConsignmentIndex, _) =>
      ua =>
        Some(
          controllers.houseConsignment.index.additionalReference.routes.AddAnotherAdditionalReferenceController
            .onPageLoad(ua.id, NormalMode, houseConsignmentIndex)
        )
    case AddHouseConsignmentAdditionalReferenceNumberYesNoPage(houseConsignmentIndex, referenceIndex) =>
      ua => additionalReferenceNumberYesNoRoute(ua, houseConsignmentIndex, referenceIndex)
  }

  private def additionalReferenceNumberYesNoRoute(ua: UserAnswers, houseConsignmentIndex: Index, referenceIndex: Index): Option[Call] =
    ua.get(AddHouseConsignmentAdditionalReferenceNumberYesNoPage(houseConsignmentIndex, referenceIndex)) map {
      case true =>
        controllers.houseConsignment.index.additionalReference.routes.AdditionalReferenceNumberController
          .onPageLoad(ua.id, NormalMode, houseConsignmentIndex, referenceIndex)
      case false =>
        controllers.houseConsignment.index.additionalReference.routes.AddAnotherAdditionalReferenceController
          .onPageLoad(ua.id, NormalMode, houseConsignmentIndex)
    }

  override protected def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case HouseConsignmentAdditionalReferenceTypePage(houseConsignmentIndex, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case HouseConsignmentAdditionalReferenceNumberPage(houseConsignmentIndex, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))

  }
}
