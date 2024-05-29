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

import controllers.houseConsignment.index.items.document.routes
import models.{DocType, Index, Mode, NormalMode, UserAnswers}
import navigation.Navigator
import pages._
import pages.houseConsignment.index.items.document._
import play.api.mvc.Call

class DocumentNavigator(houseConsignmentMode: Mode, itemMode: Mode) extends Navigator {

  override protected def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AdditionalInformationPage(houseConsignmentIndex, itemIndex, documentIndex) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case DocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, documentIndex) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case TypePage(houseConsignmentIndex, index, documentIndex) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
  }

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddAdditionalInformationYesNoPage(houseConsignmentIndex, itemIndex, documentIndex) =>
      ua => addAdditionalInformationYesNoNavigation(ua, houseConsignmentIndex, itemIndex, documentIndex, NormalMode)
    case AdditionalInformationPage(houseConsignmentIndex, itemIndex, documentIndex) =>
      ua => Some(routes.AddAnotherDocumentController.onPageLoad(ua.id, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode))
    case TypePage(houseConsignmentIndex, itemIndex, documentIndex) =>
      ua =>
        Some(
          routes.DocumentReferenceNumberController.onPageLoad(
            ua.id,
            houseConsignmentMode,
            itemMode,
            NormalMode,
            houseConsignmentIndex,
            itemIndex,
            documentIndex
          )
        )
    case DocumentReferenceNumberPage(houseConsignmentIndex, index, documentIndex) =>
      ua => documentReferenceNumberNavigation(ua, houseConsignmentIndex, index, documentIndex, NormalMode)
  }

  private def addAdditionalInformationYesNoNavigation(
    ua: UserAnswers,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    documentIndex: Index,
    documentMode: Mode
  ): Option[Call] =
    ua.get(AddAdditionalInformationYesNoPage(houseConsignmentIndex, itemIndex, documentIndex)).map {
      case true =>
        routes.AdditionalInformationController.onPageLoad(ua.id, houseConsignmentMode, itemMode, documentMode, houseConsignmentIndex, itemIndex, documentIndex)
      case false =>
        routes.AddAnotherDocumentController.onPageLoad(ua.id, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
    }

  private def documentReferenceNumberNavigation(
    ua: UserAnswers,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    documentIndex: Index,
    documentMode: Mode
  ): Option[Call] =
    ua.get(TypePage(houseConsignmentIndex, itemIndex, documentIndex)).map {
      _.`type` match {
        case DocType.Support =>
          routes.AddAdditionalInformationYesNoController
            .onPageLoad(ua.id, houseConsignmentMode, itemMode, documentMode, houseConsignmentIndex, itemIndex, documentIndex)
        case DocType.Transport =>
          routes.AddAnotherDocumentController.onPageLoad(ua.id, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
        case _ =>
          fallback(ua, houseConsignmentIndex, itemIndex, documentIndex, documentMode)
      }
    }

  private def fallback(ua: UserAnswers, houseConsignmentIndex: Index, itemIndex: Index, documentIndex: Index, documentMode: Mode): Call = {
    logger.warn(s"Previous document unexpectedly selected for item level document type")
    routes.TypeController.onPageLoad(ua.id, houseConsignmentMode, itemMode, documentMode, houseConsignmentIndex, itemIndex, documentIndex)
  }
}

object DocumentNavigator {

  class DocumentNavigatorProvider {

    def apply(houseConsignmentMode: Mode, itemMode: Mode): DocumentNavigator =
      new DocumentNavigator(houseConsignmentMode, itemMode)
  }
}
