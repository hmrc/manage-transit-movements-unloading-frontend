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

package navigation.houseConsignment.index.departureMeansOfTransport

import models._
import navigation.Navigator
import pages._
import pages.houseConsignment.index.departureMeansOfTransport._
import play.api.mvc.Call

class DepartureTransportMeansNavigator(houseConsignmentMode: Mode) extends Navigator {

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case TransportMeansIdentificationPage(houseConsignmentIndex, transportMeansIndex) =>
      ua =>
        Some(
          controllers.houseConsignment.index.departureMeansOfTransport.routes.IdentificationNumberController
            .onPageLoad(ua.id, houseConsignmentIndex, transportMeansIndex, houseConsignmentMode, NormalMode)
        )
    case VehicleIdentificationNumberPage(houseConsignmentIndex, transportMeansIndex) =>
      ua =>
        Some(
          controllers.houseConsignment.index.departureMeansOfTransport.routes.CountryController
            .onPageLoad(ua.id, houseConsignmentIndex, transportMeansIndex, houseConsignmentMode, NormalMode)
        )
    case CountryPage(houseConsignmentIndex, _) =>
      ua =>
        Some(
          controllers.houseConsignment.index.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController
            .onPageLoad(ua.id, houseConsignmentIndex, houseConsignmentMode)
        )
  }

  override def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {

    case TransportMeansIdentificationPage(houseConsignmentIndex, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case CountryPage(houseConsignmentIndex, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case VehicleIdentificationNumberPage(houseConsignmentIndex, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))

  }
}

object DepartureTransportMeansNavigator {

  class DepartureTransportMeansNavigatorProvider {

    def apply(houseConsignmentMode: Mode): DepartureTransportMeansNavigator =
      new DepartureTransportMeansNavigator(houseConsignmentMode)
  }
}
