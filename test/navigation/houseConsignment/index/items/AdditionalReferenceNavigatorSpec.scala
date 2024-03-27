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
import models.reference.AdditionalReferenceType
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.additionalReference._
import controllers.houseConsignment.index.items.additionalReference.routes

class AdditionalReferenceNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new AdditionalReferenceNavigator

  "AdditionalReferenceNavigator" - {

    "in Checkmode" - {

      val mode = CheckMode

      "must go from AdditionalReferenceTypePage to UnloadingFindingsPage page" in {

        val userAnswers =
          emptyUserAnswers.setValue(AdditionalReferenceTypePage(additionalReferenceIndex, hcIndex, itemIndex), AdditionalReferenceType("test", "test"))

        navigator
          .nextPage(AdditionalReferenceTypePage(additionalReferenceIndex, hcIndex, itemIndex), mode, userAnswers)
          .mustBe(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
      }

      "must go from AdditionalReferenceNumberPage to UnloadingFindingsPage page" in {

        val userAnswers = emptyUserAnswers.setValue(AdditionalReferenceNumberPage(additionalReferenceIndex, hcIndex, itemIndex), "test")

        navigator
          .nextPage(AdditionalReferenceNumberPage(additionalReferenceIndex, hcIndex, itemIndex), mode, userAnswers)
          .mustBe(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
      }

    }

    "in NormalMode" - {

      val mode = NormalMode

      "must go from AdditionalReferenceTypePage to AdditionalReferenceNumberYesNoPage page" in {

        val userAnswers =
          emptyUserAnswers.setValue(AdditionalReferenceTypePage(additionalReferenceIndex, hcIndex, itemIndex), AdditionalReferenceType("test", "test"))

        navigator
          .nextPage(AdditionalReferenceTypePage(additionalReferenceIndex, houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(routes.AddAdditionalReferenceNumberYesNoController.onPageLoad(arrivalId, mode, additionalReferenceIndex, hcIndex, itemIndex))
      }

      "must go from AdditionalReferenceNumberYesNoPage to AdditionalReferenceNumberPage page" in {

        val userAnswers = emptyUserAnswers.setValue(AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex, hcIndex, itemIndex), true)

        navigator
          .nextPage(AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex, hcIndex, itemIndex), mode, userAnswers)
          .mustBe(routes.AdditionalReferenceNumberController.onPageLoad(arrivalId, mode, additionalReferenceIndex, hcIndex, itemIndex))
      }

      "must go from AdditionalReferenceNumberYesNoPage to AddAnotherAdditionalReference page when Yes is selected" in {

        val userAnswers = emptyUserAnswers.setValue(AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex, hcIndex, itemIndex), true)

        navigator
          .nextPage(AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex, hcIndex, itemIndex), mode, userAnswers)
          .mustBe(routes.AdditionalReferenceNumberController.onPageLoad(arrivalId, NormalMode, additionalReferenceIndex, hcIndex, itemIndex))
      }

      "must go from AdditionalReferenceNumberYesNoPage to AddAnotherAdditionalReference page when No is selected" in {

        val userAnswers = emptyUserAnswers.setValue(AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex, hcIndex, itemIndex), false)

        navigator
          .nextPage(AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex, houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(routes.AddAnotherAdditionalReferenceController.onPageLoad(arrivalId, mode, houseConsignmentIndex, itemIndex))
      }

    }
  }
}
