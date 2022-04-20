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
import org.scalacheck.Arbitrary.arbitrary
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import java.time.LocalDate

class UnloadingRemarksRejectionHelperSpec extends SpecBase {

  "must return summary list row" - {

    "when .vehicleNameRegistrationReference" in {

      forAll(arbitrary[String]) {
        str =>
          val userAnswers = emptyUserAnswers
          val helper      = new UnloadingRemarksRejectionHelper
          val result      = helper.vehicleNameRegistrationReference(userAnswers.id, str)

          result mustEqual SummaryListRow(
            key = "Name, registration or reference".toKey,
            value = Value(str.toText),
            actions = Some(
              Actions(
                items = Seq(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.VehicleNameRegistrationRejectionController.onPageLoad(userAnswers.id).url,
                    visuallyHiddenText = Some("name, registration or reference"),
                    attributes = Map("id" -> "change-vehicle-registration-rejection")
                  )
                )
              )
            )
          )
      }
    }

    "when .totalNumberOfPackages" in {

      forAll(arbitrary[String]) {
        str =>
          val userAnswers = emptyUserAnswers
          val helper      = new UnloadingRemarksRejectionHelper
          val result      = helper.totalNumberOfPackages(userAnswers.id, str)

          result mustEqual SummaryListRow(
            key = "Total number of packages".toKey,
            value = Value(str.toText),
            actions = Some(
              Actions(
                items = Seq(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.TotalNumberOfPackagesRejectionController.onPageLoad(userAnswers.id).url,
                    visuallyHiddenText = Some("total number of packages"),
                    attributes = Map("id" -> "change-total-number-of-packages")
                  )
                )
              )
            )
          )
      }
    }

    "when .totalNumberOfItems" in {

      forAll(arbitrary[String]) {
        str =>
          val userAnswers = emptyUserAnswers
          val helper      = new UnloadingRemarksRejectionHelper
          val result      = helper.totalNumberOfItems(userAnswers.id, str)

          result mustEqual SummaryListRow(
            key = "Total number of items".toKey,
            value = Value(str.toText),
            actions = Some(
              Actions(
                items = Seq(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.TotalNumberOfItemsRejectionController.onPageLoad(userAnswers.id).url,
                    visuallyHiddenText = Some("total number of items"),
                    attributes = Map("id" -> "change-total-number-of-items")
                  )
                )
              )
            )
          )
      }
    }

    "when .grossMassAmount" in {

      forAll(arbitrary[String]) {
        str =>
          val userAnswers = emptyUserAnswers
          val helper      = new UnloadingRemarksRejectionHelper
          val result      = helper.grossMassAmount(userAnswers.id, str)

          result mustEqual SummaryListRow(
            key = "Total gross mass in kilograms".toKey,
            value = Value(str.toText),
            actions = Some(
              Actions(
                items = Seq(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.GrossMassAmountRejectionController.onPageLoad(userAnswers.id).url,
                    visuallyHiddenText = Some("total gross mass in kilograms"),
                    attributes = Map("id" -> "change-gross-mass-amount")
                  )
                )
              )
            )
          )
      }
    }

    "when .unloadingDate" in {

      val userAnswers = emptyUserAnswers
      val helper      = new UnloadingRemarksRejectionHelper
      val result      = helper.unloadingDate(userAnswers.id, LocalDate.parse("2000-01-01"))

      result mustEqual SummaryListRow(
        key = "Unloading date".toKey,
        value = Value("1 January 2000".toText),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                content = "Change".toText,
                href = routes.DateGoodsUnloadedRejectionController.onPageLoad(userAnswers.id).url,
                visuallyHiddenText = Some("unloading date"),
                attributes = Map("id" -> "change-date-goods-unloaded")
              )
            )
          )
        )
      )
    }

  }
}
