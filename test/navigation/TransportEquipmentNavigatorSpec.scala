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

package navigation

import base.SpecBase
import generators.Generators
import models._
import models.reference.Item
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.ContainerIdentificationNumberPage
import pages.transportEquipment.index.{AddAnotherSealPage, ApplyAnotherItemPage}
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import pages.transportEquipment.index.{AddAnotherSealPage, ApplyAnotherItemPage, ItemPage}

class TransportEquipmentNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new TransportEquipmentNavigator

  "TransportNavigator" - {

    "must go from AddAnotherSealPage page " - {
      "to SealIdentificationNumberPage if answer is true" in {
        forAll(arbitrary[Mode]) {
          mode =>
            val page        = AddAnotherSealPage(equipmentIndex, sealIndex)
            val userAnswers = emptyUserAnswers.setValue(page, true)

            navigator
              .nextPage(page, mode, userAnswers)
              .mustBe(
                controllers.transportEquipment.index.seals.routes.SealIdentificationNumberController.onPageLoad(arrivalId, mode, equipmentIndex, sealIndex)
              )
        }
      }

      "to UnloadingFindings page if answer is false" in {
        forAll(arbitrary[Mode]) {
          mode =>
            val page        = AddAnotherSealPage(equipmentIndex, sealIndex)
            val userAnswers = emptyUserAnswers.setValue(page, false)

            navigator
              .nextPage(page, mode, userAnswers)
              .mustBe(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }
      }
    }

    "must go from ApplyAnotherItemPage page " - {
      "to GoodsReferencePage if answer is true" in {
        forAll(arbitrary[Mode]) {
          mode =>
            val page        = ApplyAnotherItemPage(equipmentIndex, itemIndex)
            val userAnswers = emptyUserAnswers.setValue(page, true)

            navigator
              .nextPage(page, mode, userAnswers)
              .mustBe(
                controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(arrivalId, equipmentIndex, itemIndex, mode)
              )
        }
      }

      "to UnloadingFindings page if answer is false" in {
        forAll(arbitrary[Mode]) {
          mode =>
            val page        = ApplyAnotherItemPage(equipmentIndex, itemIndex)
            val userAnswers = emptyUserAnswers.setValue(page, false)

            navigator
              .nextPage(page, mode, userAnswers)
              .mustBe(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }
      }
    }

    "in CheckMode" - {

      val mode = CheckMode

      "must go from ContainerIdentificationNumber page to UnloadingFindings page" in {

        val userAnswers = emptyUserAnswers.setValue(ContainerIdentificationNumberPage(equipmentIndex), "container1")

        navigator
          .nextPage(ContainerIdentificationNumberPage(equipmentIndex), mode, userAnswers)
          .mustBe(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
      }

      "must go from SealIdentificationNumber page to UnloadingFindings page" in {

        val userAnswers = emptyUserAnswers.setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), "seal1")

        navigator
          .nextPage(SealIdentificationNumberPage(equipmentIndex, sealIndex), mode, userAnswers)
          .mustBe(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
      }

      "must go from Item page to ApplyAnotherItem page" in {
        forAll(arbitrary[Item]) {
          item =>
            val userAnswers = emptyUserAnswers.setValue(ItemPage(equipmentIndex, itemIndex), item)

            navigator
              .nextPage(ItemPage(equipmentIndex, itemIndex), mode, userAnswers)
              .mustBe(controllers.transportEquipment.index.routes.ApplyAnotherItemController.onPageLoad(arrivalId, mode, equipmentIndex))
        }
      }

      "must go from ApplyAnotherItem page to Item page when answer is true" in {
        forAll(arbitrary[Item]) {
          item =>
            val userAnswers = emptyUserAnswers
              .setValue(ItemPage(equipmentIndex, itemIndex), item)
              .setValue(ApplyAnotherItemPage(equipmentIndex, itemIndex), true)

            navigator
              .nextPage(ApplyAnotherItemPage(equipmentIndex, itemIndex), mode, userAnswers)
              .mustBe(controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(arrivalId, equipmentIndex, itemIndex, mode))
        }
      }

      "must go from ApplyAnotherItem page to UnloadingFindings page when answer is false" in {
        forAll(arbitrary[Item]) {
          item =>
            val userAnswers = emptyUserAnswers
              .setValue(ItemPage(equipmentIndex, itemIndex), item)
              .setValue(ApplyAnotherItemPage(equipmentIndex, itemIndex), false)

            navigator
              .nextPage(ApplyAnotherItemPage(equipmentIndex, itemIndex), mode, userAnswers)
              .mustBe(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }
      }
    }
  }
}
