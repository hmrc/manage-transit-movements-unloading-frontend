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
import models.{CheckMode, NormalMode}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.houseConsignment.index.items.PackageTypeViewModel.PackageTypeViewModelProvider

class PackageTypeViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "must create view model" - {
    "when Normal mode" in {
      val viewModelProvider = new PackageTypeViewModelProvider()
      val result            = viewModelProvider.apply(itemIndex, houseConsignmentIndex, NormalMode)

      result.title mustBe "What type of package are you using for the item?"
      result.heading mustBe "What type of package are you using for the item?"
    }

    "when Check mode" in {
      val viewModelProvider = new PackageTypeViewModelProvider()

      val result = viewModelProvider.apply(itemIndex, houseConsignmentIndex, CheckMode)

      result.title mustBe "What is the new package type for item 1 in house consignment 1?"
      result.heading mustBe "What is the new package type for item 1 in house consignment 1?"
    }
  }
}
