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

package navigation.houseConsignment.index.items

import base.SpecBase
import generators.Generators
import models._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.packages.AddNumberOfPackagesYesNoPage

class AddPackagesNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new AddPackagesNavigator

  "AddPackagesNavigator" - {

    "in Normal mode" - {

      val mode = NormalMode

      "must go from AddNumberOfPackagesYesNoPage to NumberOfPackages page when answer is true" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), true)

        navigator
          .nextPage(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.packages.routes.NumberOfPackagesController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode)
          )
      }

      "must go from AddNumberOfPackagesYesNoPage to AddPackageShippingMarkYesNo page when answer is false" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), false)

        navigator
          .nextPage(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.packages.routes.AddPackageShippingMarkYesNoController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode)
          )
      }

    }
  }
}
