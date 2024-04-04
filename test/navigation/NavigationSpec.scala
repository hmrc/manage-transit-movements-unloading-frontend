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
import controllers.routes
import generated._
import generators.Generators
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import pages.houseConsignment.index.items.GrossWeightPage
import pages.transportEquipment.index.seals.SealIdentificationNumberPage

class NavigationSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new Navigation

  "Navigator" - {

    "in Normal mode" - {

      val mode = NormalMode
      "must go from Unloading type page to unloading date page" in {

        val userAnswers = emptyUserAnswers.setValue(UnloadingTypePage, UnloadingType.Fully)
        navigator
          .nextPage(UnloadingTypePage, mode, userAnswers)
          .mustBe(controllers.routes.DateGoodsUnloadedController.onPageLoad(userAnswers.id, NormalMode))
      }

      "must go from date goods unloaded page" - {
        "to can seals be read page when seals exist in transport equipment" in {
          val transportEquipment = arbitrary[TransportEquipmentType05].retryUntil(_.Seal.nonEmpty).sample.value

          val consignment = arbitrary[ConsignmentType05].sample.value.copy(
            TransportEquipment = Seq(transportEquipment),
            Incident = Nil
          )

          val ie043 = arbitrary[CC043CType].sample.value.copy(Consignment = Some(consignment))

          val userAnswers = emptyUserAnswers.copy(ie043Data = ie043)

          navigator
            .nextPage(DateGoodsUnloadedPage, mode, userAnswers)
            .mustBe(controllers.routes.CanSealsBeReadController.onPageLoad(userAnswers.id, mode))
        }

        "to can seals be read page when seals exist in incident transport equipment" in {
          val transportEquipment = arbitrary[TransportEquipmentType07].retryUntil(_.Seal.nonEmpty).sample.value

          val incident = arbitrary[IncidentType04].sample.value.copy(TransportEquipment = Seq(transportEquipment))

          val consignment = arbitrary[ConsignmentType05].sample.value.copy(
            TransportEquipment = Nil,
            Incident = Seq(incident)
          )

          val ie043 = arbitrary[CC043CType].sample.value.copy(Consignment = Some(consignment))

          val userAnswers = emptyUserAnswers.copy(ie043Data = ie043)

          navigator
            .nextPage(DateGoodsUnloadedPage, mode, userAnswers)
            .mustBe(controllers.routes.CanSealsBeReadController.onPageLoad(userAnswers.id, mode))
        }

        "to cross-check page when no seals exist" in {
          val ie043 = arbitrary[CC043CType].map(_.copy(Consignment = None)).sample.value

          val userAnswers = emptyUserAnswers.copy(ie043Data = ie043)

          navigator
            .nextPage(DateGoodsUnloadedPage, mode, userAnswers)
            .mustBe(routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }
      }

      "must go from can seals be read page" - {
        "to Are any seals broken page when answer is Yes" in {

          val userAnswers = emptyUserAnswers.setValue(CanSealsBeReadPage, true)
          navigator
            .nextPage(CanSealsBeReadPage, mode, userAnswers)
            .mustBe(controllers.routes.AreAnySealsBrokenController.onPageLoad(userAnswers.id, mode))
        }

        "to Are any seals broken page  when the answer is No" in {

          val userAnswers = emptyUserAnswers.setValue(CanSealsBeReadPage, false)
          navigator
            .nextPage(CanSealsBeReadPage, mode, userAnswers)
            .mustBe(controllers.routes.AreAnySealsBrokenController.onPageLoad(userAnswers.id, mode))
        }
      }

      "must go from are any seals broken page " - {
        "to add unloading comments yes/no page when the answer is No" in {

          val userAnswers = emptyUserAnswers.setValue(AreAnySealsBrokenPage, false)

          navigator
            .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
            .mustBe(routes.AddUnloadingCommentsYesNoController.onPageLoad(arrivalId, NormalMode))
        }

        "to add unloading comments yes/no page when the answer is Yes" in {

          val userAnswers = emptyUserAnswers.setValue(AreAnySealsBrokenPage, true)

          navigator
            .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
            .mustBe(routes.AddUnloadingCommentsYesNoController.onPageLoad(arrivalId, NormalMode))
        }
      }

      "must go from can unloading comment page to check your answers page" in {

        val userAnswers = emptyUserAnswers.setValue(UnloadingCommentsPage, "test")
        navigator
          .nextPage(UnloadingCommentsPage, mode, userAnswers)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
      }

      "must go from add unloading comments yes/no page" - {
        "when answer is true to unloading comments controller" in {
          val userAnswers = emptyUserAnswers.setValue(AddUnloadingCommentsYesNoPage, true)

          navigator
            .nextPage(AddUnloadingCommentsYesNoPage, mode, userAnswers)
            .mustBe(routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }

        "when answer is false to add unloading remarks yes/no page" in {
          val userAnswers = emptyUserAnswers.setValue(AddUnloadingCommentsYesNoPage, false)

          navigator
            .nextPage(AddUnloadingCommentsYesNoPage, mode, userAnswers)
            .mustBe(routes.CheckYourAnswersController.onPageLoad(arrivalId)) //TODO: Change to AddUnloadingRemarksYesNo
        }

        "to session expired controller when no existing answers found" in {
          navigator
            .nextPage(AddUnloadingCommentsYesNoPage, mode, emptyUserAnswers)
            .mustBe(routes.SessionExpiredController.onPageLoad())
        }
      }

      "must go from a page that doesn't exist in the route map to session expired" in {

        case object UnknownPage extends Page

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(UnknownPage, mode, answers)
              .mustBe(routes.SessionExpiredController.onPageLoad())
        }
      }
    }

    "in Check mode" - {

      val mode = CheckMode

      "must go from a page that doesn't exist in the edit route map  to Check Your Answers" in {

        case object UnknownPage extends Page

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(UnknownPage, mode, answers)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(arrivalId))
        }
      }

      "must go from unloading comments yes no page" - {
        "to check your answers page if no selected" in {

          val userAnswers = emptyUserAnswers
            .setValue(AddUnloadingCommentsYesNoPage, false)

          navigator
            .nextPage(AddUnloadingCommentsYesNoPage, mode, userAnswers)
            .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(arrivalId))
        }
        "to additional comments page if yes is selected and no comments found" in {

          val userAnswers = emptyUserAnswers
            .setValue(AddUnloadingCommentsYesNoPage, true)

          navigator
            .nextPage(AddUnloadingCommentsYesNoPage, mode, userAnswers)
            .mustBe(controllers.routes.UnloadingCommentsController.onPageLoad(arrivalId, mode))
        }
        "to additional comments page if yes is selected and comments found" in {

          val userAnswers = emptyUserAnswers
            .setValue(AddUnloadingCommentsYesNoPage, true)
            .setValue(UnloadingCommentsPage, "comment")

          navigator
            .nextPage(AddUnloadingCommentsYesNoPage, mode, userAnswers)
            .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(arrivalId))
        }
        "to session expired controller when no existing answers found" in {
          navigator
            .nextPage(AddUnloadingCommentsYesNoPage, mode, emptyUserAnswers)
            .mustBe(routes.SessionExpiredController.onPageLoad())
        }
      }

      "must go from date goods unloaded page to check your answers page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(DateGoodsUnloadedPage, mode, answers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(arrivalId))
        }
      }

      "must go from Gross mass amount page to check your answers page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(GrossWeightPage(index, itemIndex), mode, answers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(arrivalId))
        }
      }

      "must go from New Seal Number page to check your answers page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(SealIdentificationNumberPage(equipmentIndex, sealIndex), mode, answers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(arrivalId))
        }
      }

      "must go from Remove comments page " - {
        "to check your answers page when the form is submitted" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(ConfirmRemoveCommentsPage, mode, answers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(arrivalId))
          }
        }
      }
    }
  }
}
