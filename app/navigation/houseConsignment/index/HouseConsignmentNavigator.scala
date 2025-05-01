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
import config.FrontendAppConfig
import controllers.houseConsignment
import controllers.houseConsignment.index.{additionalReference, departureMeansOfTransport, documents, items, routes}
import models.*
import navigation.Navigator
import pages.Page
import pages.houseConsignment.index.{AddItemYesNoPage, *}
import play.api.mvc.Call

import javax.inject.Inject

@Singleton
class HouseConsignmentNavigator @Inject() (
  appConfig: FrontendAppConfig
) extends Navigator {

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case GrossWeightPage(houseConsignmentIndex) =>
      ua => grossWeightPageRoute(ua, houseConsignmentIndex)
    case UniqueConsignmentReferenceYesNoPage(houseConsignmentIndex) =>
      ua => addUniqueConsignmentReferenceYesNoRoute(ua, houseConsignmentIndex, NormalMode)
    case UniqueConsignmentReferencePage(houseConsignmentIndex) =>
      ua => Some(controllers.houseConsignment.index.routes.AddDepartureTransportMeansYesNoController.onPageLoad(ua.id, houseConsignmentIndex, NormalMode))
    case AddDepartureTransportMeansYesNoPage(houseConsignmentIndex) =>
      ua => addDepartureTransportMeansYesNoRoute(ua, houseConsignmentIndex, NormalMode)
    case AddDocumentYesNoPage(houseConsignmentIndex) =>
      ua => addDocumentsYesNoRoute(ua, houseConsignmentIndex, NormalMode)
    case AddAdditionalReferenceYesNoPage(houseConsignmentIndex) =>
      ua => addAdditionalReferencesYesNoRoute(ua, houseConsignmentIndex, NormalMode)
    case AddItemYesNoPage(houseConsignmentIndex) =>
      ua => addItemsYesNoRoute(ua, houseConsignmentIndex, NormalMode)
  }

  override def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case GrossWeightPage(houseConsignmentIndex) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case UniqueConsignmentReferencePage(houseConsignmentIndex) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case AddDepartureTransportMeansYesNoPage(houseConsignmentIndex) =>
      ua => addDepartureTransportMeansYesNoRoute(ua, houseConsignmentIndex, CheckMode)
    case AddDocumentYesNoPage(houseConsignmentIndex) =>
      ua => addDocumentsYesNoRoute(ua, houseConsignmentIndex, CheckMode)
    case AddAdditionalReferenceYesNoPage(houseConsignmentIndex) =>
      ua => addAdditionalReferencesYesNoRoute(ua, houseConsignmentIndex, CheckMode)
    case AddItemYesNoPage(houseConsignmentIndex) =>
      ua => addItemsYesNoRoute(ua, houseConsignmentIndex, CheckMode)
  }

  private def addDepartureTransportMeansYesNoRoute(
    ua: UserAnswers,
    houseConsignmentIndex: Index,
    houseConsignmentMode: Mode
  ): Option[Call] =
    ua.get(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex)).map {
      case true =>
        departureMeansOfTransport.routes.IdentificationController.onPageLoad(ua.id, houseConsignmentIndex, Index(0), houseConsignmentMode, NormalMode)
      case false =>
        houseConsignmentMode match {
          case NormalMode =>
            routes.AddDocumentsYesNoController.onPageLoad(ua.id, houseConsignmentMode, houseConsignmentIndex)
          case CheckMode =>
            controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex)
        }
    }

  private def addDocumentsYesNoRoute(
    ua: UserAnswers,
    houseConsignmentIndex: Index,
    houseConsignmentMode: Mode
  ): Option[Call] =
    ua.get(AddDocumentYesNoPage(houseConsignmentIndex)).map {
      case true =>
        documents.routes.TypeController.onPageLoad(ua.id, houseConsignmentMode, NormalMode, houseConsignmentIndex, Index(0))
      case false =>
        houseConsignmentMode match {
          case NormalMode =>
            routes.AddAdditionalReferenceYesNoController.onPageLoad(ua.id, houseConsignmentMode, houseConsignmentIndex)
          case CheckMode =>
            controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex)
        }
    }

  private def addAdditionalReferencesYesNoRoute(
    ua: UserAnswers,
    houseConsignmentIndex: Index,
    houseConsignmentMode: Mode
  ): Option[Call] =
    ua.get(AddAdditionalReferenceYesNoPage(houseConsignmentIndex)).map {
      case true =>
        additionalReference.routes.AdditionalReferenceTypeController.onPageLoad(ua.id, houseConsignmentMode, NormalMode, houseConsignmentIndex, Index(0))
      case false =>
        houseConsignmentMode match {
          case NormalMode =>
            routes.AddItemYesNoController.onPageLoad(ua.id, houseConsignmentIndex, houseConsignmentMode)
          case CheckMode =>
            controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex)
        }
    }

  private def addItemsYesNoRoute(
    ua: UserAnswers,
    houseConsignmentIndex: Index,
    houseConsignmentMode: Mode
  ): Option[Call] =
    ua.get(AddItemYesNoPage(houseConsignmentIndex)).map {
      case true =>
        items.routes.DescriptionController.onPageLoad(ua.id, houseConsignmentMode, NormalMode, houseConsignmentIndex, Index(0))
      case false =>
        houseConsignmentMode match {
          case NormalMode =>
            houseConsignment.routes.AddAnotherHouseConsignmentController.onPageLoad(ua.id, houseConsignmentMode)
          case CheckMode =>
            controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex)
        }
    }

  private def addUniqueConsignmentReferenceYesNoRoute(
    ua: UserAnswers,
    houseConsignmentIndex: Index,
    houseConsignmentMode: Mode
  ): Option[Call] =
    ua.get(UniqueConsignmentReferenceYesNoPage(houseConsignmentIndex)).map {
      case true =>
        controllers.houseConsignment.index.routes.UniqueConsignmentReferenceController.onPageLoad(ua.id, houseConsignmentIndex, houseConsignmentMode)
      case false =>
        controllers.houseConsignment.index.routes.AddDepartureTransportMeansYesNoController.onPageLoad(ua.id, houseConsignmentIndex, houseConsignmentMode)

    }

  private def grossWeightPageRoute(ua: UserAnswers, houseConsignmentIndex: Index): Option[Call] =
    appConfig.phase6Enabled match {
      case true  => Some(routes.UniqueConsignmentReferenceYesNoController.onPageLoad(ua.id, houseConsignmentIndex, NormalMode))
      case false => Some(routes.AddDepartureTransportMeansYesNoController.onPageLoad(ua.id, houseConsignmentIndex, NormalMode))
    }
}
