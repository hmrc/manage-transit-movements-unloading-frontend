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

package viewModels

import base.SpecBase
import generators.Generators
import models.{CheckMode, NormalMode}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.PackageShippingMarksViewModel.PackageShippingMarksViewModelProvider

class PackageShippingMarksViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "must create view model in normalMode" in {
    val viewModelProvider = new PackageShippingMarksViewModelProvider()
    val result            = viewModelProvider.apply(hcIndex, itemIndex, NormalMode)
    result.title mustBe messages("houseConsignment.item.packageShippingMark.normalMode.title")
    result.heading mustBe messages("houseConsignment.item.packageShippingMark.normalMode.title")
  }

  "must create view model in checkMode" in {
    val viewModelProvider = new PackageShippingMarksViewModelProvider()
    val result            = viewModelProvider.apply(hcIndex, itemIndex, CheckMode)

    result.title mustBe messages("houseConsignment.item.packageShippingMark.checkMode.title", hcIndex.display, itemIndex.display)
    result.heading mustBe messages("houseConsignment.item.packageShippingMark.checkMode.heading", hcIndex.display, itemIndex.display)

  }
}
