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

package navigation.houseConsignment.index.departureTransportMeans

import base.SpecBase
import generators.Generators
import models._
import models.reference.{Country, TransportMeansIdentification}
import navigation.houseConsignment.index.departureMeansOfTransport.HCDepartureTransportMeansNavigator
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.departureMeansOfTransport._

class HCDepartureTransportMeansNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new HCDepartureTransportMeansNavigator

  "DepartureTransportMeansNavigator" - {

    "in Check mode" - {

      val mode = CheckMode

      "must go from TransportMeansIdentificationPage to HouseConsignmentController" in {

        val userAnswers = emptyUserAnswers
          .setValue(TransportMeansIdentificationPage(hcIndex, transportMeansIndex), TransportMeansIdentification("test", "test"))

        navigator
          .nextPage(TransportMeansIdentificationPage(hcIndex, transportMeansIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))

      }

      "must go from CountryPage to HouseConsignmentController" in {

        val userAnswers = emptyUserAnswers
          .setValue(CountryPage(hcIndex, transportMeansIndex), Country("GB", "test"))

        navigator
          .nextPage(CountryPage(hcIndex, transportMeansIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))

      }

      "must go from VehicleIdentificationNumberPage to HouseConsignmentController" in {

        val userAnswers = emptyUserAnswers
          .setValue(VehicleIdentificationNumberPage(hcIndex, transportMeansIndex), "test")

        navigator
          .nextPage(VehicleIdentificationNumberPage(hcIndex, transportMeansIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))

      }
    }
  }
}
