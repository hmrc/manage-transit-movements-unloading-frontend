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

package viewModels.houseConsignment.index.items

import base.SpecBase
import generators.Generators
import models.{CheckMode, Index, Mode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.houseConsignment.index.items.CustomsUnionAndStatisticsCodeViewModel.CustomsUnionAndStatisticsCodeViewModelProvider

class CustomsUnionAndStatisticsCodeViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val houseConsignmentMode = arbitrary[Mode].sample.value

  "must create view model" - {
    "when Normal mode" in {
      val viewModelProvider = new CustomsUnionAndStatisticsCodeViewModelProvider()
      val result            = viewModelProvider.apply(arrivalId, houseConsignmentMode, NormalMode, Index(0), Index(1))

      result.title mustBe "What is the Customs Union and Statistics (CUS) code?"
      result.heading mustBe "What is the Customs Union and Statistics (CUS) code?"
      result.requiredError mustBe "Enter the Customs Union and Statistics (CUS) code"
    }

    "when Check mode" in {
      val viewModelProvider = new CustomsUnionAndStatisticsCodeViewModelProvider()

      val result = viewModelProvider.apply(arrivalId, houseConsignmentMode, CheckMode, Index(0), Index(1))

      result.title mustBe "What is the new Customs Union and Statistics (CUS) code for item 2 in house consignment 1?"
      result.heading mustBe "What is the new Customs Union and Statistics (CUS) code for item 2 in house consignment 1?"
      result.requiredError mustBe "Enter the new Customs Union and Statistics (CUS) code for item 2 in house consignment 1"
    }
  }
}
