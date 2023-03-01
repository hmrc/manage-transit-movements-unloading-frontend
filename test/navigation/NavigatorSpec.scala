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
import generators.Generators
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import queries.SealsQuery

class NavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      val mode = NormalMode

      "must go from a page that doesn't exist in the route map to unloading summary" in {

        case object UnknownPage extends Page

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(UnknownPage, mode, answers)
              .mustBe(routes.UnloadingSummaryController.onPageLoad(arrivalId))
        }
      }

      "must go from date goods unloaded page" - {
        "to can seals be read page when seals exist" in {
          forAll(arbitrary[UserAnswers], arbitrary[Seal]) {
            (answers, seal) =>
              val updatedUserAnswers = answers.setValue(SealsQuery, Seq(seal))
              navigator
                .nextPage(DateGoodsUnloadedPage, mode, updatedUserAnswers)
                .mustBe(routes.CanSealsBeReadController.onPageLoad(updatedUserAnswers.id, mode))
          }
        }

        "to unloading summary page when no seals exist" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.removeValue(SealsQuery)
              navigator
                .nextPage(DateGoodsUnloadedPage, mode, updatedUserAnswers)
                .mustBe(routes.UnloadingSummaryController.onPageLoad(updatedUserAnswers.id))
          }
        }
      }

      "must go from can seals be read page" - {
        "to Are any seals broken page when answer is Yes" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.setValue(CanSealsBeReadPage, true)
              navigator
                .nextPage(CanSealsBeReadPage, mode, updatedUserAnswers)
                .mustBe(routes.AreAnySealsBrokenController.onPageLoad(updatedUserAnswers.id, mode))
          }
        }

        "to Are any seals broken page  when the answer is No" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.setValue(CanSealsBeReadPage, false)
              navigator
                .nextPage(CanSealsBeReadPage, mode, updatedUserAnswers)
                .mustBe(routes.AreAnySealsBrokenController.onPageLoad(updatedUserAnswers.id, mode))
          }
        }

        "to session expired page when the answer is empty" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.removeValue(CanSealsBeReadPage)
              navigator
                .nextPage(CanSealsBeReadPage, mode, updatedUserAnswers)
                .mustBe(routes.SessionExpiredController.onPageLoad())
          }
        }
      }

      "must go from are any seals broken page " - {
        "to unloading summary page when the answer is No" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.setValue(AreAnySealsBrokenPage, false)

              navigator
                .nextPage(AreAnySealsBrokenPage, mode, updatedUserAnswers)
                .mustBe(routes.UnloadingSummaryController.onPageLoad(updatedUserAnswers.id))
          }
        }

        "to unloading summary page when the answer is Yes" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.setValue(AreAnySealsBrokenPage, true)

              navigator
                .nextPage(AreAnySealsBrokenPage, mode, updatedUserAnswers)
                .mustBe(routes.UnloadingSummaryController.onPageLoad(updatedUserAnswers.id))
          }
        }

        "to session expired page when the answer is empty" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.removeValue(AreAnySealsBrokenPage)

              navigator
                .nextPage(AreAnySealsBrokenPage, mode, updatedUserAnswers)
                .mustBe(routes.SessionExpiredController.onPageLoad())
          }
        }
      }

      "must go from New Seal Number page to unloading summary page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(SealPage(Index(0)), mode, answers)
              .mustBe(routes.UnloadingSummaryController.onPageLoad(answers.id))
        }
      }

      "from changes to report page to unloading summary page" - {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(UnloadingCommentsPage, mode, answers)
              .mustBe(routes.UnloadingSummaryController.onPageLoad(answers.id))

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
                .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.id))
          }
        }

        "must go from date goods unloaded page to check your answers page" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(DateGoodsUnloadedPage, mode, answers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.id))
          }
        }

        "must go from Vehicle Name Registration Reference page to check your answers page" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(VehicleNameRegistrationReferencePage, mode, answers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.id))
          }
        }

        "must go from Vehicle Registration Country page to check your answers page" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(VehicleNameRegistrationReferencePage, mode, answers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.id))
          }
        }

        "must go from Gross mass amount page to check your answers page" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(GrossMassAmountPage, mode, answers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.id))
          }
        }

        "must go from New Seal Number page to check your answers page" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(SealPage(Index(0)), mode, answers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.id))
          }
        }

        "must go from Remove comments page " - {
          "to check your answers page when the form is submitted" in {

            forAll(arbitrary[UserAnswers]) {
              answers =>
                navigator
                  .nextPage(ConfirmRemoveCommentsPage, mode, answers)
                  .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.id))
            }
          }
        }
      }
    }
  }
}
