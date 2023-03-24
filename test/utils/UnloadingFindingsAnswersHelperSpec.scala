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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.html.components.{ActionItem, Actions}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import uk.gov.hmrc.http.HeaderCarrier
import utils.UnloadingFindingsAnswersHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnloadingFindingsAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  implicit val hc = HeaderCarrier.apply()

  private val countryDesc = "Great Britian"
  "UnloadingFindingsAnswersHelper" - {

    "transportMeansID" - {

      val vehicleIdentificationNumber = Gen.alphaNumStr.sample.value
      val vehicleIdentificationType   = Gen.oneOf(Identification.values).sample.value

      val identificationTypeMessage = messages(s"${Identification.messageKeyPrefix}.${vehicleIdentificationType.toString}")

      "must return None" - {
        s"when $VehicleIdentificationNumberPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers, mockReferenceDataService)
          val result = helper.transportMeansID(index)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $VehicleIdentificationNumberPage defined" in {
          val answers = emptyUserAnswers
            .setValue(VehicleIdentificationNumberPage(index), vehicleIdentificationNumber)
            .setValue(VehicleIdentificationTypePage(index), vehicleIdentificationType.identificationType.toString)

          val helper = new UnloadingFindingsAnswersHelper(answers, mockReferenceDataService)
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

    "transportRegisteredCountry" - {

      "must return Some(Row)" - {
        s"when $VehicleRegistrationCountryPage defined" in {
          val answers = emptyUserAnswers
            .setValue(VehicleRegistrationCountryPage(index), "GB")
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper = new UnloadingFindingsAnswersHelper(answers, mockReferenceDataService)
          val result = helper.transportRegisteredCountry(countryDesc)

          result mustBe
            SummaryListRow(
              key = Key("Registered country".toText),
              value = Value(countryDesc.toText),
              actions = None
            )
        }
      }
    }

    "containerIdentificationNumber" - {

      val containerIdentificationNumber = Gen.alphaNumStr.sample.value

      "must return None" - {
        s"when $ContainerIdentificationNumberPage undefined" in {
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers, mockReferenceDataService)
          val result = helper.containerIdentificationNumber(index)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $ContainerIdentificationNumberPage defined" in {
          val answers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(index), containerIdentificationNumber)
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper = new UnloadingFindingsAnswersHelper(answers, mockReferenceDataService)
          val result = helper.containerIdentificationNumber(index)

          result mustBe Some(
            SummaryListRow(
              key = Key("Container identification number".toText),
              value = Value(containerIdentificationNumber.toText),
              actions = None
            )
          )
        }
      }
    }

    "transportEquipmentSections" - {
      "must return None" - {
        s"when no transport equipments defined" in {
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers, mockReferenceDataService)
          val result = helper.transportEquipmentSections
          result.isEmpty mustBe true
        }
      }

      "must return Some(Row)s" - {
        s"when a transport equipment is are defined" in {

          val answers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(index), "container1")
            .setValue(SealPage(index, index), "seal1")
            .setValue(ContainerIdentificationNumberPage(Index(1)), "container2")
            .setValue(SealPage(Index(1), index), "seal2")
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper              = new UnloadingFindingsAnswersHelper(answers, mockReferenceDataService)
          val transportEquipment1 = helper.transportEquipmentSections.head.rows
          val transportEquipment2 = helper.transportEquipmentSections(1).rows

          val containerRow1 = transportEquipment1.head
          val sealRow1      = transportEquipment1(1)
          val containerRow2 = transportEquipment2.head
          val sealRow2      = transportEquipment2(1)

          containerRow1 mustBe
            SummaryListRow(
              key = Key("Container identification number".toText),
              value = Value("container1".toText)
            )

          sealRow1 mustBe
            SummaryListRow(
              key = Key("Seal 1".toText),
              value = Value("seal1".toText)
            )

          containerRow2 mustBe
            SummaryListRow(
              key = Key("Container identification number".toText),
              value = Value("container2".toText)
            )

          sealRow2 mustBe
            SummaryListRow(
              key = Key("Seal 1".toText),
              value = Value("seal2".toText)
            )
        }
      }
    }

    "transportEquipmentSeal" - {

      val sealIdentifier = Gen.alphaNumStr.sample.value

      "must return None" - {
        s"when $SealPage undefined" in {
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers, mockReferenceDataService)
          val result = helper.transportEquipmentSeal(equipmentIndex, sealIndex)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $SealPage defined" in {
          val answers = emptyUserAnswers
            .setValue(SealPage(equipmentIndex, sealIndex), sealIdentifier)
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper = new UnloadingFindingsAnswersHelper(answers, mockReferenceDataService)
          val result = helper.transportEquipmentSeal(equipmentIndex, sealIndex)

          result mustBe Some(
            SummaryListRow(
              key = Key(s"Seal ${sealIndex.display}".toText),
              value = Value(sealIdentifier.toText),
              actions = None
            )
          )
        }
      }
    }

    "houseConsignmentSections" - {

      "must return None" - {
        s"when no house consignments defined" in {
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers, mockReferenceDataService)
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
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper   = new UnloadingFindingsAnswersHelper(answers, mockReferenceDataService)
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
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper = new UnloadingFindingsAnswersHelper(answers, mockReferenceDataService)
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
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper = new UnloadingFindingsAnswersHelper(answers, mockReferenceDataService)
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
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers, mockReferenceDataService)
          val result = helper.itemDescriptionRow(index, itemIndex)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $ItemDescriptionPage defined" in {
          val answers = emptyUserAnswers
            .setValue(ItemDescriptionPage(index, itemIndex), itemDesc)
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper = new UnloadingFindingsAnswersHelper(answers, mockReferenceDataService)
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
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers, mockReferenceDataService)
          val result = helper.grossWeightRow(index, itemIndex)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $GrossWeightPage defined" in {
          val answers = emptyUserAnswers
            .setValue(GrossWeightPage(index, itemIndex), weight)
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper = new UnloadingFindingsAnswersHelper(answers, mockReferenceDataService)
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
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers, mockReferenceDataService)
          val result = helper.netWeightRow(index, itemIndex)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $NetWeightPage defined" in {
          val answers = emptyUserAnswers
            .setValue(NetWeightPage(index, itemIndex), weight)
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper = new UnloadingFindingsAnswersHelper(answers, mockReferenceDataService)
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
