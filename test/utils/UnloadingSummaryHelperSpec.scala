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
import models.{CheckMode, Index, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import uk.gov.hmrc.viewmodels.MessageInterpolators
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}

class UnloadingSummaryHelperSpec extends SpecBase {

  "must return summary list row" - {

    "when .seals" in {

      forAll(arbitrary[String], arbitrary[Int]) {
        (str, position) =>
          val userAnswers = emptyUserAnswers
          val index       = Index(position)
          val helper      = new UnloadingSummaryHelper(userAnswers)
          val result      = helper.seals(index, str)

          result mustEqual Row(
            key = Key(msg"changeSeal.sealList.label".withArgs(index.display)),
            value = Value(lit"$str"),
            actions = List(
              Action(
                content = msg"site.edit",
                href = controllers.routes.NewSealNumberController.onPageLoad(userAnswers.id, index, CheckMode).url,
                visuallyHiddenText = Some(msg"changeSeal.sealList.change.hidden".withArgs(index.display, str)),
                attributes = Map("id" -> s"change-seal-${index.position}")
              )
            )
          )
      }
    }

    "when .sealsWithRemove" in {

      forAll(arbitrary[String], arbitrary[Int]) {
        (str, position) =>
          val userAnswers = emptyUserAnswers
          val index       = Index(position)
          val helper      = new UnloadingSummaryHelper(userAnswers)
          val result      = helper.sealsWithRemove(index, str)

          result mustEqual Row(
            key = Key(msg"changeSeal.sealList.label".withArgs(index.display)),
            value = Value(lit"$str"),
            actions = List(
              Action(
                content = msg"site.edit",
                href = controllers.routes.NewSealNumberController.onPageLoad(userAnswers.id, index, CheckMode).url,
                visuallyHiddenText = Some(msg"changeSeal.sealList.change.hidden".withArgs(index.display, str)),
                attributes = Map("id" -> s"change-seal-${index.position}")
              ),
              Action(
                content = msg"site.delete",
                href = controllers.routes.ConfirmRemoveSealController.onPageLoad(userAnswers.id, index, NormalMode).url,
                visuallyHiddenText = Some(msg"changeSeal.sealList.remove.hidden".withArgs(index.display, str)),
                attributes = Map("id" -> s"remove-seal-${index.position}")
              )
            )
          )
      }
    }

    "when .items" in {

      forAll(arbitrary[String], arbitrary[Int]) {
        (str, position) =>
          val userAnswers = emptyUserAnswers
          val index       = Index(position)
          val helper      = new UnloadingSummaryHelper(userAnswers)
          val result      = helper.items(index, str)

          result mustEqual Row(
            key = Key(msg"changeItem.itemList.label".withArgs(index.display)),
            value = Value(lit"$str"),
            actions = Nil
          )
      }
    }

    "when .vehicleUsed" in {

      forAll(arbitrary[String]) {
        str =>
          val userAnswers = emptyUserAnswers
          val helper      = new UnloadingSummaryHelper(userAnswers)
          val result      = helper.vehicleUsed(str)

          result mustEqual Row(
            key = Key(msg"changeVehicle.reference.label"),
            value = Value(lit"$str"),
            actions = List(
              Action(
                content = msg"site.edit",
                href = controllers.routes.VehicleNameRegistrationReferenceController.onPageLoad(userAnswers.id, CheckMode).url,
                visuallyHiddenText = Some(msg"changeVehicle.reference.change.hidden"),
                attributes = Map("id" -> "change-vehicle-reference")
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

          result mustEqual Row(
            key = Key(msg"changeVehicle.registeredCountry.label"),
            value = Value(lit"$str"),
            actions = List(
              Action(
                content = msg"site.edit",
                href = controllers.routes.VehicleRegistrationCountryController.onPageLoad(userAnswers.id, CheckMode).url,
                visuallyHiddenText = Some(msg"changeVehicle.registeredCountry.change.hidden"),
                attributes = Map("id" -> "change-vehicle-country")
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

          result mustEqual Row(
            key = Key(msg"changeItems.grossMass.label"),
            value = Value(lit"$str"),
            actions = List(
              Action(
                content = msg"site.edit",
                href = controllers.routes.GrossMassAmountController.onPageLoad(userAnswers.id, CheckMode).url,
                visuallyHiddenText = Some(msg"changeItems.grossMass.change.hidden"),
                attributes = Map("id" -> "change-gross-mass")
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

          result mustEqual Row(
            key = Key(msg"changeItems.totalNumberOfItems.label"),
            value = Value(lit"$int"),
            actions = List(
              Action(
                content = msg"site.edit",
                href = controllers.routes.TotalNumberOfItemsController.onPageLoad(userAnswers.id, CheckMode).url,
                visuallyHiddenText = Some(msg"changeItems.totalNumberOfItems.change.hidden"),
                attributes = Map("id" -> "change-total-number-of-items")
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

          result mustEqual Row(
            key = Key(msg"changeItems.totalNumberOfPackages.label"),
            value = Value(lit"$int"),
            actions = List(
              Action(
                content = msg"site.edit",
                href = controllers.routes.TotalNumberOfPackagesController.onPageLoad(userAnswers.id, CheckMode).url,
                visuallyHiddenText = Some(msg"changeItems.totalNumberOfPackages.change.hidden"),
                attributes = Map("id" -> "change-total-number-of-packages")
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

          result mustEqual Row(
            key = Key(msg"changeItems.comments.label"),
            value = Value(lit"$str"),
            actions = List(
              Action(
                content = msg"site.edit",
                href = controllers.routes.ChangesToReportController.onPageLoad(userAnswers.id, NormalMode).url,
                visuallyHiddenText = Some(msg"changeItems.comments.change.hidden"),
                attributes = Map("id" -> "change-comments")
              ),
              Action(
                content = msg"site.delete",
                href = controllers.routes.ConfirmRemoveCommentsController.onPageLoad(userAnswers.id, NormalMode).url,
                visuallyHiddenText = Some(msg"changeItems.comments.remove.hidden"),
                attributes = Map("id" -> "remove-comment")
              )
            )
          )
      }
    }

  }
}
