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
import models.{Identification, Index, NormalMode}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.html.components.{ActionItem, Actions}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.UnloadingFindingsAnswersHelper

class UnloadingFindingsAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "UnloadingFindingsAnswersHelper" - {

    "transportMeansID" - {

      val vehicleIdentificationNumber = Gen.alphaNumStr.sample.value
      val vehicleIdentificationType   = Gen.oneOf(Identification.values).sample.value

      val identificationTypeMessage = messages(s"${Identification.messageKeyPrefix}.${vehicleIdentificationType.toString}")

      "must return None" - {
        s"when $VehicleIdentificationNumberPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.transportMeansID(index)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $VehicleIdentificationNumberPage defined" in {
          val answers = emptyUserAnswers
            .setValue(VehicleIdentificationNumberPage(index), vehicleIdentificationNumber)
            .setValue(VehicleIdentificationTypePage(index), vehicleIdentificationType.identificationType.toString)

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.transportMeansID(index)

          result mustBe Some(
            SummaryListRow(
              key = Key(identificationTypeMessage.toText),
              value = Value(vehicleIdentificationNumber.toText),
              actions = None
            )
          )
        }
      }
    }

    "transportRegisteredCountry" - { // TODO: Change to country codes once implemented

      "must return None" - {
        s"when $VehicleRegistrationCountryPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.transportRegisteredCountry(index)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $VehicleRegistrationCountryPage defined" in {
          val answers = emptyUserAnswers
            .setValue(VehicleRegistrationCountryPage(index), "DE")

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.transportRegisteredCountry(index)

          result mustBe Some(
            SummaryListRow(
              key = Key("Registered country".toText),
              value = Value("DE".toText),
              actions = None
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

    "houseConsignmentSections" - {

      "must return None" - {
        s"when no house consignments defined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.houseConsignmentSections
          result.isEmpty mustBe true
        }
      }

      "must return Some(Row)s" - {
        s"when consignments are defined" in {

          val grossWeight      = Gen.double.sample.value
          val netWeight        = Gen.double.sample.value
          val totalGrossWeight = grossWeight * 2
          val totalNetWeight   = netWeight * 2

          val answers = emptyUserAnswers
            .setValue(GrossWeightPage(index, itemIndex), grossWeight)
            .setValue(NetWeightPage(index, itemIndex), netWeight)
            .setValue(GrossWeightPage(index, Index(1)), grossWeight)
            .setValue(NetWeightPage(index, Index(1)), netWeight)
            .setValue(ConsignorNamePage(index), "name")
            .setValue(ConsignorIdentifierPage(index), "identifier")

          val helper   = new UnloadingFindingsAnswersHelper(answers)
          val sections = helper.houseConsignmentSections.head.rows

          val grossWeightRow          = sections.head
          val netWeightRow            = sections(1)
          val consignorName           = sections(2)
          val consignorIdentification = sections(3)

          grossWeightRow mustBe
            SummaryListRow(
              key = Key("Gross mass".toText),
              value = Value(s"${totalGrossWeight}kg".toText)
            )

          netWeightRow mustBe
            SummaryListRow(
              key = Key("Net mass".toText),
              value = Value(s"${totalNetWeight}kg".toText)
            )

          consignorName mustBe
            SummaryListRow(
              key = Key("Consignor name".toText),
              value = Value("name".toText)
            )

          consignorIdentification mustBe
            SummaryListRow(
              key = Key("Consignor identification number".toText),
              value = Value("identifier".toText)
            )
        }
      }
    }

    "grossWeightRow" - {

      "must return Some(Row)" - {
        s"when total gross weight is passed to totalGrossWeightRow" in {

          val totalGrossWeight = Gen.double.sample.value

          val answers = emptyUserAnswers

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.totalGrossWeightRow(totalGrossWeight)

          result mustBe SummaryListRow(
            key = Key("Gross mass".toText),
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
            key = Key("Net mass".toText),
            value = Value(s"${totalNetWeight}kg".toText),
            actions = None
          )
        }
      }
    }

    "itemDescriptionRow" - {

      val itemDesc = Gen.alphaNumStr.sample.value

      "must return None" - {
        s"when $ItemDescriptionPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.itemDescriptionRow(index, itemIndex)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $ItemDescriptionPage defined" in {
          val answers = emptyUserAnswers
            .setValue(ItemDescriptionPage(index, itemIndex), itemDesc)

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.itemDescriptionRow(index, itemIndex)

          result mustBe
            Some(
              SummaryListRow(
                key = Key("Description".toText),
                value = Value(itemDesc.toText),
                actions = None
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
          val result = helper.grossWeightRow(index, itemIndex)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $GrossWeightPage defined" in {
          val answers = emptyUserAnswers
            .setValue(GrossWeightPage(index, itemIndex), weight)

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.grossWeightRow(index, itemIndex)

          result mustBe Some(
            SummaryListRow(
              key = Key("Gross weight".toText),
              value = Value(s"${weight}kg".toText),
              actions = None
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
          val result = helper.netWeightRow(index, itemIndex)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $NetWeightPage defined" in {
          val answers = emptyUserAnswers
            .setValue(NetWeightPage(index, itemIndex), weight)

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.netWeightRow(index, itemIndex)

          result mustBe Some(
            SummaryListRow(
              key = Key("Net weight".toText),
              value = Value(s"${weight}kg".toText),
              actions = None
            )
          )
        }
      }
    }
  }

}
