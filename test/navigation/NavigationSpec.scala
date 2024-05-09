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

class NavigationSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new Navigation

  "Navigator" - {

    "in Normal mode" - {

      val mode = NormalMode
      "must go from Unloading type page to unloading date page" in {

        val userAnswers = emptyUserAnswers.setValue(UnloadingTypePage, UnloadingType.Fully)
        navigator
          .nextPage(UnloadingTypePage, mode, userAnswers)
          .mustBe(routes.DateGoodsUnloadedController.onPageLoad(userAnswers.id, NormalMode))
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
            .mustBe(routes.CanSealsBeReadController.onPageLoad(userAnswers.id, mode))
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
            .mustBe(routes.CanSealsBeReadController.onPageLoad(userAnswers.id, mode))
        }

        "to add transit unloading permission discrepancies yes/no page when no seals exist" in {
          val ie043 = arbitrary[CC043CType].map(_.copy(Consignment = None)).sample.value

          val userAnswers = emptyUserAnswers.copy(ie043Data = ie043)

          navigator
            .nextPage(DateGoodsUnloadedPage, mode, userAnswers)
            .mustBe(routes.AddTransitUnloadingPermissionDiscrepanciesYesNoController.onPageLoad(arrivalId, mode))
        }
      }

      "must go from can seals be read page" - {
        "to Are any seals broken page when answer is Yes" in {

          val userAnswers = emptyUserAnswers.setValue(CanSealsBeReadPage, true)
          navigator
            .nextPage(CanSealsBeReadPage, mode, userAnswers)
            .mustBe(routes.AreAnySealsBrokenController.onPageLoad(userAnswers.id, mode))
        }

        "to Are any seals broken page when the answer is No" in {

          val userAnswers = emptyUserAnswers.setValue(CanSealsBeReadPage, false)
          navigator
            .nextPage(CanSealsBeReadPage, mode, userAnswers)
            .mustBe(routes.AreAnySealsBrokenController.onPageLoad(userAnswers.id, mode))
        }
      }

      "must go from are any seals broken page " - {
        "to add transit unloading permission discrepancies yes/no page when seals are present and not damaged" in {

          val userAnswers = emptyUserAnswers
            .setValue(CanSealsBeReadPage, true)
            .setValue(AreAnySealsBrokenPage, false)

          navigator
            .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
            .mustBe(routes.AddTransitUnloadingPermissionDiscrepanciesYesNoController.onPageLoad(arrivalId, mode))
        }

        "to unloading findings page when seals are not present" in {

          navigator
            .nextPage(AreAnySealsBrokenPage, mode, emptyUserAnswers)
            .mustBe(routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }

        "to unloading findings page when seals are present but not readable" in {

          val userAnswers = emptyUserAnswers
            .setValue(CanSealsBeReadPage, false)
            .setValue(AreAnySealsBrokenPage, false)

          navigator
            .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
            .mustBe(routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }

        "to unloading findings page when seals are present but broken" in {

          val userAnswers = emptyUserAnswers
            .setValue(CanSealsBeReadPage, true)
            .setValue(AreAnySealsBrokenPage, true)

          navigator
            .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
            .mustBe(routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }
      }

      "must go from add transit unloading permission discrepancies yes/no page page" - {
        "when answer is true to unloading comments controller" in {
          val userAnswers = emptyUserAnswers.setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

          navigator
            .nextPage(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, mode, userAnswers)
            .mustBe(routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }

        "when answer is false to do you have anything else to report yes/no page" in {
          val userAnswers = emptyUserAnswers.setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)

          navigator
            .nextPage(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, mode, userAnswers)
            .mustBe(routes.DoYouHaveAnythingElseToReportYesNoController.onPageLoad(arrivalId, mode))
        }

        "to session expired controller when no existing answers found" in {
          navigator
            .nextPage(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, mode, emptyUserAnswers)
            .mustBe(routes.SessionExpiredController.onPageLoad())
        }
      }

      "must go from add comments yes/no page" - {
        "when answer is true to unloading comments controller" in {
          val userAnswers = emptyUserAnswers.setValue(AddCommentsYesNoPage, true)

          navigator
            .nextPage(AddCommentsYesNoPage, mode, userAnswers)
            .mustBe(routes.UnloadingCommentsController.onPageLoad(arrivalId, mode))
        }

        "when answer is false to do you have anything else to report yes/no page" in {
          val userAnswers = emptyUserAnswers.setValue(AddCommentsYesNoPage, false)

          navigator
            .nextPage(AddCommentsYesNoPage, mode, userAnswers)
            .mustBe(routes.DoYouHaveAnythingElseToReportYesNoController.onPageLoad(arrivalId, mode))
        }
      }

      "must go from can unloading comments page to do you have anything else to report yes/no page" in {
        val userAnswers = emptyUserAnswers.setValue(UnloadingCommentsPage, "test")
        navigator
          .nextPage(UnloadingCommentsPage, mode, userAnswers)
          .mustBe(routes.DoYouHaveAnythingElseToReportYesNoController.onPageLoad(arrivalId, mode))
      }

      "must go from do you have anything else to report page" - {
        "when answer is true to other things to report controller" in {
          val userAnswers = emptyUserAnswers.setValue(DoYouHaveAnythingElseToReportYesNoPage, true)

          navigator
            .nextPage(DoYouHaveAnythingElseToReportYesNoPage, mode, userAnswers)
            .mustBe(routes.OtherThingsToReportController.onPageLoad(arrivalId, mode))
        }

        "when answer is false to check your answers page" in {
          val userAnswers = emptyUserAnswers.setValue(DoYouHaveAnythingElseToReportYesNoPage, false)

          navigator
            .nextPage(DoYouHaveAnythingElseToReportYesNoPage, mode, userAnswers)
            .mustBe(routes.CheckYourAnswersController.onPageLoad(arrivalId))
        }
      }

      "must go from other things to report page to check your answers page" in {
        val userAnswers = emptyUserAnswers.setValue(OtherThingsToReportPage, "test")
        navigator
          .nextPage(OtherThingsToReportPage, mode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad(arrivalId))

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

      "must go from unloading type page to check your answers" in {
        val userAnswers = emptyUserAnswers.setValue(UnloadingTypePage, UnloadingType.Fully)

        navigator
          .nextPage(UnloadingTypePage, mode, userAnswers)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
      }

      "must go from date goods unloaded page to check your answers" in {
        val userAnswers = emptyUserAnswers.setValue(DateGoodsUnloadedPage, arbitraryLocalDate.arbitrary.sample.value)

        navigator
          .nextPage(DateGoodsUnloadedPage, mode, userAnswers)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
      }

      "must go from can seals be read page to are any seals broken page" - {
        val userAnswers = emptyUserAnswers.setValue(CanSealsBeReadPage, true)

        navigator
          .nextPage(CanSealsBeReadPage, mode, userAnswers)
          .mustBe(routes.AreAnySealsBrokenController.onPageLoad(userAnswers.id, CheckMode))
      }

      "must go from are any seals broken page" - {
        "to add transit unloading permission discrepancies yes/no page when state of seals = 1" in {
          val userAnswers = emptyUserAnswers
            .setValue(CanSealsBeReadPage, true)
            .setValue(AreAnySealsBrokenPage, false)

          navigator
            .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
            .mustBe(routes.AddTransitUnloadingPermissionDiscrepanciesYesNoController.onPageLoad(userAnswers.id, CheckMode))
        }

        "to unloading findings page when state of seals = 0" in {
          val userAnswers = emptyUserAnswers
            .setValue(AreAnySealsBrokenPage, true)

          navigator
            .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
            .mustBe(routes.UnloadingFindingsController.onPageLoad(userAnswers.id))
        }
      }

      "must go from add transit unloading permission discrepancies yes/no page to unloading findings page when true" in {
        val userAnswers = emptyUserAnswers.setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

        navigator
          .nextPage(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, mode, userAnswers)
          .mustBe(controllers.routes.UnloadingFindingsController.onPageLoad(userAnswers.id))
      }

      "must go from add transit unloading permission discrepancies yes/no page to check your answers page when false" in {
        val userAnswers = emptyUserAnswers.setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)

        navigator
          .nextPage(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, mode, userAnswers)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
      }

      "must go from add comments yes/no page to unloading comments page when true" in {
        val userAnswers = emptyUserAnswers.setValue(AddCommentsYesNoPage, true)

        navigator
          .nextPage(AddCommentsYesNoPage, mode, userAnswers)
          .mustBe(controllers.routes.UnloadingCommentsController.onPageLoad(userAnswers.id, mode))
      }

      "must go from add comments yes/no page to check your answers page when false" in {
        val userAnswers = emptyUserAnswers.setValue(AddCommentsYesNoPage, false)

        navigator
          .nextPage(AddCommentsYesNoPage, mode, userAnswers)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
      }

      "must go from unloading comments page to check your answers" in {
        val userAnswers = emptyUserAnswers.setValue(UnloadingCommentsPage, "comments")

        navigator
          .nextPage(UnloadingCommentsPage, mode, userAnswers)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
      }

      "must go from do you have anything else to report yes/no page to other things to report page when true" in {
        val userAnswers = emptyUserAnswers.setValue(DoYouHaveAnythingElseToReportYesNoPage, true)

        navigator
          .nextPage(DoYouHaveAnythingElseToReportYesNoPage, mode, userAnswers)
          .mustBe(controllers.routes.OtherThingsToReportController.onPageLoad(userAnswers.id, mode))
      }

      "must go from do you have anything else to report yes/no page to check your answers page when false" in {
        val userAnswers = emptyUserAnswers.setValue(DoYouHaveAnythingElseToReportYesNoPage, false)

        navigator
          .nextPage(DoYouHaveAnythingElseToReportYesNoPage, mode, userAnswers)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
      }

      "must go from other things to report page to check your answers" in {
        val userAnswers = emptyUserAnswers.setValue(OtherThingsToReportPage, "report")

        navigator
          .nextPage(OtherThingsToReportPage, mode, userAnswers)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
      }

      "must go from a page that doesn't exist in the edit route map  to Check Your Answers" in {

        case object UnknownPage extends Page

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(UnknownPage, mode, answers)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(arrivalId))
        }
      }
    }
    "in CheckMode" - {
      val mode = CheckMode
      "must go from gross weight page to Unloading findings page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(pages.GrossWeightPage, mode, answers)
              .mustBe(routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }
      }
    }
  }
}
