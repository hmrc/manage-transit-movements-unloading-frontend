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
import models.Identification
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.libs.json.{JsObject, Json}
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.UnloadingFindingsAnswersHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnloadingFindingsAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  private val countryDesc = "Great Britain"

  "UnloadingFindingsAnswersHelper" - {

    "buildTransportSections" - {
      "must return None" - {
        s"when no transport means defined" in {
          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers, mockReferenceDataService)
          val result = helper.buildTransportSections.futureValue
          result.isEmpty mustBe true
        }
      }

      "must return Some(Row)s" - {
        val vehicleIdentificationNumber = Gen.alphaNumStr.sample.value
        val vehicleIdentificationType   = Gen.oneOf(Identification.values).sample.value
        val identificationTypeMessage   = messages(s"${Identification.messageKeyPrefix}.${vehicleIdentificationType.toString}")
        s"when there is 1 transport means section defined" in {

          val json = Json
            .parse(
              s"""
                 |{
                 |   "Consignment" : {
                 |       "DepartureTransportMeans" : [
                 |           {
                 |               "sequenceNumber" : "dtm-1",
                 |               "typeOfIdentification" : "${vehicleIdentificationType.identificationType}",
                 |               "identificationNumber" : "$vehicleIdentificationNumber",
                 |               "nationality" : "GB"
                 |           }
                 |       ]
                 |   }
                 |}
                 |
                 |""".stripMargin
            )
            .as[JsObject]

          val userAnswers = emptyUserAnswers.copy(ie043Data = json)

          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper          = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
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

          val json = Json
            .parse(
              s"""
                 |{
                 |   "Consignment" : {
                 |       "DepartureTransportMeans" : [
                 |           {
                 |               "sequenceNumber" : "dtm-1",
                 |               "typeOfIdentification" : "${vehicleIdentificationType.identificationType}",
                 |               "identificationNumber" : "$vehicleIdentificationNumber"
                 |           }
                 |       ]
                 |   }
                 |}
                 |
                 |""".stripMargin
            )
            .as[JsObject]

          val userAnswers = emptyUserAnswers.copy(ie043Data = json)

          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper          = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
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

          val json = Json
            .parse(
              s"""
                 |{
                 |   "Consignment" : {
                 |       "DepartureTransportMeans" : [
                 |           {
                 |               "sequenceNumber" : "dtm-1",
                 |               "nationality" : "GB"
                 |           }
                 |       ]
                 |   }
                 |}
                 |
                 |""".stripMargin
            )
            .as[JsObject]

          val userAnswers = emptyUserAnswers.copy(ie043Data = json)

          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper          = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
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

          val json = Json
            .parse(
              s"""
                 |{
                 |   "Consignment" : {
                 |       "DepartureTransportMeans" : [
                 |           {
                 |               "sequenceNumber" : "dtm-1",
                 |               "identificationNumber" : "$vehicleIdentificationNumber"
                 |           }
                 |       ]
                 |   }
                 |}
                 |
                 |""".stripMargin
            )
            .as[JsObject]

          val userAnswers = emptyUserAnswers.copy(ie043Data = json)

          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper          = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
          val result          = helper.buildTransportSections.futureValue
          val transportMeans1 = result.head.rows

          transportMeans1 mustBe empty

        }
        s"when multiple transport means sections are defined" in {

          val json = Json
            .parse(
              s"""
                 |{
                 |   "Consignment" : {
                 |       "DepartureTransportMeans" : [
                 |           {
                 |               "sequenceNumber" : "dtm-1",
                 |               "typeOfIdentification" : "${vehicleIdentificationType.identificationType}",
                 |               "identificationNumber" : "$vehicleIdentificationNumber",
                 |               "nationality" : "GB"
                 |           },
                 |           {
                 |               "sequenceNumber" : "dtm-1",
                 |               "typeOfIdentification" : "${vehicleIdentificationType.identificationType}",
                 |               "identificationNumber" : "$vehicleIdentificationNumber",
                 |               "nationality" : "GB"
                 |           }
                 |       ]
                 |   }
                 |}
                 |
                 |""".stripMargin
            )
            .as[JsObject]

          val userAnswers = emptyUserAnswers.copy(ie043Data = json)

          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper          = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
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
        s"when $VehicleIdentificationNumberPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers, mockReferenceDataService)
          val result = helper.transportMeansID(index)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $VehicleIdentificationNumberPage defined" in {

          val json = Json
            .parse(
              s"""
                 |{
                 |   "Consignment" : {
                 |       "DepartureTransportMeans" : [
                 |           {
                 |               "sequenceNumber" : "dtm-1",
                 |               "typeOfIdentification" : "${vehicleIdentificationType.identificationType}",
                 |               "identificationNumber" : "$vehicleIdentificationNumber",
                 |               "nationality" : "GB"
                 |           }
                 |       ]
                 |   }
                 |}
                 |""".stripMargin
            )
            .as[JsObject]

          val userAnswers = emptyUserAnswers.copy(ie043Data = json)

          val helper = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
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
        s"when $DepartureMeansOfTransportCountryPage defined" in {
          val answers = emptyUserAnswers
            .setValue(DepartureMeansOfTransportCountryPage(index), "GB")

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

      val containerIdentificationNumber: String = Gen.alphaNumStr.sample.value

      "must return None" - {
        s"when $ContainerIdentificationNumberPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers, mockReferenceDataService)
          val result = helper.containerIdentificationNumber(index)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $ContainerIdentificationNumberPage defined" in {

          val json = Json
            .parse(
              s"""
                 |{
                 |   "Consignment" : {
                 |       "TransportEquipment" : [
                 |           {
                 |               "sequenceNumber" : "te1",
                 |               "containerIdentificationNumber" : "$containerIdentificationNumber"
                 |           }
                 |       ]
                 |   }
                 |}
                 |""".stripMargin
            )
            .as[JsObject]

          val userAnswers = emptyUserAnswers.copy(ie043Data = json)

          val helper = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
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
          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers, mockReferenceDataService)
          val result = helper.transportEquipmentSections
          result.isEmpty mustBe true
        }
      }

      "must return Some(Row)s" - {
        "when container row is not defined" in {

          val json = Json
            .parse(
              s"""
                 |{
                 |    "Consignment" : {
                 |        "TransportEquipment" : [
                 |            {
                 |                "sequenceNumber" : "te1",
                 |                "numberOfSeals" : 103,
                 |                "Seal" : [
                 |                    {
                 |                        "sequenceNumber" : "1001",
                 |                        "identifier" : "seal1"
                 |                    }
                 |                ]
                 |            }
                 |        ]
                 |    }
                 |}
                 |""".stripMargin
            )
            .as[JsObject]

          val userAnswers = emptyUserAnswers.copy(ie043Data = json)

          val helper              = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
          val transportEquipment1 = helper.transportEquipmentSections.head.rows

          val sealRow1 = transportEquipment1.head

          sealRow1 mustBe
            SummaryListRow(
              key = Key("Seal 1".toText),
              value = Value("seal1".toText)
            )

          transportEquipment1.length mustBe 1

        }
        "when no seals are defined" in {

          val json = Json
            .parse(
              s"""
                 |{
                 |    "Consignment" : {
                 |        "TransportEquipment" : [
                 |            {
                 |                "sequenceNumber" : "te1",
                 |                "containerIdentificationNumber" : "container1"
                 |            }
                 |        ]
                 |    }
                 |}
                 |""".stripMargin
            )
            .as[JsObject]

          val userAnswers = emptyUserAnswers.copy(ie043Data = json)

          val helper              = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
          val transportEquipment1 = helper.transportEquipmentSections.head.rows

          val containerRow1 = transportEquipment1.head

          containerRow1 mustBe
            SummaryListRow(
              key = Key("Container identification number".toText),
              value = Value("container1".toText)
            )

          transportEquipment1.length mustBe 1

        }
        s"when 1 transport equipment section is defined" in {

          val json = Json
            .parse(
              s"""
                 |{
                 |    "Consignment" : {
                 |        "TransportEquipment" : [
                 |            {
                 |                "sequenceNumber" : "te1",
                 |                "containerIdentificationNumber" : "container1",
                 |                "numberOfSeals" : 103,
                 |                "Seal" : [
                 |                    {
                 |                        "sequenceNumber" : "1001",
                 |                        "identifier" : "seal1"
                 |                    }
                 |                ]
                 |            }
                 |        ]
                 |    }
                 |}
                 |""".stripMargin
            )
            .as[JsObject]

          val userAnswers = emptyUserAnswers.copy(ie043Data = json)

          val helper              = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
          val transportEquipment1 = helper.transportEquipmentSections.head.rows

          val containerRow1 = transportEquipment1.head
          val sealRow1      = transportEquipment1(1)

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

        }
        s"when multiple transport equipment sections are defined" in {

          val json = Json
            .parse(
              s"""
                 |{
                 |    "Consignment" : {
                 |        "TransportEquipment" : [
                 |            {
                 |                "sequenceNumber" : "te1",
                 |                "containerIdentificationNumber" : "container1",
                 |                "numberOfSeals" : 103,
                 |                "Seal" : [
                 |                    {
                 |                        "sequenceNumber" : "1001",
                 |                        "identifier" : "seal1"
                 |                    }
                 |                ]
                 |            },
                 |            {
                 |                "sequenceNumber" : "te1",
                 |                "containerIdentificationNumber" : "container2",
                 |                "numberOfSeals" : 103,
                 |                "Seal" : [
                 |                    {
                 |                        "sequenceNumber" : "1001",
                 |                        "identifier" : "seal2"
                 |                    }
                 |                ]
                 |            }
                 |        ]
                 |    }
                 |}
                 |""".stripMargin
            )
            .as[JsObject]

          val userAnswers = emptyUserAnswers.copy(ie043Data = json)

          val helper              = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
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

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers, mockReferenceDataService)
          val result = helper.transportEquipmentSeal(equipmentIndex, sealIndex)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $SealPage defined" in {

          val json = Json
            .parse(
              s"""
                 |{
                 |    "Consignment" : {
                 |        "TransportEquipment" : [
                 |            {
                 |                "sequenceNumber" : "te1",
                 |                "containerIdentificationNumber" : "container1",
                 |                "numberOfSeals" : 103,
                 |                "Seal" : [
                 |                    {
                 |                        "sequenceNumber" : "1001",
                 |                        "identifier" : "$sealIdentifier"
                 |                    }
                 |                ]
                 |            }
                 |        ]
                 |    }
                 |}
                 |""".stripMargin
            )
            .as[JsObject]

          val userAnswers = emptyUserAnswers.copy(ie043Data = json)

          val helper = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
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

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers, mockReferenceDataService)
          val result = helper.houseConsignmentSections
          result.isEmpty mustBe true
        }
      }

      "must return Some(Row)s" - {
        s"when consignments are defined" in {

          val grossWeight      = Gen.double.sample.value
          val netWeight        = Gen.double.sample.value
          val totalGrossWeight = (BigDecimal(grossWeight) + BigDecimal(grossWeight)).underlying().stripTrailingZeros()
          val totalNetWeight   = (BigDecimal(netWeight) + BigDecimal(netWeight)).underlying().stripTrailingZeros()

          val json = Json
            .parse(
              s"""
                 |{
                 |   "Consignment" : {
                 |       "HouseConsignment" : [
                 |           {
                 |               "sequenceNumber" : "hc1",
                 |               "Consignor" : {
                 |                   "identificationNumber" : "identifier",
                 |                   "name" : "name"
                 |               },
                 |               "Consignee" : {
                 |                   "identificationNumber" : "identifier2",
                 |                   "name" : "name2"
                 |               },
                 |               "DepartureTransportMeans" : [
                 |                   {
                 |                       "sequenceNumber" : "56",
                 |                       "typeOfIdentification" : "2",
                 |                       "identificationNumber" : "23",
                 |                       "nationality" : "IT"
                 |                   }
                 |               ],
                 |               "ConsignmentItem" : [
                 |                   {
                 |                       "goodsItemNumber" : "6",
                 |                       "declarationGoodsItemNumber" : 100,
                 |                       "Commodity" : {
                 |                           "descriptionOfGoods" : "shirts",
                 |                           "GoodsMeasure" : {
                 |                               "grossMass" : $grossWeight,
                 |                               "netMass" : $netWeight
                 |                           }
                 |                       }
                 |                   },
                 |                   {
                 |                       "goodsItemNumber" : "6",
                 |                       "declarationGoodsItemNumber" : 100,
                 |                       "Commodity" : {
                 |                           "descriptionOfGoods" : "shirts",
                 |                           "GoodsMeasure" : {
                 |                               "grossMass" : $grossWeight,
                 |                               "netMass" : $netWeight
                 |                           }
                 |                       }
                 |                   }
                 |               ]
                 |           }
                 |       ]
                 |   }
                 |}
                 |
                 |""".stripMargin
            )
            .as[JsObject]

          val userAnswers = emptyUserAnswers.copy(ie043Data = json)

          val helper   = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
          val sections = helper.houseConsignmentSections.head.rows

          val grossWeightRow          = sections.head
          val netWeightRow            = sections(1)
          val consignorName           = sections(2)
          val consignorIdentification = sections(3)
          val consigneeName           = sections(4)
          val consigneeIdentification = sections(5)

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

          consigneeName mustBe
            SummaryListRow(
              key = Key("Consignee name".toText),
              value = Value("name2".toText)
            )

          consigneeIdentification mustBe
            SummaryListRow(
              key = Key("Consignee EORI number or Trader Identification Number (TIN)".toText),
              value = Value("identifier2".toText)
            )
        }
        s"when a consignments is defined" - {
          "and consignor identification number is not defined" in {

            val grossWeight = Gen.double.sample.value
            val netWeight   = Gen.double.sample.value

            val json = Json
              .parse(
                s"""
                   |{
                   |   "Consignment" : {
                   |       "HouseConsignment" : [
                   |           {
                   |               "sequenceNumber" : "hc1",
                   |               "Consignor" : {
                   |                   "name" : "name"
                   |           },
                   |               "ConsignmentItem" : [
                   |                   {
                   |                       "goodsItemNumber" : "6",
                   |                       "declarationGoodsItemNumber" : 100,
                   |                       "Commodity" : {
                   |                           "descriptionOfGoods" : "shirts",
                   |                           "GoodsMeasure" : {
                   |                               "grossMass" : $grossWeight,
                   |                               "netMass" : $netWeight
                   |                           }
                   |                       }
                   |                   }
                   |               ]
                   |           }
                   |       ]
                   |   }
                   |}
                   |
                   |""".stripMargin
              )
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(ie043Data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
            val sections = helper.houseConsignmentSections.head.rows

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

            val json = Json
              .parse(
                s"""
                   |{
                   |   "Consignment" : {
                   |       "HouseConsignment" : [
                   |           {
                   |               "sequenceNumber" : "hc1",
                   |               "Consignor" : {
                   |                   "name" : "name"
                   |               }
                   |           }
                   |       ]
                   |   }
                   |}
                   |
                   |""".stripMargin
              )
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(ie043Data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
            val sections = helper.houseConsignmentSections.head.rows

            val consignorName = sections.head

            consignorName mustBe
              SummaryListRow(
                key = Key("Consignor name".toText),
                value = Value("name".toText)
              )

            sections.length mustBe 1

          }
          "and only consignor identification number is defined" in {

            val json = Json
              .parse(
                s"""
                   |{
                   |   "Consignment" : {
                   |       "HouseConsignment" : [
                   |           {
                   |               "sequenceNumber" : "hc1",
                   |               "Consignor" : {
                   |                   "identificationNumber" : "identifier"
                   |               }
                   |           }
                   |       ]
                   |   }
                   |}
                   |
                   |""".stripMargin
              )
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(ie043Data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
            val sections = helper.houseConsignmentSections.head.rows

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
            val json = Json
              .parse(
                s"""
                   |{
                   |   "Consignment" : {
                   |       "HouseConsignment" : [
                   |           {
                   |               "sequenceNumber" : "hc1",
                   |               "Consignor" : {
                   |                   "identificationNumber" : "identifier"
                   |           },
                   |               "ConsignmentItem" : [
                   |                   {
                   |                       "goodsItemNumber" : "6",
                   |                       "declarationGoodsItemNumber" : 100,
                   |                       "Commodity" : {
                   |                           "descriptionOfGoods" : "shirts",
                   |                           "GoodsMeasure" : {
                   |                               "grossMass" : $grossWeight,
                   |                               "netMass" : $netWeight
                   |                           }
                   |                       }
                   |                   }
                   |               ]
                   |           }
                   |       ]
                   |   }
                   |}
                   |
                   |""".stripMargin
              )
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(ie043Data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
            val sections = helper.houseConsignmentSections.head.rows

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
          "and consignee identification number is not defined" in {

            val grossWeight = Gen.double.sample.value
            val netWeight   = Gen.double.sample.value

            val json = Json
              .parse(
                s"""
                   |{
                   |   "Consignment" : {
                   |       "HouseConsignment" : [
                   |           {
                   |               "sequenceNumber" : "hc1",
                   |               "Consignee" : {
                   |                   "name" : "name"
                   |               },
                   |               "ConsignmentItem" : [
                   |                   {
                   |                       "goodsItemNumber" : "6",
                   |                       "declarationGoodsItemNumber" : 100,
                   |                       "Commodity" : {
                   |                           "descriptionOfGoods" : "shirts",
                   |                           "GoodsMeasure" : {
                   |                               "grossMass" : $grossWeight,
                   |                               "netMass" : $netWeight
                   |                           }
                   |                       }
                   |                   }
                   |               ]
                   |           }
                   |       ]
                   |   }
                   |}
                   |
                   |""".stripMargin
              )
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(ie043Data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
            val sections = helper.houseConsignmentSections.head.rows

            val grossWeightRow = sections.head
            val netWeightRow   = sections(1)
            val consigneeName  = sections(2)

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

            consigneeName mustBe
              SummaryListRow(
                key = Key("Consignee name".toText),
                value = Value("name".toText)
              )

            sections.length mustBe 3

          }
          "and only consignee name is defined" in {

            val json = Json
              .parse(
                s"""
                   |{
                   |   "Consignment" : {
                   |       "HouseConsignment" : [
                   |           {
                   |               "sequenceNumber" : "hc1",
                   |               "Consignee" : {
                   |                   "name" : "name"
                   |               }
                   |           }
                   |       ]
                   |   }
                   |}
                   |
                   |""".stripMargin
              )
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(ie043Data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
            val sections = helper.houseConsignmentSections.head.rows

            val consigneeName = sections.head

            consigneeName mustBe
              SummaryListRow(
                key = Key("Consignee name".toText),
                value = Value("name".toText)
              )

            sections.length mustBe 1

          }
          "and only consignee identification number is defined" in {

            val json = Json
              .parse(
                s"""
                   |{
                   |   "Consignment" : {
                   |       "HouseConsignment" : [
                   |           {
                   |               "sequenceNumber" : "hc1",
                   |               "Consignee" : {
                   |                   "identificationNumber" : "identifier"
                   |               }
                   |           }
                   |       ]
                   |   }
                   |}
                   |
                   |""".stripMargin
              )
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(ie043Data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
            val sections = helper.houseConsignmentSections.head.rows

            val consigneeIdentification = sections.head

            consigneeIdentification mustBe
              SummaryListRow(
                key = Key("Consignee EORI number or Trader Identification Number (TIN)".toText),
                value = Value("identifier".toText)
              )

            sections.length mustBe 1

          }
          "and consignee name is not defined" in {

            val grossWeight = Gen.double.sample.value
            val netWeight   = Gen.double.sample.value

            val json = Json
              .parse(
                s"""
                   |{
                   |   "Consignment" : {
                   |       "HouseConsignment" : [
                   |           {
                   |               "sequenceNumber" : "hc1",
                   |               "Consignee" : {
                   |                   "identificationNumber" : "identifier"
                   |               },
                   |               "ConsignmentItem" : [
                   |                   {
                   |                       "goodsItemNumber" : "6",
                   |                       "declarationGoodsItemNumber" : 100,
                   |                       "Commodity" : {
                   |                           "descriptionOfGoods" : "shirts",
                   |                           "GoodsMeasure" : {
                   |                               "grossMass" : $grossWeight,
                   |                               "netMass" : $netWeight
                   |                           }
                   |                       }
                   |                   }
                   |               ]
                   |           }
                   |       ]
                   |   }
                   |}
                   |
                   |""".stripMargin
              )
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(ie043Data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
            val sections = helper.houseConsignmentSections.head.rows

            val grossWeightRow          = sections.head
            val netWeightRow            = sections(1)
            val consigneeIdentification = sections(2)

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

            consigneeIdentification mustBe
              SummaryListRow(
                key = Key("Consignee EORI number or Trader Identification Number (TIN)".toText),
                value = Value("identifier".toText)
              )

            sections.length mustBe 3

          }
          "and gross or net weight not defined" in {

            val json = Json
              .parse(
                s"""
                   |{
                   |   "Consignment" : {
                   |       "HouseConsignment" : [
                   |           {
                   |               "sequenceNumber" : "hc1",
                   |               "Consignor" : {
                   |                   "identificationNumber" : "identifier",
                   |                   "name" : "name"
                   |               },
                   |               "ConsignmentItem" : [
                   |                   {
                   |                       "goodsItemNumber" : "6",
                   |                       "declarationGoodsItemNumber" : 100,
                   |                       "Commodity" : {
                   |                           "descriptionOfGoods" : "shirts"
                   |                       }
                   |                   }
                   |               ]
                   |           }
                   |       ]
                   |   }
                   |}
                   |
                   |""".stripMargin
              )
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(ie043Data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
            val sections = helper.houseConsignmentSections.head.rows

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

    "totalGrossWeightRow" - {

      "must return Some(Row)" - {
        s"when total gross weight is passed to totalGrossWeightRow" in {

          val totalGrossWeight = Gen.double.sample.value

          val answers = emptyUserAnswers

          val helper = new UnloadingFindingsAnswersHelper(answers, mockReferenceDataService)
          val result = helper.totalGrossWeightRow(totalGrossWeight)

          result mustBe SummaryListRow(
            key = Key("Gross weight".toText),
            value = Value(s"${BigDecimal(totalGrossWeight)}kg".toText),
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

          val helper = new UnloadingFindingsAnswersHelper(answers, mockReferenceDataService)
          val result = helper.totalNetWeightRow(totalNetWeight)

          result mustBe SummaryListRow(
            key = Key("Net weight".toText),
            value = Value(s"${BigDecimal(totalNetWeight)}kg".toText),
            actions = None
          )
        }
      }
    }

  }

}
