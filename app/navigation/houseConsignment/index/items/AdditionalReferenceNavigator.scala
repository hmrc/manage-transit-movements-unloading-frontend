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

package navigation.houseConsignment.index.items

import models.{Index, NormalMode, UserAnswers}
import controllers.houseConsignment.index.items.additionalReference.routes
import navigation.Navigator
import pages.Page
import pages.houseConsignment.index.items.additionalReference.{
  AddAdditionalReferenceNumberYesNoPage,
  AdditionalReferenceNumberPage,
  AdditionalReferenceTypePage
}
import play.api.mvc.Call

class AdditionalReferenceNavigator extends Navigator {

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {

    case AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, referenceIndex) =>
      ua => Some(routes.AddAdditionalReferenceNumberYesNoController.onPageLoad(ua.id, NormalMode, houseConsignmentIndex, itemIndex, referenceIndex))
    case AddAdditionalReferenceNumberYesNoPage(houseConsignmentIndex, itemIndex, referenceIndex) =>
      ua => additionalReferenceNumberYesNoRoute(ua, houseConsignmentIndex, itemIndex, referenceIndex)
    case AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, _) =>
      ua => Some(routes.AddAnotherAdditionalReferenceController.onPageLoad(ua.id, NormalMode, houseConsignmentIndex, itemIndex))
  }

  private def additionalReferenceNumberYesNoRoute(ua: UserAnswers, houseConsignmentIndex: Index, itemIndex: Index, referenceIndex: Index): Option[Call] =
    ua.get(AddAdditionalReferenceNumberYesNoPage(houseConsignmentIndex, itemIndex, referenceIndex)) map {
      case true =>
        routes.AdditionalReferenceNumberController.onPageLoad(ua.id, NormalMode, houseConsignmentIndex, itemIndex, referenceIndex)
      case false =>
        routes.AddAnotherAdditionalReferenceController.onPageLoad(ua.id, NormalMode, houseConsignmentIndex, itemIndex)
    }

  override protected def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AdditionalReferenceTypePage(houseConsignmentIndex, _, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case AdditionalReferenceNumberPage(houseConsignmentIndex, _, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))

  }
}
