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
import models.*
import models.reference.Country
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.countriesOfRouting.CountryOfRoutingPage

class CountryOfRoutingNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator = new CountryOfRoutingNavigator

  "CountryOfRoutingNavigator" - {

    "in Normal mode" - {

      val mode = NormalMode

      "must go from CountryOfRoutingPage to AddAnotherCountryController" in {

        val userAnswers = emptyUserAnswers.setValue(CountryOfRoutingPage(index), Country("FR", "France"))

        navigator
          .nextPage(CountryOfRoutingPage(index), mode, userAnswers)
          .mustEqual(controllers.countriesOfRouting.routes.AddAnotherCountryController.onPageLoad(arrivalId, mode))
      }

      "in Check mode" - {

        val mode = CheckMode

        "must go from CountryOfRoutingPage to UnloadingFindingsController" in {

          val userAnswers = emptyUserAnswers.setValue(CountryOfRoutingPage(index), Country("FR", "France"))

          navigator
            .nextPage(CountryOfRoutingPage(index), mode, userAnswers)
            .mustEqual(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }
      }
    }
  }
}
