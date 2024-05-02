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
import controllers.houseConsignment.index.items.additionalReference.routes
import generators.Generators
import models._
import models.reference.AdditionalReferenceType
import navigation.houseConsignment.index.items.AdditionalReferenceNavigator.AdditionalReferenceNavigatorProvider
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.additionalReference._

class AdditionalReferenceNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigatorProvider = new AdditionalReferenceNavigatorProvider

  "AdditionalReferenceNavigator" - {

    "in CheckMode" - {

      val houseConsignmentMode    = CheckMode
      val itemMode                = CheckMode
      val additionalReferenceMode = CheckMode
      val navigator               = navigatorProvider.apply(houseConsignmentMode, itemMode)

      "must go from AdditionalReferenceTypePage to HouseConsignmentController page" in {

        val userAnswers =
          emptyUserAnswers.setValue(AdditionalReferenceTypePage(additionalReferenceIndex, hcIndex, itemIndex), AdditionalReferenceType("test", "test"))

        navigator
          .nextPage(AdditionalReferenceTypePage(additionalReferenceIndex, hcIndex, itemIndex), additionalReferenceMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex))
      }

      "must go from AdditionalReferenceNumberPage to HouseConsignmentController page" in {

        val userAnswers = emptyUserAnswers.setValue(AdditionalReferenceNumberPage(additionalReferenceIndex, hcIndex, itemIndex), "test")

        navigator
          .nextPage(AdditionalReferenceNumberPage(additionalReferenceIndex, hcIndex, itemIndex), additionalReferenceMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex))
      }

    }

    "in NormalMode" - {

      val (houseConsignmentMode, itemMode) = arbitrary[(Mode, Mode)]
        .retryUntil {
          case (NormalMode, CheckMode) => false
          case _                       => true
        }
        .sample
        .value

      val additionalReferenceMode = NormalMode
      val navigator               = navigatorProvider.apply(houseConsignmentMode, itemMode)

      "must go from AdditionalReferenceTypePage to AdditionalReferenceNumberYesNoPage page" in {

        val userAnswers =
          emptyUserAnswers.setValue(AdditionalReferenceTypePage(additionalReferenceIndex, hcIndex, itemIndex), AdditionalReferenceType("test", "test"))

        navigator
          .nextPage(AdditionalReferenceTypePage(additionalReferenceIndex, houseConsignmentIndex, itemIndex), additionalReferenceMode, userAnswers)
          .mustBe(
            routes.AddAdditionalReferenceNumberYesNoController
              .onPageLoad(arrivalId, houseConsignmentMode, itemMode, additionalReferenceMode, additionalReferenceIndex, hcIndex, itemIndex)
          )
      }

      "must go from AdditionalReferenceNumberYesNoPage to AdditionalReferenceNumberPage page" in {

        val userAnswers = emptyUserAnswers.setValue(AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex, hcIndex, itemIndex), true)

        navigator
          .nextPage(AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex, hcIndex, itemIndex), additionalReferenceMode, userAnswers)
          .mustBe(
            routes.AdditionalReferenceNumberController
              .onPageLoad(arrivalId, houseConsignmentMode, itemMode, additionalReferenceMode, additionalReferenceIndex, hcIndex, itemIndex)
          )
      }

      "must go from AdditionalReferenceNumberYesNoPage to AddAnotherAdditionalReference page when Yes is selected" in {

        val userAnswers = emptyUserAnswers.setValue(AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex, hcIndex, itemIndex), true)

        navigator
          .nextPage(AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex, hcIndex, itemIndex), additionalReferenceMode, userAnswers)
          .mustBe(
            routes.AdditionalReferenceNumberController
              .onPageLoad(arrivalId, houseConsignmentMode, itemMode, additionalReferenceMode, additionalReferenceIndex, hcIndex, itemIndex)
          )
      }

      "must go from AdditionalReferenceNumberYesNoPage to AddAnotherAdditionalReference page when No is selected" in {

        val userAnswers = emptyUserAnswers.setValue(AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex, hcIndex, itemIndex), false)

        navigator
          .nextPage(AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex, houseConsignmentIndex, itemIndex), additionalReferenceMode, userAnswers)
          .mustBe(routes.AddAnotherAdditionalReferenceController.onPageLoad(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex))
      }

    }
  }
}
