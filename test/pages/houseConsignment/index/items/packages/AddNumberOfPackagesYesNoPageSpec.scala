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

package pages.houseConsignment.index.items.packages

import pages.behaviours.PageBehaviours

class AddNumberOfPackagesYesNoPageSpec extends PageBehaviours {

  "AddPackageShippingMarkYesNoPage" - {

    beRetrievable[Boolean](AddNumberOfPackagesYesNoPage(index, index, index))

    beSettable[Boolean](AddNumberOfPackagesYesNoPage(index, index, index))

    beRemovable[Boolean](AddNumberOfPackagesYesNoPage(index, index, index))

    "cleanup" - {
      "must remove number of packages when no selected" in {
        forAll(positiveBigInts) {
          value =>
            val userAnswers = emptyUserAnswers
              .setValue(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), true)
              .setValue(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), value)

            val result = userAnswers.setValue(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), false)

            result.get(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex)) must not be defined

        }
      }

      "must keep number of packages when yes selected" in {
        forAll(positiveBigInts) {
          value =>
            val userAnswers = emptyUserAnswers
              .setValue(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), true)
              .setValue(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), value)

            val result = userAnswers.setValue(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), true)

            result.get(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex)) mustBe defined
        }
      }
    }
  }
}
