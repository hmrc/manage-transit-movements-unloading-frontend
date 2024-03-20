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
import models._
import pages._
import pages.departureMeansOfTransport._
import play.api.mvc.Call

@Singleton
class DepartureTransportMeansNavigator extends Navigator {

  override def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddIdentificationYesNoPage(transportIndex) => ua => addIdentificationYesNoNavigation(ua.id, ua, transportIndex)
    case TransportMeansIdentificationPage(transportIndex) =>
      ua => Some(controllers.departureMeansOfTransport.routes.AddIdentificationNumberYesNoController.onPageLoad(ua.id, transportIndex, NormalMode))
    case AddIdentificationNumberYesNoPage(transportIndex) => ua => addIdentificationNumberYesNoNavigation(ua.id, ua, transportIndex)
    case VehicleIdentificationNumberPage(transportIndex) =>
      ua => Some(controllers.departureMeansOfTransport.routes.AddNationalityYesNoController.onPageLoad(ua.id, transportIndex, NormalMode))
    case AddNationalityYesNoPage(transportIndex) => ua => addNationalityYesNoNavigation(ua.id, ua, transportIndex)
    case CountryPage(_) =>
      ua => Some(controllers.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController.onPageLoad(ua.id, NormalMode))

  }

  override def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {

    case TransportMeansIdentificationPage(_) => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
    case CountryPage(_)                      => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
    case VehicleIdentificationNumberPage(_)  => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))

  }

  private def addIdentificationYesNoNavigation(arrivalId: ArrivalId, ua: UserAnswers, transportIndex: Index): Option[Call] =
    ua.get(AddIdentificationYesNoPage(transportIndex)).map {
      case true =>
        controllers.departureMeansOfTransport.routes.IdentificationController.onPageLoad(arrivalId, transportIndex, NormalMode)
      case false =>
        controllers.departureMeansOfTransport.routes.AddIdentificationNumberYesNoController.onPageLoad(arrivalId, transportIndex, NormalMode)
    }

  private def addIdentificationNumberYesNoNavigation(arrivalId: ArrivalId, ua: UserAnswers, transportIndex: Index): Option[Call] =
    ua.get(AddIdentificationNumberYesNoPage(transportIndex)).map {
      case true =>
        controllers.departureMeansOfTransport.routes.IdentificationNumberController.onPageLoad(arrivalId, transportIndex, NormalMode)
      case false =>
        controllers.departureMeansOfTransport.routes.AddNationalityYesNoController.onPageLoad(arrivalId, transportIndex, NormalMode)
    }

  private def addNationalityYesNoNavigation(arrivalId: ArrivalId, ua: UserAnswers, transportIndex: Index): Option[Call] =
    ua.get(AddNationalityYesNoPage(transportIndex)).map {
      case true =>
        controllers.departureMeansOfTransport.routes.CountryController.onPageLoad(arrivalId, transportIndex, NormalMode)
      case false =>
        controllers.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController.onPageLoad(arrivalId, NormalMode)
    }

}
