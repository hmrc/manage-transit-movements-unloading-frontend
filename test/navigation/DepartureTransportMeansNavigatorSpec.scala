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

package navigation

import base.SpecBase
import generators.Generators
import models._
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.Country
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.departureMeansOfTransport._

class DepartureTransportMeansNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new DepartureTransportMeansNavigator

  "DepartureTransportMeansNavigator" - {

    "in Normal mode" - {

      val mode = NormalMode

      "must go from AddAnotherDepartureMeansOfTransport page" - {
        "when user answers Yes to AddIdentificationYesNo page" in {
          val userAnswers = emptyUserAnswers.setValue(AddAnotherDepartureMeansOfTransportPage(transportMeansIndex), true)

          navigator
            .nextPage(AddAnotherDepartureMeansOfTransportPage(transportMeansIndex), mode, userAnswers)
            .mustBe(controllers.departureMeansOfTransport.routes.AddIdentificationYesNoController.onPageLoad(arrivalId, transportMeansIndex, mode))
        }

        "when user answers No to UnloadingFindingsController" in {
          val userAnswers = emptyUserAnswers.setValue(AddAnotherDepartureMeansOfTransportPage(transportMeansIndex), false)

          navigator
            .nextPage(AddAnotherDepartureMeansOfTransportPage(transportMeansIndex), mode, userAnswers)
            .mustBe(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }
      }

      "must go from AddIdentificationYesNo page" - {
        "when user answers Yes to TransportMeansIdentification page" in {
          val userAnswers = emptyUserAnswers.setValue(AddIdentificationYesNoPage(transportMeansIndex), true)

          navigator
            .nextPage(AddIdentificationYesNoPage(transportMeansIndex), mode, userAnswers)
            .mustBe(controllers.departureMeansOfTransport.routes.IdentificationController.onPageLoad(arrivalId, transportMeansIndex, mode))
        }

        "when user answers No to AddIdentificationNumberYesNoPage" in {
          val userAnswers = emptyUserAnswers.setValue(AddIdentificationYesNoPage(transportMeansIndex), false)

          navigator
            .nextPage(AddIdentificationYesNoPage(transportMeansIndex), mode, userAnswers)
            .mustBe(controllers.departureMeansOfTransport.routes.AddIdentificationNumberYesNoController.onPageLoad(arrivalId, transportMeansIndex, mode))
        }
      }

      "must go from TransportMeansIdentificationPage to AddIdentificationNumberYesNoPage" - {

        val userAnswers = emptyUserAnswers.setValue(TransportMeansIdentificationPage(transportMeansIndex), TransportMeansIdentification("10", "test"))

        navigator
          .nextPage(TransportMeansIdentificationPage(transportMeansIndex), mode, userAnswers)
          .mustBe(controllers.departureMeansOfTransport.routes.AddIdentificationNumberYesNoController.onPageLoad(arrivalId, transportMeansIndex, mode))
      }

      "must go from AddIdentificationNumberYesNo page" - {
        "when user answers Yes to IdentificationNumber page" in {
          val userAnswers = emptyUserAnswers.setValue(AddIdentificationNumberYesNoPage(transportMeansIndex), true)

          navigator
            .nextPage(AddIdentificationNumberYesNoPage(transportMeansIndex), mode, userAnswers)
            .mustBe(controllers.departureMeansOfTransport.routes.IdentificationNumberController.onPageLoad(arrivalId, transportMeansIndex, mode))
        }

        "when user answers No to AddNationalityYesNoPage" in {
          val userAnswers = emptyUserAnswers.setValue(AddIdentificationNumberYesNoPage(transportMeansIndex), false)

          navigator
            .nextPage(AddIdentificationNumberYesNoPage(transportMeansIndex), mode, userAnswers)
            .mustBe(controllers.departureMeansOfTransport.routes.AddNationalityYesNoController.onPageLoad(arrivalId, transportMeansIndex, mode))
        }
      }

      "must go from IdentificationNumberPage to AddNationalityYesNo page" - {

        val userAnswers = emptyUserAnswers.setValue(VehicleIdentificationNumberPage(transportMeansIndex), "idNumber")

        navigator
          .nextPage(VehicleIdentificationNumberPage(transportMeansIndex), mode, userAnswers)
          .mustBe(controllers.departureMeansOfTransport.routes.AddNationalityYesNoController.onPageLoad(arrivalId, transportMeansIndex, mode))
      }

      "must go from AddNationalityYesNo page" - {
        "when user answers Yes to Country page" in {
          val userAnswers = emptyUserAnswers.setValue(AddNationalityYesNoPage(transportMeansIndex), true)

          navigator
            .nextPage(AddNationalityYesNoPage(transportMeansIndex), mode, userAnswers)
            .mustBe(controllers.departureMeansOfTransport.routes.CountryController.onPageLoad(arrivalId, transportMeansIndex, mode))
        }

        "when user answers No to AddNationalityYesNoPage" in {
          val userAnswers = emptyUserAnswers.setValue(AddNationalityYesNoPage(transportMeansIndex), false)

          navigator
            .nextPage(AddNationalityYesNoPage(transportMeansIndex), mode, userAnswers)
            .mustBe(controllers.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController.onPageLoad(arrivalId, mode))
        }
      }

      "must go from CountryPage to AddAnotherDepartureMeansOfTransport page" - {

        val userAnswers = emptyUserAnswers.setValue(CountryPage(transportMeansIndex), Country("UK", "test"))

        navigator
          .nextPage(CountryPage(transportMeansIndex), mode, userAnswers)
          .mustBe(controllers.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController.onPageLoad(arrivalId, mode))
      }
    }
  }
}
