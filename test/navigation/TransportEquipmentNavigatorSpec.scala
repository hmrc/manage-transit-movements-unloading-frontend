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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.ContainerIdentificationNumberPage
import pages.transportEquipment.index._

class TransportEquipmentNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new TransportEquipmentNavigator

  "TransportNavigator" - {

    "in NormalMode" - {

      val mode = NormalMode

      "must go from AddContainerIdentificationNumberYesNoPage" - {

        "to AddContainerIdentificationNumberYesNoPage when answered Yes" in {
          val userAnswers = emptyUserAnswers.setValue(AddContainerIdentificationNumberYesNoPage(equipmentIndex), true)

          navigator
            .nextPage(AddContainerIdentificationNumberYesNoPage(equipmentIndex), mode, userAnswers)
            .mustBe(controllers.transportEquipment.index.routes.ContainerIdentificationNumberController.onPageLoad(arrivalId, equipmentIndex, mode))

        }

        "to AddSealsYesNoPage when answered No" in {
          val userAnswers = emptyUserAnswers.setValue(AddContainerIdentificationNumberYesNoPage(equipmentIndex), false)

          navigator
            .nextPage(AddContainerIdentificationNumberYesNoPage(equipmentIndex), mode, userAnswers)
            .mustBe(controllers.transportEquipment.index.routes.AddSealYesNoController.onPageLoad(arrivalId, equipmentIndex, mode))

        }
      }

      "must go from ContainerIdentificationNumberPage to AddSealsYesNoPage" in {
        val userAnswers = emptyUserAnswers.setValue(ContainerIdentificationNumberPage(equipmentIndex), "123")

        navigator
          .nextPage(ContainerIdentificationNumberPage(equipmentIndex), mode, userAnswers)
          .mustBe(controllers.transportEquipment.index.routes.AddSealYesNoController.onPageLoad(arrivalId, equipmentIndex, mode))

      }

      "must go from AddSealYesNoPage" - {

        "to SealsIdentificationNumberPage when answered Yes" in {
          val userAnswers = emptyUserAnswers.setValue(AddSealYesNoPage(equipmentIndex), true)

          navigator
            .nextPage(AddSealYesNoPage(equipmentIndex), mode, userAnswers)
            .mustBe(
              controllers.transportEquipment.index.seals.routes.SealIdentificationNumberController
                .onPageLoad(arrivalId, mode, NormalMode, equipmentIndex, sealIndex)
            )

        }

        "to Apply Item Page when answered No" in {

          val userAnswers = emptyUserAnswers.setValue(AddSealYesNoPage(equipmentIndex), false)
          navigator
            .nextPage(AddSealYesNoPage(equipmentIndex), mode, userAnswers)
            .mustBe(controllers.transportEquipment.index.routes.ApplyAnItemYesNoController.onPageLoad(arrivalId, equipmentIndex, mode))
        }
      }

      "must go from ItemPage to ApplyAnotherItemPage" in {
        val userAnswers = emptyUserAnswers.setValue(ItemPage(equipmentIndex, itemIndex), BigInt(0))

        navigator
          .nextPage(ItemPage(equipmentIndex, itemIndex), mode, userAnswers)
          .mustBe(controllers.transportEquipment.index.routes.ApplyAnotherItemController.onPageLoad(arrivalId, NormalMode, equipmentIndex))

      }

      "must go from ApplyAnItemYesNoPage" - {

        "to ItemPage when answered Yes" in {
          val userAnswers = emptyUserAnswers.setValue(ApplyAnItemYesNoPage(equipmentIndex), true)

          navigator
            .nextPage(ApplyAnItemYesNoPage(equipmentIndex), mode, userAnswers)
            .mustBe(controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(arrivalId, equipmentIndex, itemIndex, mode))

        }

        "to AddAnotherEquipment page  when answered No" in {
          val userAnswers = emptyUserAnswers.setValue(ApplyAnItemYesNoPage(equipmentIndex), false)

          navigator
            .nextPage(ApplyAnItemYesNoPage(equipmentIndex), mode, userAnswers)
            .mustBe(controllers.transportEquipment.routes.AddAnotherEquipmentController.onPageLoad(arrivalId, mode))

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

      "must go from Item page to ApplyAnotherItem page" in {
        forAll(arbitrary[BigInt]) {
          item =>
            val userAnswers = emptyUserAnswers.setValue(ItemPage(equipmentIndex, itemIndex), item)

            navigator
              .nextPage(ItemPage(equipmentIndex, itemIndex), mode, userAnswers)
              .mustBe(controllers.transportEquipment.index.routes.ApplyAnotherItemController.onPageLoad(arrivalId, mode, equipmentIndex))
        }
      }

      "must go from ApplyAnItemYesNoPage" - {

        "to ItemPage when answered Yes" in {
          val userAnswers = emptyUserAnswers.setValue(ApplyAnItemYesNoPage(equipmentIndex), true)

          navigator
            .nextPage(ApplyAnItemYesNoPage(equipmentIndex), mode, userAnswers)
            .mustBe(controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(arrivalId, equipmentIndex, itemIndex, mode))

        }

        "to AddAnotherEquipment page  when answered No" in {
          val userAnswers = emptyUserAnswers.setValue(ApplyAnItemYesNoPage(equipmentIndex), false)

          navigator
            .nextPage(ApplyAnItemYesNoPage(equipmentIndex), mode, userAnswers)
            .mustBe(controllers.transportEquipment.routes.AddAnotherEquipmentController.onPageLoad(arrivalId, mode))

        }
      }
    }
  }
}
