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
import models.{CheckMode, Index, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

class UnloadingSummaryHelperSpec extends SpecBase {

  // numbers over 1000 will be comma-separated when passed in to messages, so will be ignored for the purposes of our tests
  // 998 is used because .display adds 1 to the index position
  private val arbitraryInt = Gen.choose(0, 998)

  "must return summary list row" - {

    "when .seals" in {

      forAll(arbitrary[String], arbitraryInt) {
        (str, position) =>
          val userAnswers = emptyUserAnswers
          val index       = Index(position)
          val helper      = new UnloadingSummaryHelper(userAnswers)
          val result      = helper.seals(index, str)

          result mustEqual SummaryListRow(
            key = s"Official customs seal ${index.display}".toKey,
            value = Value(str.toText),
            actions = Some(
              Actions(items =
                List(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.NewSealNumberController.onPageLoad(userAnswers.id, index, CheckMode).url,
                    visuallyHiddenText = Some(s"official customs seal ${index.display} $str"),
                    attributes = Map("id" -> s"change-seal-$position")
                  )
                )
              )
            )
          )
      }
    }

    "when .sealsWithRemove" in {

      forAll(arbitrary[String], arbitraryInt) {
        (str, position) =>
          val userAnswers = emptyUserAnswers
          val index       = Index(position)
          val helper      = new UnloadingSummaryHelper(userAnswers)
          val result      = helper.sealsWithRemove(index, str)

          result mustEqual SummaryListRow(
            key = s"Official customs seal ${index.display}".toKey,
            value = Value(str.toText),
            actions = Some(
              Actions(items =
                List(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.NewSealNumberController.onPageLoad(userAnswers.id, index, CheckMode).url,
                    visuallyHiddenText = Some(s"official customs seal ${index.display} $str"),
                    attributes = Map("id" -> s"change-seal-$position")
                  ),
                  ActionItem(
                    content = "Remove".toText,
                    href = routes.ConfirmRemoveSealController.onPageLoad(userAnswers.id, index, CheckMode).url,
                    visuallyHiddenText = Some(s"official customs seal ${index.display} $str"),
                    attributes = Map("id" -> s"remove-seal-$position")
                  )
                )
              )
            )
          )
      }
    }

    "when .items" in {

      forAll(arbitrary[String], arbitraryInt) {
        (str, position) =>
          val userAnswers = emptyUserAnswers
          val index       = Index(position)
          val helper      = new UnloadingSummaryHelper(userAnswers)
          val result      = helper.items(index, str)

          result mustEqual SummaryListRow(
            key = s"Item ${index.display}".toKey,
            value = Value(str.toText),
            actions = None
          )
      }
    }

    "when .vehicleUsed" in {

      forAll(arbitrary[String]) {
        str =>
          val userAnswers = emptyUserAnswers
          val helper      = new UnloadingSummaryHelper(userAnswers)
          val result      = helper.vehicleUsed(str)

          result mustEqual SummaryListRow(
            key = "Name, registration or reference".toKey,
            value = Value(str.toText),
            actions = Some(
              Actions(items =
                List(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.VehicleNameRegistrationReferenceController.onPageLoad(userAnswers.id, CheckMode).url,
                    visuallyHiddenText = Some("name, registration or reference"),
                    attributes = Map("id" -> "change-vehicle-reference")
                  )
                )
              )
            )
          )
      }
    }

    "when .registeredCountry" in {

      forAll(arbitrary[String]) {
        str =>
          val userAnswers = emptyUserAnswers
          val helper      = new UnloadingSummaryHelper(userAnswers)
          val result      = helper.registeredCountry(str)

          result mustEqual SummaryListRow(
            key = "Registered".toKey,
            value = Value(str.toText),
            actions = Some(
              Actions(items =
                List(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.VehicleRegistrationCountryController.onPageLoad(userAnswers.id, CheckMode).url,
                    visuallyHiddenText = Some("registered"),
                    attributes = Map("id" -> "change-vehicle-country")
                  )
                )
              )
            )
          )
      }
    }

    "when .grossMass" in {

      forAll(arbitrary[String]) {
        str =>
          val userAnswers = emptyUserAnswers
          val helper      = new UnloadingSummaryHelper(userAnswers)
          val result      = helper.grossMass(str)

          result mustEqual SummaryListRow(
            key = "Total gross mass in kilograms".toKey,
            value = Value(str.toText),
            actions = Some(
              Actions(items =
                List(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.GrossMassAmountController.onPageLoad(userAnswers.id, CheckMode).url,
                    visuallyHiddenText = Some("total gross mass in kilograms"),
                    attributes = Map("id" -> "change-gross-mass")
                  )
                )
              )
            )
          )
      }
    }

    "when .totalNumberOfItems" in {

      forAll(arbitrary[Int]) {
        int =>
          val userAnswers = emptyUserAnswers
          val helper      = new UnloadingSummaryHelper(userAnswers)
          val result      = helper.totalNumberOfItems(int)

          result mustEqual SummaryListRow(
            key = "Total number of items".toKey,
            value = Value(int.toString.toText),
            actions = Some(
              Actions(items =
                List(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.TotalNumberOfItemsController.onPageLoad(userAnswers.id, CheckMode).url,
                    visuallyHiddenText = Some("total number of items"),
                    attributes = Map("id" -> "change-total-number-of-items")
                  )
                )
              )
            )
          )
      }
    }

    "when .totalNumberOfPackages" in {

      forAll(arbitrary[Int]) {
        int =>
          val userAnswers = emptyUserAnswers
          val helper      = new UnloadingSummaryHelper(userAnswers)
          val result      = helper.totalNumberOfPackages(int)

          result mustEqual SummaryListRow(
            key = "Total number of packages".toKey,
            value = Value(int.toString.toText),
            actions = Some(
              Actions(items =
                List(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.TotalNumberOfPackagesController.onPageLoad(userAnswers.id, CheckMode).url,
                    visuallyHiddenText = Some("total number of packages"),
                    attributes = Map("id" -> "change-total-number-of-packages")
                  )
                )
              )
            )
          )
      }
    }

    "when .comments" in {

      forAll(arbitrary[String]) {
        str =>
          val userAnswers = emptyUserAnswers
          val helper      = new UnloadingSummaryHelper(userAnswers)
          val result      = helper.comments(str)

          result mustEqual SummaryListRow(
            key = "Comments".toKey,
            value = Value(str.toText),
            actions = Some(
              Actions(items =
                List(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.ChangesToReportController.onPageLoad(userAnswers.id, NormalMode).url,
                    visuallyHiddenText = Some("comments"),
                    attributes = Map("id" -> "change-comments")
                  ),
                  ActionItem(
                    content = "Remove".toText,
                    href = routes.ConfirmRemoveCommentsController.onPageLoad(userAnswers.id, NormalMode).url,
                    visuallyHiddenText = Some("comments"),
                    attributes = Map("id" -> "remove-comments")
                  )
                )
              )
            )
          )
      }
    }

  }
}
