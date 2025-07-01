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

package viewModels.houseConsignment.index.documents

import base.SpecBase
import generators.Generators
import models.{CheckMode, NormalMode}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.houseConsignment.index.documents.AdditionalInformationViewModel.AdditionalInformationViewModelProvider

class AdditionalInformationViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "must create view model" - {
    "when Normal mode" in {
      val viewModelProvider = new AdditionalInformationViewModelProvider()
      val result            = viewModelProvider.apply(NormalMode, hcIndex)

      result.title mustBe s"Enter the additional information in house consignment ${hcIndex.display}"
      result.heading mustBe s"Enter the additional information in house consignment ${hcIndex.display}"
    }

    "when Check mode" in {
      val viewModelProvider = new AdditionalInformationViewModelProvider()
      val result            = viewModelProvider.apply(CheckMode, hcIndex)

      result.title mustBe s"Enter the new additional information in house consignment ${hcIndex.display}"
      result.heading mustBe s"Enter the new additional information in house consignment ${hcIndex.display}"
    }
  }
}
