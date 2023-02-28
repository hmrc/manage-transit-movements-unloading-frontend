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

package utils

import base.SpecBase
import controllers.routes
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import pages._
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.UnloadingRemarksRejectionHelper._

import java.time.LocalDate

class UnloadingRemarksRejectionHelperSpec extends SpecBase with Generators {

  "must return summary list row" - {

    "when .vehicleNameRegistrationReference" in {

      forAll(arbitrary[String]) {
        str =>
          val userAnswers = emptyUserAnswers.setValue(VehicleNameRegistrationReferencePage, str)
          val helper      = new UnloadingRemarksRejectionHelper(userAnswers)
          val result      = helper.vehicleNameRegistrationReference.get

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

      forAll(arbitrary[Int]) {
        int =>
          val userAnswers = emptyUserAnswers.setValue(TotalNumberOfPackagesPage, int)
          val helper      = new UnloadingRemarksRejectionHelper(userAnswers)
          val result      = helper.totalNumberOfPackages.get

          result mustEqual SummaryListRow(
            key = "Total number of packages".toKey,
            value = Value(int.toString.toText),
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

      forAll(arbitrary[Int]) {
        int =>
          val userAnswers = emptyUserAnswers.setValue(TotalNumberOfItemsPage, int)
          val helper      = new UnloadingRemarksRejectionHelper(userAnswers)
          val result      = helper.totalNumberOfItems.get

          result mustEqual SummaryListRow(
            key = "Total number of items".toKey,
            value = Value(int.toString.toText),
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

    "when .GrossWeight" in {

      forAll(arbitrary[String]) {
        str =>
          val userAnswers = emptyUserAnswers.setValue(GrossWeightPage, str)
          val helper      = new UnloadingRemarksRejectionHelper(userAnswers)
          val result      = helper.GrossWeightAmount.get

          result mustEqual SummaryListRow(
            key = "Total gross mass in kilograms".toKey,
            value = Value(str.toText),
            actions = Some(
              Actions(
                items = Seq(
                  ActionItem(
                    content = "Change".toText,
                    href = routes.GrossWeightAmountRejectionController.onPageLoad(userAnswers.id).url,
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

      val date        = LocalDate.parse("2000-01-01")
      val userAnswers = emptyUserAnswers.setValue(DateGoodsUnloadedPage, date)
      val helper      = new UnloadingRemarksRejectionHelper(userAnswers)
      val result      = helper.unloadingDate.get

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

  "RichFunctionalError" - {
    ".toSummaryList" - {
      "must return summary list" in {
        forAll(arbitraryRejectionError.arbitrary) {
          functionalError =>
            functionalError.toSummaryList mustBe SummaryList(
              rows = Seq(
                SummaryListRow(
                  key = "Error code".toKey,
                  value = Value(functionalError.errorType.toString.toText)
                ),
                SummaryListRow(
                  key = "Pointer".toKey,
                  value = Value(functionalError.pointer.value.toText)
                )
              )
            )
        }
      }
    }
  }
}
