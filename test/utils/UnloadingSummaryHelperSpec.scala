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
import generators.Generators
import models.reference.Country
import models.{Index, Mode, Seals, UnloadingPermission}
import org.scalacheck.Arbitrary.arbitrary
import pages._
import queries.{GoodsItemsQuery, SealsQuery}
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

class UnloadingSummaryHelperSpec extends SpecBase with Generators {

  private val mode = arbitrary[Mode].sample.value

  "must return summary list row" - {

    "when .seals" - {

      "when no seals" in {

        val userAnswers = emptyUserAnswers
        val helper      = new UnloadingSummaryHelper(userAnswers, mode)
        val result      = helper.seals

        result mustEqual Nil
      }

      "when there are existing seals" in {

        forAll(listWithMaxLength[String](Seals.maxSeals)) {
          strs =>
            val userAnswers = emptyUserAnswers
              .setPrepopulatedValue(SealsQuery, strs)
              .setValue(SealsQuery, strs)

            val helper = new UnloadingSummaryHelper(userAnswers, mode)
            val result = helper.seals

            result.size mustEqual strs.size

            val index = Index(0)
            val str   = strs.head
            result.head mustEqual SummaryListRow(
              key = s"Official customs seal ${index.display}".toKey,
              value = Value(str.toText),
              actions = Some(
                Actions(items =
                  List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.NewSealNumberController.onPageLoad(userAnswers.id, index, mode).url,
                      visuallyHiddenText = Some(s"official customs seal ${index.display} $str"),
                      attributes = Map("id" -> s"change-seal-${index.position}")
                    )
                  )
                )
              )
            )
        }
      }
    }

    "when .sealsWithRemove" - {

      "when no seals" in {

        val userAnswers = emptyUserAnswers
        val helper      = new UnloadingSummaryHelper(userAnswers, mode)
        val result      = helper.sealsWithRemove

        result mustEqual Nil
      }

      "when there are new seals" in {

        forAll(listWithMaxLength[String](Seals.maxSeals)) {
          strs =>
            val userAnswers = emptyUserAnswers.setValue(SealsQuery, strs)
            val helper      = new UnloadingSummaryHelper(userAnswers, mode)
            val result      = helper.sealsWithRemove

            result.size mustEqual strs.size

            val index = Index(0)
            val str   = strs.head
            result.head mustEqual SummaryListRow(
              key = s"Official customs seal ${index.display}".toKey,
              value = Value(str.toText),
              actions = Some(
                Actions(items =
                  List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.NewSealNumberController.onPageLoad(userAnswers.id, index, mode).url,
                      visuallyHiddenText = Some(s"official customs seal ${index.display} $str"),
                      attributes = Map("id" -> s"change-seal-${index.position}")
                    ),
                    ActionItem(
                      content = "Remove".toText,
                      href = routes.ConfirmRemoveSealController.onPageLoad(userAnswers.id, index, mode).url,
                      visuallyHiddenText = Some(s"official customs seal ${index.display} $str"),
                      attributes = Map("id" -> s"remove-seal-${index.position}")
                    )
                  )
                )
              )
            )
        }
      }
    }

    "when .items" in {

      forAll(listWithMaxLength[String](UnloadingPermission.maxGoodsItems)) {
        strs =>
          val userAnswers = emptyUserAnswers.setValue(GoodsItemsQuery, strs)
          val helper      = new UnloadingSummaryHelper(userAnswers, mode)
          val result      = helper.items

          result.size mustEqual strs.size

          val index = Index(0)
          val str   = strs.head
          result.head mustEqual SummaryListRow(
            key = s"Item ${index.display}".toKey,
            value = Value(str.toText),
            actions = None
          )
      }
    }

    "when .vehicleUsed" in {

      forAll(arbitrary[String]) {
        str =>
          val userAnswers = emptyUserAnswers.setValue(VehicleNameRegistrationReferencePage, str)
          val helper      = new UnloadingSummaryHelper(userAnswers, mode)
          val result      = helper.vehicleUsed.get

          result mustEqual SummaryListRow(
            key = "Name, registration or reference".toKey,
            value = Value(str.toText),
            actions = Some(
              Actions(items =
                List(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.VehicleNameRegistrationReferenceController.onPageLoad(userAnswers.id, mode).url,
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

      forAll(arbitrary[Country]) {
        country =>
          val userAnswers = emptyUserAnswers.setValue(VehicleRegistrationCountryPage, country)
          val helper      = new UnloadingSummaryHelper(userAnswers, mode)
          val result      = helper.registeredCountry.get

          result mustEqual SummaryListRow(
            key = "Registered".toKey,
            value = Value(country.description.toText),
            actions = Some(
              Actions(items =
                List(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.VehicleRegistrationCountryController.onPageLoad(userAnswers.id, mode).url,
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
          val userAnswers = emptyUserAnswers.setValue(GrossMassAmountPage, str)
          val helper      = new UnloadingSummaryHelper(userAnswers, mode)
          val result      = helper.grossMass.get

          result mustEqual SummaryListRow(
            key = "Total gross mass in kilograms".toKey,
            value = Value(str.toText),
            actions = Some(
              Actions(items =
                List(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.GrossMassAmountController.onPageLoad(userAnswers.id, mode).url,
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
          val userAnswers = emptyUserAnswers.setValue(TotalNumberOfItemsPage, int)
          val helper      = new UnloadingSummaryHelper(userAnswers, mode)
          val result      = helper.totalNumberOfItems.get

          result mustEqual SummaryListRow(
            key = "Total number of items".toKey,
            value = Value(int.toString.toText),
            actions = Some(
              Actions(items =
                List(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.TotalNumberOfItemsController.onPageLoad(userAnswers.id, mode).url,
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
          val userAnswers = emptyUserAnswers.setValue(TotalNumberOfPackagesPage, int)
          val helper      = new UnloadingSummaryHelper(userAnswers, mode)
          val result      = helper.totalNumberOfPackages.get

          result mustEqual SummaryListRow(
            key = "Total number of packages".toKey,
            value = Value(int.toString.toText),
            actions = Some(
              Actions(items =
                List(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.TotalNumberOfPackagesController.onPageLoad(userAnswers.id, mode).url,
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
          val userAnswers = emptyUserAnswers.setValue(ChangesToReportPage, str)
          val helper      = new UnloadingSummaryHelper(userAnswers, mode)
          val result      = helper.comments.get

          result mustEqual SummaryListRow(
            key = "Comments".toKey,
            value = Value(str.toText),
            actions = Some(
              Actions(items =
                List(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.ChangesToReportController.onPageLoad(userAnswers.id, mode).url,
                    visuallyHiddenText = Some("comments"),
                    attributes = Map("id" -> "change-comments")
                  ),
                  ActionItem(
                    content = "Remove".toText,
                    href = routes.ConfirmRemoveCommentsController.onPageLoad(userAnswers.id, mode).url,
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
