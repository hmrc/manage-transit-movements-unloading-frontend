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

package viewModels.houseConsignment.index.departureTransportMeans

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.{CheckMode, NormalMode}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.houseConsignment.index.departureTransportMeans.HouseConsignmentCountryViewModel.HouseConsignmentCountryViewModelProvider

class HouseConsignmentCountryViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "must create view model" - {
    "when NormalMode" in {
      val viewModelProvider = new HouseConsignmentCountryViewModelProvider()
      val result            = viewModelProvider.apply(NormalMode, houseConsignmentIndex)(messages)

      result.title mustEqual "What country is this vehicle registered to?"
      result.heading mustEqual "What country is this vehicle registered to?"
    }

    "when CheckMode" in {
      val viewModelProvider = new HouseConsignmentCountryViewModelProvider()

      val result = viewModelProvider.apply(CheckMode, houseConsignmentIndex)(messages)

      result.title mustEqual s"What country is the new vehicle registered to in house consignment ${houseConsignmentIndex.display}?"
      result.heading mustEqual s"What country is the new vehicle registered to in house consignment ${houseConsignmentIndex.display}?"
    }
  }
}
