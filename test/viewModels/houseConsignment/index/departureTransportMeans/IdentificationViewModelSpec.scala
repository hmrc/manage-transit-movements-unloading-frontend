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

package viewModels.houseConsignment.index.departureTransportMeans

import base.SpecBase
import generators.Generators
import models.{CheckMode, NormalMode}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.houseConsignment.index.departureTransportMeans.IdentificationViewModel.IdentificationViewModelProvider

class IdentificationViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "must create view model" - {
    "when NormalMode" in {
      val viewModelProvider = new IdentificationViewModelProvider()
      val result            = viewModelProvider.apply(NormalMode, houseConsignmentIndex)(messages)

      result.title mustEqual "Which identification do you want to use for the departure means of transport in house consignment 1?"
      result.heading mustEqual "Which identification do you want to use for the departure means of transport in house consignment 1?"
      result.paragraph must not be defined
    }

    "when CheckMode" in {
      val viewModelProvider = new IdentificationViewModelProvider()

      val result = viewModelProvider.apply(CheckMode, houseConsignmentIndex)(messages)

      result.title mustEqual "Which identification do you want to use for the new departure means of transport in house consignment 1?"
      result.heading mustEqual "Which identification do you want to use for the new departure means of transport in house consignment 1?"
      result.paragraph.value mustEqual "This is the means of transport used from the UK office of departure to a UK port or airport."
    }
  }
}
