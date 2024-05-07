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

import com.google.inject.Singleton
import models._
import navigation.Navigator
import pages._
import pages.houseConsignment.index.documents._
import play.api.mvc.Call

@Singleton
class HouseConsignmentDocumentNavigator extends Navigator {

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case TypePage(houseConsignmentIndex, documentIndex) =>
      ua =>
        Some(controllers.houseConsignment.index.documents.routes.ReferenceNumberController.onPageLoad(ua.id, NormalMode, houseConsignmentIndex, documentIndex))

    case DocumentReferenceNumberPage(houseConsignmentIndex, documentIndex) =>
      ua => documentReferenceNumberNavigation(ua, houseConsignmentIndex, documentIndex, NormalMode)

    case AddAdditionalInformationYesNoPage(houseConsignmentIndex, documentIndex) =>
      ua => addAdditionalInformationYesNoRoute(ua, houseConsignmentIndex, documentIndex, NormalMode)

    case AdditionalInformationPage(houseConsignmentIndex, _) =>
      ua => Some(controllers.houseConsignment.index.documents.routes.AddAnotherDocumentController.onPageLoad(ua.id, houseConsignmentIndex, NormalMode))

  }

  override def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {

    case TypePage(houseConsignmentIndex, _) => ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case DocumentReferenceNumberPage(houseConsignmentIndex, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case AdditionalInformationPage(houseConsignmentIndex, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))

  }

  private def addAdditionalInformationYesNoRoute(ua: UserAnswers, houseConsignmentIndex: Index, documentIndex: Index, mode: Mode): Option[Call] =
    ua.get(AddAdditionalInformationYesNoPage(houseConsignmentIndex, documentIndex)).map {
      case true =>
        controllers.houseConsignment.index.documents.routes.AdditionalInformationController.onPageLoad(ua.id, mode, houseConsignmentIndex, documentIndex)
      case false => controllers.houseConsignment.index.documents.routes.AddAnotherDocumentController.onPageLoad(ua.id, houseConsignmentIndex, mode)

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
          controllers.houseConsignment.index.documents.routes.AddAdditionalInformationYesNoController.onPageLoad(ua.id, houseConsignmentIndex, documentIndex)
        case DocType.Transport =>
          controllers.houseConsignment.index.documents.routes.AddAnotherDocumentController
            .onPageLoad(ua.id, houseConsignmentIndex, documentMode)
        case _ => Call("GET", "#") //TODO: Update document navigation
      }
    }
}
