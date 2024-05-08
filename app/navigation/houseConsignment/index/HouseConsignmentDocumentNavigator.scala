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

import controllers.houseConsignment.index.documents.routes
import models._
import navigation.Navigator
import pages._
import pages.houseConsignment.index.documents._
import play.api.mvc.Call

class HouseConsignmentDocumentNavigator(houseConsignmentMode: Mode) extends Navigator {

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case TypePage(houseConsignmentIndex, documentIndex) =>
      ua => Some(routes.ReferenceNumberController.onPageLoad(ua.id, houseConsignmentMode, NormalMode, houseConsignmentIndex, documentIndex))

    case DocumentReferenceNumberPage(houseConsignmentIndex, documentIndex) =>
      ua => documentReferenceNumberNavigation(ua, houseConsignmentIndex, documentIndex, NormalMode)

    case AddAdditionalInformationYesNoPage(houseConsignmentIndex, documentIndex) =>
      ua => addAdditionalInformationYesNoRoute(ua, houseConsignmentIndex, documentIndex, NormalMode)

    case AdditionalInformationPage(houseConsignmentIndex, _) =>
      ua => Some(routes.AddAnotherDocumentController.onPageLoad(ua.id, houseConsignmentIndex, houseConsignmentMode))

  }

  override def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case TypePage(houseConsignmentIndex, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case DocumentReferenceNumberPage(houseConsignmentIndex, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case AdditionalInformationPage(houseConsignmentIndex, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
  }

  private def addAdditionalInformationYesNoRoute(ua: UserAnswers, houseConsignmentIndex: Index, documentIndex: Index, documentMode: Mode): Option[Call] =
    ua.get(AddAdditionalInformationYesNoPage(houseConsignmentIndex, documentIndex)).map {
      case true =>
        routes.AdditionalInformationController.onPageLoad(ua.id, houseConsignmentMode, documentMode, houseConsignmentIndex, documentIndex)
      case false =>
        routes.AddAnotherDocumentController.onPageLoad(ua.id, houseConsignmentIndex, houseConsignmentMode)
    }

  private def documentReferenceNumberNavigation(
    ua: UserAnswers,
    houseConsignmentIndex: Index,
    documentIndex: Index,
    documentMode: Mode
  ): Option[Call] =
    ua.get(TypePage(houseConsignmentIndex, documentIndex)).map {
      _.`type` match {
        case DocType.Support =>
          routes.AddAdditionalInformationYesNoController.onPageLoad(ua.id, houseConsignmentMode, documentMode, houseConsignmentIndex, documentIndex)
        case DocType.Transport =>
          routes.AddAnotherDocumentController.onPageLoad(ua.id, houseConsignmentIndex, houseConsignmentMode)
        case _ => Call("GET", "#") //TODO: Update document navigation
      }
    }
}

object HouseConsignmentDocumentNavigator {

  class HouseConsignmentDocumentNavigatorProvider {

    def apply(houseConsignmentMode: Mode): HouseConsignmentDocumentNavigator =
      new HouseConsignmentDocumentNavigator(houseConsignmentMode)
  }
}
