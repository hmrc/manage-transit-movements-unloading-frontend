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

package viewModels.houseConsignment.index.items.packages

import base.SpecBase
import generators.Generators
import models.{CheckMode, NormalMode}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import NumberOfPackagesViewModel.NumberOfPackagesViewModelProvider

class NumberOfPackagesViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "must create view model" - {
    "when NormalMode" in {
      val viewModelProvider = new NumberOfPackagesViewModelProvider()
      val result            = viewModelProvider.apply(hcIndex, itemIndex, NormalMode)(messages)

      result.title mustBe "How many of this package are you using?"
      result.heading mustBe "How many of this package are you using?"
    }

    "when CheckMode" in {
      val viewModelProvider = new NumberOfPackagesViewModelProvider()

      val result = viewModelProvider.apply(hcIndex, itemIndex, CheckMode)(messages)

      result.title mustBe "How many of this package are you using for item 1 in house consignment 1?"
      result.heading mustBe "How many of this package are you using for item 1 in house consignment 1?"
    }
  }
}
