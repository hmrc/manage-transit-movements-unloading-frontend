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
import uk.gov.hmrc.viewmodels.MessageInterpolators
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}

import java.time.LocalDate

class UnloadingRemarksRejectionHelperSpec extends SpecBase {

  "must return summary list row" - {

    "when .vehicleNameRegistrationReference" in {

      forAll(arbitrary[String]) {
        str =>
          val userAnswers = emptyUserAnswers
          val helper      = new UnloadingRemarksRejectionHelper
          val result      = helper.vehicleNameRegistrationReference(userAnswers.id, str)

          result mustEqual Row(
            key = Key(msg"changeVehicle.reference.label"),
            value = Value(lit"$str"),
            actions = List(
              Action(
                content = msg"site.edit",
                href = routes.VehicleNameRegistrationRejectionController.onPageLoad(userAnswers.id).url,
                visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"changeVehicle.reference.label")),
                attributes = Map("id" -> "change-vehicle-registration-rejection")
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

          result mustEqual Row(
            key = Key(msg"changeItems.totalNumberOfPackages.label"),
            value = Value(lit"$str"),
            actions = List(
              Action(
                content = msg"site.edit",
                href = routes.TotalNumberOfPackagesRejectionController.onPageLoad(userAnswers.id).url,
                visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"changeItems.totalNumberOfPackages.label"))
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

          result mustEqual Row(
            key = Key(msg"changeItems.totalNumberOfItems.label"),
            value = Value(lit"$str"),
            actions = List(
              Action(
                content = msg"site.edit",
                href = routes.TotalNumberOfItemsRejectionController.onPageLoad(userAnswers.id).url,
                visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"changeItems.totalNumberOfItems.label"))
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

          result mustEqual Row(
            key = Key(msg"changeItems.grossMass.label"),
            value = Value(lit"$str"),
            actions = List(
              Action(
                content = msg"site.edit",
                href = routes.GrossMassAmountRejectionController.onPageLoad(userAnswers.id).url,
                visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"changeItems.grossMass.label"))
              )
            )
          )
      }
    }

    "when .unloadingDate" in {

      val userAnswers = emptyUserAnswers
      val helper      = new UnloadingRemarksRejectionHelper
      val result      = helper.unloadingDate(userAnswers.id, LocalDate.parse("2000-01-01"))

      result mustEqual Row(
        key = Key(msg"changeItems.dateGoodsUnloaded.label"),
        value = Value(lit"1 January 2000"),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.DateGoodsUnloadedRejectionController.onPageLoad(userAnswers.id).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"changeItems.dateGoodsUnloaded.label")),
            attributes = Map("id" -> "change-date-goods-unloaded")
          )
        )
      )
    }

  }
}
