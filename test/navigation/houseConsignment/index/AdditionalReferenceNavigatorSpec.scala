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

package navigation.houseConsignment.index

import base.SpecBase
import generators.Generators
import models._
import models.reference.AdditionalReferenceType
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.additionalReference.{
  AddHouseConsignmentAdditionalReferenceNumberYesNoPage,
  HouseConsignmentAdditionalReferenceNumberPage,
  HouseConsignmentAdditionalReferenceTypePage
}

class AdditionalReferenceNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new AdditionalReferenceNavigator

  "AdditionalReferenceNavigator HC level" - {

    "in Checkmode" - {

      val mode = CheckMode

      "must go from AdditionalReferenceTypePage to HouseConsignmentController page" in {

        val userAnswers =
          emptyUserAnswers.setValue(HouseConsignmentAdditionalReferenceTypePage(hcIndex, additionalReferenceIndex), AdditionalReferenceType("test", "test"))

        navigator
          .nextPage(HouseConsignmentAdditionalReferenceTypePage(hcIndex, additionalReferenceIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex))
      }

      "must go from AdditionalReferenceNumberPage to HouseConsignmentController page" in {

        val userAnswers = emptyUserAnswers.setValue(HouseConsignmentAdditionalReferenceNumberPage(hcIndex, additionalReferenceIndex), "test")

        navigator
          .nextPage(HouseConsignmentAdditionalReferenceNumberPage(hcIndex, additionalReferenceIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex))
      }
    }

    "in NormalMode" - {

      val mode = NormalMode

      "must go from HouseConsignmentAdditionalReferenceTypePage to AddHouseConsignmentAdditionalReferenceNumberYesNoPage page" in {

        val userAnswers =
          emptyUserAnswers.setValue(HouseConsignmentAdditionalReferenceTypePage(hcIndex, additionalReferenceIndex), AdditionalReferenceType("test", "test"))

        navigator
          .nextPage(HouseConsignmentAdditionalReferenceTypePage(hcIndex, additionalReferenceIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.additionalReference.routes.AddAdditionalReferenceNumberYesNoController
              .onPageLoad(arrivalId, mode, hcIndex, additionalReferenceIndex)
          )
      }

      "must go from AddHouseConsignmentAdditionalReferenceNumberYesNoPage to HouseConsignmentAdditionalReferenceNumberPage page" in {

        val userAnswers = emptyUserAnswers.setValue(AddHouseConsignmentAdditionalReferenceNumberYesNoPage(hcIndex, additionalReferenceIndex), true)

        navigator
          .nextPage(AddHouseConsignmentAdditionalReferenceNumberYesNoPage(hcIndex, additionalReferenceIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.additionalReference.routes.AdditionalReferenceNumberController
              .onPageLoad(arrivalId, mode, hcIndex, additionalReferenceIndex)
          )
      }

      "must go from AdditionalReferenceNumberYesNoPage to AddAnotherAdditionalReference page when Yes is selected" in {

        val userAnswers = emptyUserAnswers.setValue(AddHouseConsignmentAdditionalReferenceNumberYesNoPage(hcIndex, additionalReferenceIndex), true)

        navigator
          .nextPage(AddHouseConsignmentAdditionalReferenceNumberYesNoPage(hcIndex, additionalReferenceIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.additionalReference.routes.AdditionalReferenceNumberController
              .onPageLoad(arrivalId, NormalMode, hcIndex, additionalReferenceIndex)
          )
      }

      "must go from AdditionalReferenceNumberYesNoPage to AddAnotherAdditionalReference page when No is selected" in {

        val userAnswers = emptyUserAnswers.setValue(AddHouseConsignmentAdditionalReferenceNumberYesNoPage(hcIndex, additionalReferenceIndex), false)

        navigator
          .nextPage(AddHouseConsignmentAdditionalReferenceNumberYesNoPage(hcIndex, additionalReferenceIndex), mode, userAnswers)
          .mustBe(controllers.houseConsignment.index.additionalReference.routes.AddAnotherAdditionalReferenceController.onPageLoad(arrivalId, mode, hcIndex))
      }

    }
  }
}
