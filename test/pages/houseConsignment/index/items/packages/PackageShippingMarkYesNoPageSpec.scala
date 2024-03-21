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

class PackageShippingMarkYesNoPageSpec extends PageBehaviours {

  "PackageShippingMarkYesNoPage" - {

    beRetrievable[Boolean](PackageShippingMarkYesNoPage(index, index, index))

    beSettable[Boolean](PackageShippingMarkYesNoPage(index, index, index))

    beRemovable[Boolean](PackageShippingMarkYesNoPage(index, index, index))

    "cleanup" - {
      "must remove package shipping mark when no selected" in {
        forAll(nonEmptyString) {
          value =>
            val userAnswers = emptyUserAnswers
              .setValue(PackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), true)
              .setValue(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), value)

            val result = userAnswers.setValue(PackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), false)

            result.get(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex)) must not be defined

        }
      }

      "must keep package shipping markwhen yes selected" in {
        forAll(nonEmptyString) {
          value =>
            val userAnswers = emptyUserAnswers
              .setValue(PackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), true)
              .setValue(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), value)

            val result = userAnswers.setValue(PackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), true)

            result.get(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)) mustBe defined
        }
      }
    }
  }
}
