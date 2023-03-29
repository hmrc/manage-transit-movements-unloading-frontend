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
import generators.Generators
import models.{Identification, Index}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HouseConsignmentAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  private val countryDesc = "Great Britain"

  "HouseConsignmentAnswersHelper" - {

    "buildTransportSections" - {
      "must return None" - {
        s"when no transport means defined" in {
          val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, index, mockReferenceDataService)
          val result = helper.buildTransportSections.futureValue
          result.isEmpty mustBe true
        }
      }

      "must return Some(Row)s" - {
        val vehicleIdentificationNumber = Gen.alphaNumStr.sample.value
        val vehicleIdentificationType   = Gen.oneOf(Identification.values).sample.value
        val identificationTypeMessage   = messages(s"${Identification.messageKeyPrefix}.${vehicleIdentificationType.toString}")
        s"when there is 1 transport means section defined" in {

          val answers = emptyUserAnswers
            .setValue(DepartureTransportMeansIdentificationNumberPage(index, index), vehicleIdentificationNumber)
            .setValue(DepartureTransportMeansIdentificationTypePage(index, index), vehicleIdentificationType.identificationType.toString)
            .setValue(DepartureTransportMeansCountryPage(index, index), "GB")

          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper          = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
          val result          = helper.buildTransportSections.futureValue
          val transportMeans1 = result.head.rows

          val transportMeansIDRow1      = transportMeans1.head
          val transportMeansCountryRow1 = transportMeans1(1)

          transportMeansIDRow1 mustBe
            SummaryListRow(
              key = Key(identificationTypeMessage.toText),
              value = Value(vehicleIdentificationNumber.toText)
            )

          transportMeansCountryRow1 mustBe
            SummaryListRow(
              key = Key("Registered country".toText),
              value = Value(countryDesc.toText)
            )
        }
        s"when only identification type and number defined" in {

          val answers = emptyUserAnswers
            .setValue(DepartureTransportMeansIdentificationNumberPage(index, index), vehicleIdentificationNumber)
            .setValue(DepartureTransportMeansIdentificationTypePage(index, index), vehicleIdentificationType.identificationType.toString)

          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper          = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
          val result          = helper.buildTransportSections.futureValue
          val transportMeans1 = result.head.rows

          val transportMeansIDRow1 = transportMeans1.head

          transportMeans1.length mustBe 1

          transportMeansIDRow1 mustBe
            SummaryListRow(
              key = Key(identificationTypeMessage.toText),
              value = Value(vehicleIdentificationNumber.toText)
            )

        }
        s"when only country is defined" in {

          val answers = emptyUserAnswers.setValue(DepartureTransportMeansCountryPage(index, index), "GB")
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper          = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
          val result          = helper.buildTransportSections.futureValue
          val transportMeans1 = result.head.rows

          val transportMeansCountryRow1 = transportMeans1.head

          transportMeans1.length mustBe 1

          transportMeansCountryRow1 mustBe
            SummaryListRow(
              key = Key("Registered country".toText),
              value = Value(countryDesc.toText)
            )

        }
        s"when only number is defined" in {

          val answers = emptyUserAnswers.setValue(DepartureTransportMeansIdentificationNumberPage(index, index), vehicleIdentificationNumber)

          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper          = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
          val result          = helper.buildTransportSections.futureValue
          val transportMeans1 = result.head.rows

          transportMeans1 mustBe empty

        }
        s"when multiple transport means sections are defined" in {

          val answers = emptyUserAnswers
            .setValue(DepartureTransportMeansIdentificationNumberPage(index, index), vehicleIdentificationNumber)
            .setValue(DepartureTransportMeansIdentificationTypePage(index, index), vehicleIdentificationType.identificationType.toString)
            .setValue(DepartureTransportMeansCountryPage(index, index), "GB")
            .setValue(DepartureTransportMeansIdentificationNumberPage(index, Index(1)), vehicleIdentificationNumber)
            .setValue(DepartureTransportMeansIdentificationTypePage(index, Index(1)), vehicleIdentificationType.identificationType.toString)
            .setValue(DepartureTransportMeansCountryPage(index, Index(1)), "GB")

          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper          = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
          val result          = helper.buildTransportSections.futureValue
          val transportMeans1 = result.head.rows
          val transportMeans2 = result(1).rows

          val transportMeansIDRow1      = transportMeans1.head
          val transportMeansCountryRow1 = transportMeans1(1)
          val transportMeansIDRow2      = transportMeans2.head
          val transportMeansCountryRow2 = transportMeans2(1)

          transportMeansIDRow1 mustBe
            SummaryListRow(
              key = Key(identificationTypeMessage.toText),
              value = Value(vehicleIdentificationNumber.toText)
            )

          transportMeansCountryRow1 mustBe
            SummaryListRow(
              key = Key("Registered country".toText),
              value = Value(countryDesc.toText)
            )

          transportMeansIDRow2 mustBe
            SummaryListRow(
              key = Key(identificationTypeMessage.toText),
              value = Value(vehicleIdentificationNumber.toText)
            )

          transportMeansCountryRow2 mustBe
            SummaryListRow(
              key = Key("Registered country".toText),
              value = Value(countryDesc.toText)
            )
        }
      }
    }

    "transportMeansID" - {

      val vehicleIdentificationNumber = Gen.alphaNumStr.sample.value
      val vehicleIdentificationType   = Gen.oneOf(Identification.values).sample.value

      val identificationTypeMessage = messages(s"${Identification.messageKeyPrefix}.${vehicleIdentificationType.toString}")

      "must return None" - {
        s"when $DepartureTransportMeansIdentificationNumberPage undefined" in {

          val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, index, mockReferenceDataService)
          val result = helper.transportMeansID(index)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $DepartureTransportMeansIdentificationNumberPage defined" in {
          val answers = emptyUserAnswers
            .setValue(DepartureTransportMeansIdentificationNumberPage(index, index), vehicleIdentificationNumber)
            .setValue(DepartureTransportMeansIdentificationTypePage(index, index), vehicleIdentificationType.identificationType.toString)

          val helper = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
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
          val answers = emptyUserAnswers.setValue(DepartureTransportMeansCountryPage(index, index), "GB")
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
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

    "houseConsignmentSections" - {

      "must return Some(Row)s" - {
        s"when consignments are defined" in {

          val grossWeight      = Gen.double.sample.value
          val netWeight        = Gen.double.sample.value
          val totalGrossWeight = BigDecimal(grossWeight) + BigDecimal(grossWeight)
          val totalNetWeight   = BigDecimal(netWeight) + BigDecimal(netWeight)

          val answers = emptyUserAnswers
            .setValue(GrossWeightPage(index, itemIndex), grossWeight)
            .setValue(NetWeightPage(index, itemIndex), netWeight)
            .setValue(GrossWeightPage(index, Index(1)), grossWeight)
            .setValue(NetWeightPage(index, Index(1)), netWeight)
            .setValue(ConsignorNamePage(index), "name")
            .setValue(ConsignorIdentifierPage(index), "identifier")

          val helper   = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
          val sections = helper.houseConsignmentSection.head.rows

          val grossWeightRow          = sections.head
          val netWeightRow            = sections(1)
          val consignorName           = sections(2)
          val consignorIdentification = sections(3)

          grossWeightRow mustBe
            SummaryListRow(
              key = Key("Gross weight".toText),
              value = Value(s"${totalGrossWeight}kg".toText)
            )

          netWeightRow mustBe
            SummaryListRow(
              key = Key("Net weight".toText),
              value = Value(s"${totalNetWeight}kg".toText)
            )

          consignorName mustBe
            SummaryListRow(
              key = Key("Consignor name".toText),
              value = Value("name".toText)
            )

          consignorIdentification mustBe
            SummaryListRow(
              key = Key("Consignor EORI number or Trader Identification Number (TIN)".toText),
              value = Value("identifier".toText)
            )
        }
        s"when a consignments is defined" - {
          "and consignor identification number is not defined" in {

            val grossWeight = Gen.double.sample.value
            val netWeight   = Gen.double.sample.value

            val answers = emptyUserAnswers
              .setValue(GrossWeightPage(index, itemIndex), grossWeight)
              .setValue(NetWeightPage(index, itemIndex), netWeight)
              .setValue(ConsignorNamePage(index), "name")

            val helper   = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
            val sections = helper.houseConsignmentSection.head.rows

            val grossWeightRow = sections.head
            val netWeightRow   = sections(1)
            val consignorName  = sections(2)

            grossWeightRow mustBe
              SummaryListRow(
                key = Key("Gross weight".toText),
                value = Value(s"${BigDecimal(grossWeight)}kg".toText)
              )

            netWeightRow mustBe
              SummaryListRow(
                key = Key("Net weight".toText),
                value = Value(s"${BigDecimal(netWeight)}kg".toText)
              )

            consignorName mustBe
              SummaryListRow(
                key = Key("Consignor name".toText),
                value = Value("name".toText)
              )

            sections.length mustBe 3

          }
          "and only consignor name is defined" in {

            val answers = emptyUserAnswers
              .setValue(ConsignorNamePage(index), "name")

            val helper   = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
            val sections = helper.houseConsignmentSection.head.rows

            val consignorName = sections.head

            consignorName mustBe
              SummaryListRow(
                key = Key("Consignor name".toText),
                value = Value("name".toText)
              )

            sections.length mustBe 1

          }
          "and only consignor identification number is defined" in {

            val answers = emptyUserAnswers
              .setValue(ConsignorIdentifierPage(index), "identifier")

            val helper   = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
            val sections = helper.houseConsignmentSection.head.rows

            val consignorIdentification = sections.head

            consignorIdentification mustBe
              SummaryListRow(
                key = Key("Consignor EORI number or Trader Identification Number (TIN)".toText),
                value = Value("identifier".toText)
              )

            sections.length mustBe 1

          }
          "and consignor name is not defined" in {

            val grossWeight = Gen.double.sample.value
            val netWeight   = Gen.double.sample.value

            val answers = emptyUserAnswers
              .setValue(GrossWeightPage(index, itemIndex), grossWeight)
              .setValue(NetWeightPage(index, itemIndex), netWeight)
              .setValue(ConsignorIdentifierPage(index), "identifier")

            val helper   = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
            val sections = helper.houseConsignmentSection.head.rows

            val grossWeightRow          = sections.head
            val netWeightRow            = sections(1)
            val consignorIdentification = sections(2)

            grossWeightRow mustBe
              SummaryListRow(
                key = Key("Gross weight".toText),
                value = Value(s"${BigDecimal(grossWeight)}kg".toText)
              )

            netWeightRow mustBe
              SummaryListRow(
                key = Key("Net weight".toText),
                value = Value(s"${BigDecimal(netWeight)}kg".toText)
              )

            consignorIdentification mustBe
              SummaryListRow(
                key = Key("Consignor EORI number or Trader Identification Number (TIN)".toText),
                value = Value("identifier".toText)
              )

            sections.length mustBe 3

          }
          "and gross or net weight not defined" in {

            val answers = emptyUserAnswers
              .setValue(ConsignorNamePage(index), "name")
              .setValue(ConsignorIdentifierPage(index), "identifier")

            val helper   = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
            val sections = helper.houseConsignmentSection.head.rows

            val consignorName           = sections.head
            val consignorIdentification = sections(1)

            consignorName mustBe
              SummaryListRow(
                key = Key("Consignor name".toText),
                value = Value("name".toText)
              )

            consignorIdentification mustBe
              SummaryListRow(
                key = Key("Consignor EORI number or Trader Identification Number (TIN)".toText),
                value = Value("identifier".toText)
              )

            sections.length mustBe 2

          }

        }
      }
    }

    "grossWeightRow" - {

      "must return Some(Row)" - {
        s"when total gross weight is passed to totalGrossWeightRow" in {

          val grossWeight = BigDecimal(Gen.double.sample.value)

          val answers = emptyUserAnswers

          val helper = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
          val result = helper.totalGrossWeightRow(grossWeight)

          result mustBe SummaryListRow(
            key = Key("Gross weight".toText),
            value = Value(s"${grossWeight}kg".toText),
            actions = None
          )
        }
      }
    }

    "totalNetWeightRow" - {

      "must return Some(Row)" - {
        s"when total net weight is passed to totalNetWeightRow" in {

          val netWeight = BigDecimal(Gen.double.sample.value)

          val answers = emptyUserAnswers

          val helper = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
          val result = helper.totalNetWeightRow(netWeight)

          result mustBe SummaryListRow(
            key = Key("Net weight".toText),
            value = Value(s"${netWeight}kg".toText),
            actions = None
          )
        }
      }
    }

    "itemDescriptionRow" - {

      val itemDesc = Gen.alphaNumStr.sample.value

      "must return None" - {
        s"when $ItemDescriptionPage undefined" in {

          val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, index, mockReferenceDataService)
          val result = helper.itemDescriptionRow(index, itemIndex)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $ItemDescriptionPage defined" in {
          val answers = emptyUserAnswers
            .setValue(ItemDescriptionPage(index, itemIndex), itemDesc)

          val helper = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
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

      val grossWeight = Gen.double.sample.value

      "must return None" - {
        s"when $GrossWeightPage undefined" in {

          val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, index, mockReferenceDataService)
          val result = helper.grossWeightRow(index, itemIndex)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $GrossWeightPage defined" in {
          val answers = emptyUserAnswers
            .setValue(GrossWeightPage(index, itemIndex), grossWeight)

          val helper = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
          val result = helper.grossWeightRow(index, itemIndex)

          result mustBe Some(
            SummaryListRow(
              key = Key("Gross weight".toText),
              value = Value(s"${grossWeight}kg".toText),
              actions = None
            )
          )
        }
      }
    }

    "netWeightRow" - {

      val netWeight = Gen.double.sample.value

      "must return None" - {
        s"when $NetWeightPage undefined" in {

          val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, index, mockReferenceDataService)
          val result = helper.netWeightRow(index, itemIndex)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $NetWeightPage defined" in {
          val answers = emptyUserAnswers
            .setValue(NetWeightPage(index, itemIndex), netWeight)

          val helper = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
          val result = helper.netWeightRow(index, itemIndex)

          result mustBe Some(
            SummaryListRow(
              key = Key("Net weight".toText),
              value = Value(s"${netWeight}kg".toText),
              actions = None
            )
          )
        }
      }
    }

    "ItemsSections" - {

      val weight = Gen.double.sample.value

      "must return none" - {
        s"when no Items undefined" in {

          val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, index, mockReferenceDataService)
          val result = helper.itemSections
          result mustBe Nil
        }
      }

      "must return Some(Row)" - {
        s"when an Item is defined" in {
          val answers = emptyUserAnswers
            .setValue(ItemDescriptionPage(index, itemIndex), "test")
            .setValue(GrossWeightPage(index, itemIndex), weight)
            .setValue(NetWeightPage(index, itemIndex), weight)

          val helper = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
          val result = helper.itemSections.head.rows

          result.head mustBe
            SummaryListRow(
              key = Key("Description".toText),
              value = Value("test".toText),
              actions = None
            )
          result(1) mustBe
            SummaryListRow(
              key = Key("Gross weight".toText),
              value = Value(s"${weight}kg".toText),
              actions = None
            )
          result(2) mustBe
            SummaryListRow(
              key = Key("Net weight".toText),
              value = Value(s"${weight}kg".toText),
              actions = None
            )
        }
        s"when Net Weight is not defined" in {
          val answers = emptyUserAnswers
            .setValue(ItemDescriptionPage(index, itemIndex), "test")
            .setValue(GrossWeightPage(index, itemIndex), weight)

          val helper = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
          val result = helper.itemSections.head.rows

          result.head mustBe
            SummaryListRow(
              key = Key("Description".toText),
              value = Value("test".toText),
              actions = None
            )
          result(1) mustBe
            SummaryListRow(
              key = Key("Gross weight".toText),
              value = Value(s"${weight}kg".toText),
              actions = None
            )
          result.length mustBe 2
        }
        s"when Gross Weight is not defined" in {
          val answers = emptyUserAnswers
            .setValue(ItemDescriptionPage(index, itemIndex), "test")
            .setValue(NetWeightPage(index, itemIndex), weight)

          val helper = new HouseConsignmentAnswersHelper(answers, index, mockReferenceDataService)
          val result = helper.itemSections.head.rows

          result.head mustBe
            SummaryListRow(
              key = Key("Description".toText),
              value = Value("test".toText),
              actions = None
            )
          result(1) mustBe
            SummaryListRow(
              key = Key("Net weight".toText),
              value = Value(s"${weight}kg".toText),
              actions = None
            )
          result.length mustBe 2
        }
      }

    }

  }

}
