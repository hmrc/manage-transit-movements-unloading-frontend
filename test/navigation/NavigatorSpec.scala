/*
 * Copyright 2022 HM Revenue & Customs
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

class NavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(UnknownPage, NormalMode, answers)
              .mustBe(routes.IndexController.onPageLoad(arrivalId))
        }
      }

      "must go from can seals be read page" - {
        "to Are any seals broken page when answer is Yes" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.set(CanSealsBeReadPage, true).success.value
              navigator
                .nextPage(CanSealsBeReadPage, NormalMode, updatedUserAnswers)
                .mustBe(routes.AreAnySealsBrokenController.onPageLoad(updatedUserAnswers.id, NormalMode))
          }
        }

        "to Are any seals broken page  when the answer is No" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.set(CanSealsBeReadPage, false).success.value
              navigator
                .nextPage(CanSealsBeReadPage, NormalMode, updatedUserAnswers)
                .mustBe(routes.AreAnySealsBrokenController.onPageLoad(updatedUserAnswers.id, NormalMode))
          }
        }

        "to session expired page when the answer is empty" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.remove(CanSealsBeReadPage).success.value
              navigator
                .nextPage(CanSealsBeReadPage, NormalMode, updatedUserAnswers)
                .mustBe(routes.SessionExpiredController.onPageLoad())
          }
        }
      }

      "must go from are any seals broken page " - {
        "to unloading summary page when the answer is No" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.set(AreAnySealsBrokenPage, false).success.value

              navigator
                .nextPage(AreAnySealsBrokenPage, NormalMode, updatedUserAnswers)
                .mustBe(routes.UnloadingSummaryController.onPageLoad(updatedUserAnswers.id))
          }
        }

        "to unloading summary page when the answer is Yes" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.set(AreAnySealsBrokenPage, true).success.value

              navigator
                .nextPage(AreAnySealsBrokenPage, NormalMode, updatedUserAnswers)
                .mustBe(routes.UnloadingSummaryController.onPageLoad(updatedUserAnswers.id))
          }
        }

        "to session expired page when the answer is empty" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.remove(AreAnySealsBrokenPage).success.value

              navigator
                .nextPage(AreAnySealsBrokenPage, NormalMode, updatedUserAnswers)
                .mustBe(routes.SessionExpiredController.onPageLoad())
          }
        }
      }

      "must go from New Seal Number page to unloading summary page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(NewSealNumberPage(Index(0)), NormalMode, answers)
              .mustBe(routes.UnloadingSummaryController.onPageLoad(answers.id))
        }
      }

      "from changes to report page to unloading summary page" - {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ChangesToReportPage, NormalMode, answers)
              .mustBe(routes.UnloadingSummaryController.onPageLoad(answers.id))

        }
      }

      "in Check mode" - {

        "must go from a page that doesn't exist in the edit route map  to Check Your Answers" in {

          case object UnknownPage extends Page

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(UnknownPage, CheckMode, answers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.id))
          }
        }

        "must go from Vehicle Name Registration Reference page to unloading summary page" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(VehicleNameRegistrationReferencePage, CheckMode, answers)
                .mustBe(routes.UnloadingSummaryController.onPageLoad(answers.id))
          }
        }

        "must go from Vehicle Registration Country page to unloading summary page" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(VehicleNameRegistrationReferencePage, CheckMode, answers)
                .mustBe(routes.UnloadingSummaryController.onPageLoad(answers.id))
          }
        }

        "must go from Gross mass amount page to unloading summary page" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(GrossMassAmountPage, CheckMode, answers)
                .mustBe(routes.UnloadingSummaryController.onPageLoad(answers.id))
          }
        }

        "must go from New Seal Number page to unloading summary page" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(NewSealNumberPage(Index(0)), CheckMode, answers)
                .mustBe(routes.UnloadingSummaryController.onPageLoad(answers.id))
          }
        }

        "must go from Remove comments page " - {
          "to unloading summary page when the form is submitted" in {

            forAll(arbitrary[UserAnswers]) {
              answers =>
                navigator
                  .nextPage(ConfirmRemoveCommentsPage, NormalMode, answers)
                  .mustBe(routes.UnloadingSummaryController.onPageLoad(answers.id))
            }
          }
        }
      }
    }
  }
}
