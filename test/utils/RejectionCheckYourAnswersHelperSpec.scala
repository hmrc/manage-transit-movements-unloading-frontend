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

package utils

import base.SpecBase
import controllers.routes
import models.CheckMode
import org.scalacheck.Arbitrary.arbitrary
import pages._
import uk.gov.hmrc.viewmodels.MessageInterpolators
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}

import java.time.LocalDate

class RejectionCheckYourAnswersHelperSpec extends SpecBase {

  "when .vehicleNameRegistrationRejection" - {

    "must return None" - {
      "when VehicleNameRegistrationReferencePage is undefined" in {

        val userAnswers = emptyUserAnswers
        val helper      = new RejectionCheckYourAnswersHelper(userAnswers)
        val result      = helper.vehicleNameRegistrationRejection

        result mustBe None
      }
    }

    "must return Some(row)" - {
      "when VehicleNameRegistrationReferencePage is defined" in {

        forAll(arbitrary[String]) {
          str =>
            val userAnswers = emptyUserAnswers.set(VehicleNameRegistrationReferencePage, str).success.value
            val helper      = new RejectionCheckYourAnswersHelper(userAnswers)
            val result      = helper.vehicleNameRegistrationRejection

            result mustBe Some(
              Row(
                key = Key(msg"vehicleNameRegistrationReference.checkYourAnswersLabel"),
                value = Value(lit"$str"),
                actions = List(
                  Action(
                    content = msg"site.edit",
                    href = routes.VehicleNameRegistrationRejectionController.onPageLoad(userAnswers.id).url,
                    visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"vehicleNameRegistrationReference.checkYourAnswersLabel")),
                    attributes = Map("id" -> "change-vehicle-registration-rejection")
                  )
                )
              )
            )
        }
      }
    }
  }

  "when .dateGoodsUnloaded" - {

    "must return None" - {
      "when DateGoodsUnloadedPage is undefined" in {

        val userAnswers = emptyUserAnswers
        val helper      = new RejectionCheckYourAnswersHelper(userAnswers)
        val result      = helper.dateGoodsUnloaded

        result mustBe None
      }
    }

    "must return Some(row)" - {
      "when DateGoodsUnloadedPage is defined" in {

        val userAnswers = emptyUserAnswers.set(DateGoodsUnloadedPage, LocalDate.parse("2000-01-01")).success.value
        val helper      = new RejectionCheckYourAnswersHelper(userAnswers)
        val result      = helper.dateGoodsUnloaded

        result mustBe Some(
          Row(
            key = Key(msg"dateGoodsUnloaded.checkYourAnswersLabel"),
            value = Value(lit"1 January 2000"),
            actions = List(
              Action(
                content = msg"site.edit",
                href = routes.DateGoodsUnloadedRejectionController.onPageLoad(userAnswers.id).url,
                visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"dateGoodsUnloaded.checkYourAnswersLabel")),
                attributes = Map("id" -> "change-date-goods-unloaded")
              )
            )
          )
        )
      }
    }
  }

  "when .totalNumberOfPackages" - {

    "must return None" - {
      "when TotalNumberOfPackagesPage is undefined" in {

        val userAnswers = emptyUserAnswers
        val helper      = new RejectionCheckYourAnswersHelper(userAnswers)
        val result      = helper.totalNumberOfPackages

        result mustBe None
      }
    }

    "must return Some(row)" - {
      "when TotalNumberOfPackagesPage is defined" in {

        forAll(arbitrary[Int]) {
          int =>
            val userAnswers = emptyUserAnswers.set(TotalNumberOfPackagesPage, int).success.value
            val helper      = new RejectionCheckYourAnswersHelper(userAnswers)
            val result      = helper.totalNumberOfPackages

            result mustBe Some(
              Row(
                key = Key(msg"totalNumberOfPackages.checkYourAnswersLabel"),
                value = Value(lit"$int"),
                actions = List(
                  Action(
                    content = msg"site.edit",
                    href = routes.TotalNumberOfPackagesRejectionController.onPageLoad(userAnswers.id).url,
                    visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"totalNumberOfPackages.checkYourAnswersLabel"))
                  )
                )
              )
            )
        }
      }
    }
  }

  "when .totalNumberOfItems" - {

    "must return None" - {
      "when TotalNumberOfItemsPage is undefined" in {

        val userAnswers = emptyUserAnswers
        val helper      = new RejectionCheckYourAnswersHelper(userAnswers)
        val result      = helper.totalNumberOfItems

        result mustBe None
      }
    }

    "must return Some(row)" - {
      "when TotalNumberOfItemsPage is defined" in {

        forAll(arbitrary[Int]) {
          int =>
            val userAnswers = emptyUserAnswers.set(TotalNumberOfItemsPage, int).success.value
            val helper      = new RejectionCheckYourAnswersHelper(userAnswers)
            val result      = helper.totalNumberOfItems

            result mustBe Some(
              Row(
                key = Key(msg"totalNumberOfItems.checkYourAnswersLabel"),
                value = Value(lit"$int"),
                actions = List(
                  Action(
                    content = msg"site.edit",
                    href = routes.TotalNumberOfItemsController.onPageLoad(userAnswers.id, CheckMode).url,
                    visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"totalNumberOfItems.checkYourAnswersLabel"))
                  )
                )
              )
            )
        }
      }
    }
  }

  "when .grossMassAmount" - {

    "must return None" - {
      "when GrossMassAmountPage is undefined" in {

        val userAnswers = emptyUserAnswers
        val helper      = new RejectionCheckYourAnswersHelper(userAnswers)
        val result      = helper.grossMassAmount

        result mustBe None
      }
    }

    "must return Some(row)" - {
      "when GrossMassAmountPage is defined" in {

        forAll(arbitrary[String]) {
          str =>
            val userAnswers = emptyUserAnswers.set(GrossMassAmountPage, str).success.value
            val helper      = new RejectionCheckYourAnswersHelper(userAnswers)
            val result      = helper.grossMassAmount

            result mustBe Some(
              Row(
                key = Key(msg"grossMassAmount.checkYourAnswersLabel"),
                value = Value(lit"$str"),
                actions = List(
                  Action(
                    content = msg"site.edit",
                    href = routes.GrossMassAmountRejectionController.onPageLoad(userAnswers.id).url,
                    visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"grossMassAmount.checkYourAnswersLabel"))
                  )
                )
              )
            )
        }
      }
    }
  }

}
