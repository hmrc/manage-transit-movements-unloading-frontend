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
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

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
              SummaryListRow(
                key = "What is the name, registration or reference of the new vehicle?".toKey,
                value = Value(str.toText),
                actions = Some(
                  Actions(items =
                    List(
                      ActionItem(
                        content = "Change".toText,
                        href = routes.VehicleNameRegistrationRejectionController.onPageLoad(userAnswers.id).url,
                        visuallyHiddenText = Some("the name, registration or reference of the new vehicle"),
                        attributes = Map("id" -> "change-vehicle-registration-rejection")
                      )
                    )
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
          SummaryListRow(
            key = "When were the goods unloaded?".toKey,
            value = Value("1 January 2000".toText),
            actions = Some(
              Actions(items =
                List(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.DateGoodsUnloadedRejectionController.onPageLoad(userAnswers.id).url,
                    visuallyHiddenText = Some("the date when the goods were unloaded"),
                    attributes = Map("id" -> "change-date-goods-unloaded")
                  )
                )
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
              SummaryListRow(
                key = "What is the new total number of packages?".toKey,
                value = Value(int.toString.toText),
                actions = Some(
                  Actions(items =
                    List(
                      ActionItem(
                        content = "Change".toText,
                        href = routes.TotalNumberOfPackagesRejectionController.onPageLoad(userAnswers.id).url,
                        visuallyHiddenText = Some("the new total number of packages"),
                        attributes = Map("id" -> "change-total-number-of-packages")
                      )
                    )
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
              SummaryListRow(
                key = "What is the new total number of items?".toKey,
                value = Value(int.toString.toText),
                actions = Some(
                  Actions(items =
                    List(
                      ActionItem(
                        content = "Change".toText,
                        href = routes.TotalNumberOfItemsController.onPageLoad(userAnswers.id, CheckMode).url,
                        visuallyHiddenText = Some("the new total number of items"),
                        attributes = Map("id" -> "change-total-number-of-items")
                      )
                    )
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
              SummaryListRow(
                key = "What is the new total gross mass in kilograms?".toKey,
                value = Value(str.toText),
                actions = Some(
                  Actions(items =
                    List(
                      ActionItem(
                        content = "Change".toText,
                        href = routes.GrossMassAmountRejectionController.onPageLoad(userAnswers.id).url,
                        visuallyHiddenText = Some("the new total gross mass in kilograms"),
                        attributes = Map("id" -> "change-gross-mass-amount")
                      )
                    )
                  )
                )
              )
            )
        }
      }
    }
  }

}
