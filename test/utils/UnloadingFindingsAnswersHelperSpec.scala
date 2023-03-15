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

package utils.cyaHelpers

import base.SpecBase
import generators.Generators
import models.{Identification, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.html.components.{ActionItem, Actions}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.UnloadingFindingsAnswersHelper

class UnloadingFindingsAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "UnloadingFindingsAnswersHelper" - { //TODO: Test iterable sections and test links?

    "departureMeansID" - {

      val vehicleIdentificationNumber = Gen.alphaNumStr.sample.value
      val vehicleIdentificationType   = Gen.oneOf(Identification.values).sample.value

      val identificationTypeMessage       = messages(s"${Identification.messageKeyPrefix}.${vehicleIdentificationType.toString}")
      val identificationTypeHiddenMessage = messages(s"${Identification.messageKeyPrefix}.${vehicleIdentificationType.toString}.change.hidden")

      "must return None" - {
        s"when $VehicleIdentificationNumberPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.departureMeansID
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $VehicleIdentificationNumberPage defined" in {
          val answers = emptyUserAnswers
            .setValue(VehicleIdentificationNumberPage, vehicleIdentificationNumber)
            .setValue(VehicleIdentificationTypePage, vehicleIdentificationType.identificationType.toString)

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.departureMeansID

          result mustBe Some(
            SummaryListRow(
              key = Key(identificationTypeMessage.toText),
              value = Value(vehicleIdentificationNumber.toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = controllers.routes.VehicleIdentificationNumberController.onPageLoad(arrivalId, NormalMode).url,
                      visuallyHiddenText = Some(identificationTypeHiddenMessage),
                      attributes = Map("id" -> "change-departure-means-id")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }

    "departureRegisteredCountry" - { // TODO: Change to country codes once implemented

      "must return None" - {
        s"when $VehicleRegistrationCountryPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.departureRegisteredCountry
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $VehicleRegistrationCountryPage defined" in {
          val answers = emptyUserAnswers
            .setValue(VehicleRegistrationCountryPage, "DE")

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.departureRegisteredCountry

          result mustBe Some(
            SummaryListRow(
              key = Key("Registered country".toText),
              value = Value("DE".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = controllers.routes.VehicleRegistrationCountryController.onPageLoad(arrivalId, NormalMode).url,
                      visuallyHiddenText = Some("registered country"),
                      attributes = Map("id" -> "change-departure-means-country")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }

    "containerIdentificationNumber" - {

      val containerIdentificationNumber = Gen.alphaNumStr.sample.value

      "must return None" - {
        s"when $ContainerIdentificationNumberPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.containerIdentificationNumber(index)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $ContainerIdentificationNumberPage defined" in {
          val answers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(index), containerIdentificationNumber)

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.containerIdentificationNumber(index)

          result mustBe Some(
            SummaryListRow(
              key = Key("Container identification number".toText),
              value = Value(containerIdentificationNumber.toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = controllers.routes.NewContainerIdentificationNumberController.onPageLoad(arrivalId, index, NormalMode).url,
                      visuallyHiddenText = Some(s"container identification number $containerIdentificationNumber"),
                      attributes = Map("id" -> s"change-container-identification-number-${index.display}")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }

    "transportEquipmentSeal" - {

      val sealIdentifier = Gen.alphaNumStr.sample.value

      "must return None" - {
        s"when $SealPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.transportEquipmentSeal(equipmentIndex, sealIndex)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $SealPage defined" in {
          val answers = emptyUserAnswers
            .setValue(SealPage(equipmentIndex, sealIndex), sealIdentifier)

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.transportEquipmentSeal(equipmentIndex, sealIndex)

          result mustBe Some(
            SummaryListRow(
              key = Key(s"Seal ${sealIndex.display}".toText),
              value = Value(sealIdentifier.toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = controllers.routes.NewSealNumberController.onPageLoad(arrivalId, equipmentIndex, sealIndex, NormalMode).url,
                      visuallyHiddenText = Some(s"seal ${sealIndex.display} - $sealIdentifier"),
                      attributes = Map("id" -> s"change-seal-identifier-${sealIndex.display}")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }

    "transportEquipmentNewSeal" - {

      val sealPrefixNumber = 1
      val sealIdentifier   = Gen.alphaNumStr.sample.value

      "must return None" - {
        s"when $NewSealPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.transportEquipmentNewSeal(equipmentIndex, sealIndex, sealPrefixNumber)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $NewSealPage defined" in {

          val answers = emptyUserAnswers
            .setValue(NewSealPage(equipmentIndex, sealIndex), sealIdentifier)

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.transportEquipmentNewSeal(equipmentIndex, sealIndex, sealPrefixNumber)

          result mustBe Some(
            SummaryListRow(
              key = Key(s"Seal $sealPrefixNumber".toText),
              value = Value(sealIdentifier.toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = controllers.routes.NewSealNumberController.onPageLoad(arrivalId, equipmentIndex, sealIndex, NormalMode, newSeal = true).url,
                      visuallyHiddenText = Some(s"seal $sealPrefixNumber - $sealIdentifier"),
                      attributes = Map("id" -> s"change-new-seal-identifier-$sealPrefixNumber")
                    ),
                    ActionItem(
                      content = "Remove".toText,
                      href = controllers.routes.ConfirmRemoveSealController.onPageLoad(arrivalId, equipmentIndex, sealIndex, NormalMode).url,
                      visuallyHiddenText = Some(s"seal $sealPrefixNumber - $sealIdentifier"),
                      attributes = Map("id" -> s"remove-new-seal-identifier-$sealPrefixNumber")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }

    "itemsSummarySection" ignore {} //TODO this

    "numberOfItemsRow" - {

      val numberOfItems = arbitrary[Int].sample.value

      "must return Some(Row)" - {
        s"when number of items is passed to numberOfItemsRow" in {

          val answers = emptyUserAnswers

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.numberOfItemsRow(numberOfItems)

          result mustBe SummaryListRow(
            key = Key("Total number of items".toText),
            value = Value(s"$numberOfItems".toText),
            actions = None
          )
        }
      }
    }

    "totalGrossWeightRow" - {

      "must return Some(Row)" - {
        s"when total gross weight is passed to totalGrossWeightRow" in {

          val totalGrossWeight = Gen.double.sample.value

          val answers = emptyUserAnswers

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.totalGrossWeightRow(totalGrossWeight)

          result mustBe SummaryListRow(
            key = Key("Total gross weight of all items".toText),
            value = Value(s"${totalGrossWeight}kg".toText),
            actions = None
          )
        }
      }
    }

    "totalNetWeightRow" - {

      "must return Some(Row)" - {
        s"when total net weight is passed to totalNetWeightRow" in {

          val totalNetWeight = Gen.double.sample.value

          val answers = emptyUserAnswers

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.totalNetWeightRow(totalNetWeight)

          result mustBe SummaryListRow(
            key = Key("Total net weight of all items".toText),
            value = Value(s"${totalNetWeight}kg".toText),
            actions = None
          )
        }
      }
    }

    "itemSections" ignore {} // TODO this

    "itemDescriptionRow" - {

      val itemDesc = Gen.alphaNumStr.sample.value

      "must return None" - {
        s"when $ItemDescriptionPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.itemDescriptionRow(itemIndex)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $ItemDescriptionPage defined" in {
          val answers = emptyUserAnswers
            .setValue(ItemDescriptionPage(itemIndex), itemDesc)

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.itemDescriptionRow(itemIndex)

          result mustBe
            Some(
              SummaryListRow(
                key = Key("Description".toText),
                value = Value(itemDesc.toText),
                actions = None //TODO : Add change link once page is implemented
              )
            )
        }
      }
    }

    "grossWeightRow" - {

      val weight = Gen.double.sample.value

      "must return None" - {
        s"when $GrossWeightPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.grossWeightRow(itemIndex)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $GrossWeightPage defined" in {
          val answers = emptyUserAnswers
            .setValue(GrossWeightPage(itemIndex), weight)

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.grossWeightRow(itemIndex)

          result mustBe Some(
            SummaryListRow(
              key = Key("Gross weight".toText),
              value = Value(s"${weight}kg".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = controllers.routes.GrossWeightController.onPageLoad(arrivalId, itemIndex, NormalMode).url,
                      visuallyHiddenText = Some(s"gross weight of item ${itemIndex.display}"),
                      attributes = Map("id" -> s"change-gross-weight-${itemIndex.display}")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }

    "netWeightRow" - {

      val weight = Gen.double.sample.value

      "must return None" - {
        s"when $NetWeightPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.netWeightRow(itemIndex)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $NetWeightPage defined" in {
          val answers = emptyUserAnswers
            .setValue(NetWeightPage(itemIndex), weight)

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.netWeightRow(itemIndex)

          result mustBe Some(
            SummaryListRow(
              key = Key("Net weight".toText),
              value = Value(s"${weight}kg".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = controllers.routes.NetWeightController.onPageLoad(arrivalId, itemIndex, NormalMode).url,
                      visuallyHiddenText = Some(s"net weight of item ${itemIndex.display}"),
                      attributes = Map("id" -> s"change-net-weight-${itemIndex.display}")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }

    "additionalComment" - {

      val comment = Gen.alphaNumStr.sample.value

      "must return None" - {
        s"when $UnloadingCommentsPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.additionalComment
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $NetWeightPage defined" in {
          val answers = emptyUserAnswers
            .setValue(UnloadingCommentsPage, comment)

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.additionalComment

          result mustBe Some(
            SummaryListRow(
              key = Key("Comments".toText),
              value = Value(comment.toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = controllers.routes.UnloadingCommentsController.onPageLoad(arrivalId, NormalMode).url,
                      visuallyHiddenText = Some("comments"),
                      attributes = Map("id" -> "change-comment")
                    ),
                    ActionItem(
                      content = "Remove".toText,
                      href = controllers.routes.ConfirmRemoveCommentsController.onPageLoad(arrivalId, NormalMode).url,
                      visuallyHiddenText = Some("comments"),
                      attributes = Map("id" -> "remove-comment")
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
