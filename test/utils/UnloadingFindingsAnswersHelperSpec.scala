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
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.{AdditionalReferenceType, Country}
import models.{Identification, Index}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import pages.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import pages.departureMeansOfTransport.{CountryPage, TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import play.api.libs.json.{JsObject, Json}
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnloadingFindingsAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  private val countryDesc = "United Kingdom"

  private def containerIndicatorAction(index: Int) = Some(
    Actions(
      "",
      List(
        ActionItem(
          "#",
          Text("Change"),
          Some("container identification number"),
          "",
          Map("id" -> s"change-container-identification-number-$index")
        )
      )
    )
  )

  private def sealsAction(index: Int) = Some(
    Actions(
      "",
      List(
        ActionItem(
          "#",
          Text("Change"),
          Some("seal identification number"),
          "",
          Map("id" -> s"change-seal-details-$index")
        )
      )
    )
  )

  private val countryAction = Some(
    Actions(
      "",
      List(
        ActionItem(
          "#",
          Text("Change"),
          Some("registered country for the departure means of transport"),
          "",
          Map("id" -> "change-registered-country")
        )
      )
    )
  )

  def identificationTypeAction(index: Int) =
    Some(
      Actions(
        "",
        List(
          ActionItem(
            "#",
            Text("Change"),
            Some("identification type for the departure means of transport"),
            "",
            Map("id" -> s"change-transport-means-identification-$index")
          )
        )
      )
    )

  def identificationNumberAction(index: Int) =
    Some(
      Actions(
        "",
        List(
          ActionItem(
            "#",
            Text("Change"),
            Some("identification number for the departure means of transport"),
            "",
            Map("id" -> s"change-transport-means-identification-number-$index")
          )
        )
      )
    )

  "UnloadingFindingsAnswersHelper" - {

    "buildTransportSections" - {
      "must return None" - {
        s"when no transport means defined" in {
          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.buildTransportSections.futureValue
          result.isEmpty mustBe true
        }
      }

      "must return Some(Row)s" - {
        val vehicleIdentificationNumber = Gen.alphaNumStr.sample.value
        val vehicleIdentificationType   = Gen.oneOf(Identification.values).sample.value

        s"when there is 1 transport means section defined" in {

          val answers = emptyUserAnswers
            .setValue(VehicleIdentificationNumberPage(index), vehicleIdentificationNumber)
            .setValue(TransportMeansIdentificationPage(index),
                      TransportMeansIdentification(vehicleIdentificationType.identificationType.toString, "description")
            )
            .setValue(CountryPage(index), Country("GB", "United Kingdom"))

          val helper          = new UnloadingFindingsAnswersHelper(answers)
          val result          = helper.buildTransportSections.futureValue
          val transportMeans1 = result.head.rows

          val transportMeansIDRow      = transportMeans1.head
          val transportMeansNumberRow  = transportMeans1(1)
          val transportMeansCountryRow = transportMeans1(2)

          transportMeansIDRow mustBe
            SummaryListRow(
              key = Key("Identification type".toText),
              value = Value("description".toText),
              actions = identificationTypeAction(1)
            )

          transportMeansNumberRow mustBe
            SummaryListRow(
              key = Key("Identification number".toText),
              value = Value(vehicleIdentificationNumber.toText),
              actions = identificationNumberAction(1)
            )

          transportMeansCountryRow mustBe
            SummaryListRow(
              key = Key("Registered country".toText),
              value = Value(countryDesc.toText),
              actions = countryAction
            )
        }
        s"when only identification type and number defined" in {

          val answers = emptyUserAnswers
            .setValue(VehicleIdentificationNumberPage(index), vehicleIdentificationNumber)
            .setValue(TransportMeansIdentificationPage(index), TransportMeansIdentification("41", "description"))

          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper          = new UnloadingFindingsAnswersHelper(answers)
          val result          = helper.buildTransportSections.futureValue
          val transportMeans1 = result.head.rows

          val transportMeansIDRow     = transportMeans1.head
          val transportMeansNumberRow = transportMeans1(1)

          transportMeans1.length mustBe 2

          transportMeansIDRow mustBe
            SummaryListRow(
              key = Key("Identification type".toText),
              value = Value("description".toText),
              actions = identificationTypeAction(1)
            )

          transportMeansNumberRow mustBe
            SummaryListRow(
              key = Key("Identification number".toText),
              value = Value(vehicleIdentificationNumber.toText),
              actions = identificationNumberAction(1)
            )

        }
        s"when only country is defined" in {

          val answers = emptyUserAnswers
            .setValue(CountryPage(index), Country("GB", "United Kingdom"))

          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper          = new UnloadingFindingsAnswersHelper(answers)
          val result          = helper.buildTransportSections.futureValue
          val transportMeans1 = result.head.rows

          val transportMeansCountryRow1 = transportMeans1.head

          transportMeans1.length mustBe 1

          transportMeansCountryRow1 mustBe
            SummaryListRow(
              key = Key("Registered country".toText),
              value = Value(countryDesc.toText),
              actions = countryAction
            )

        }
        s"when only number is defined" in {

          val answers = emptyUserAnswers
            .setValue(VehicleIdentificationNumberPage(index), vehicleIdentificationNumber)

          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper          = new UnloadingFindingsAnswersHelper(answers)
          val result          = helper.buildTransportSections.futureValue
          val transportMeans1 = result.head.rows.head

          transportMeans1 mustBe
            SummaryListRow(
              key = Key("Identification number".toText),
              value = Value(vehicleIdentificationNumber.toText),
              actions = identificationNumberAction(1)
            )

        }
        s"when multiple transport means sections are defined" in {

          val answers = emptyUserAnswers
            .setValue(VehicleIdentificationNumberPage(Index(0)), vehicleIdentificationNumber)
            .setValue(
              TransportMeansIdentificationPage(Index(0)),
              TransportMeansIdentification(vehicleIdentificationType.identificationType.toString, vehicleIdentificationNumber)
            )
            .setValue(CountryPage(Index(0)), Country("GB", "United Kingdom"))
            .setValue(VehicleIdentificationNumberPage(Index(1)), vehicleIdentificationNumber)
            .setValue(
              TransportMeansIdentificationPage(Index(1)),
              TransportMeansIdentification(vehicleIdentificationType.identificationType.toString, vehicleIdentificationNumber)
            )
            .setValue(CountryPage(Index(1)), Country("GB", "United Kingdom"))

          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val helper          = new UnloadingFindingsAnswersHelper(answers)
          val result          = helper.buildTransportSections.futureValue
          val transportMeans1 = result.head.rows
          val transportMeans2 = result(1).rows

          val transportMeansIDRow1      = transportMeans1.head
          val transportMeansNumberRow1  = transportMeans1(1)
          val transportMeansCountryRow1 = transportMeans1(2)
          val transportMeansIDRow2      = transportMeans2.head
          val transportMeansNumberRow2  = transportMeans2(1)
          val transportMeansCountryRow2 = transportMeans2(2)

          transportMeansIDRow1 mustBe
            SummaryListRow(
              key = Key("Identification type".toText),
              value = Value(vehicleIdentificationNumber.toText),
              actions = identificationTypeAction(1)
            )

          transportMeansNumberRow1 mustBe
            SummaryListRow(
              key = Key("Identification number".toText),
              value = Value(vehicleIdentificationNumber.toText),
              actions = identificationNumberAction(1)
            )

          transportMeansCountryRow1 mustBe
            SummaryListRow(
              key = Key("Registered country".toText),
              value = Value(countryDesc.toText),
              actions = countryAction
            )

          transportMeansIDRow2 mustBe
            SummaryListRow(
              key = Key("Identification type".toText),
              value = Value(vehicleIdentificationNumber.toText),
              actions = identificationTypeAction(2)
            )

          transportMeansNumberRow2 mustBe
            SummaryListRow(
              key = Key("Identification number".toText),
              value = Value(vehicleIdentificationNumber.toText),
              actions = identificationNumberAction(2)
            )

          transportMeansCountryRow2 mustBe
            SummaryListRow(
              key = Key("Registered country".toText),
              value = Value(countryDesc.toText),
              actions = countryAction
            )
        }
      }
    }

    "transportMeansID" - {

      val vehicleIdentificationNumber = Gen.alphaNumStr.sample.value

      "must return None" - {
        s"when $VehicleIdentificationNumberPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          helper.transportMeansID(index) mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $VehicleIdentificationNumberPage defined" in {

          val answers = emptyUserAnswers
            .setValue(VehicleIdentificationNumberPage(index), vehicleIdentificationNumber)

          val helper          = new UnloadingFindingsAnswersHelper(answers)
          val result          = helper.buildTransportSections.futureValue
          val transportMeans1 = result.head.rows.head

          transportMeans1 mustBe
            SummaryListRow(
              key = Key("Identification number".toText),
              value = Value(vehicleIdentificationNumber.toText),
              actions = identificationNumberAction(1)
            )
        }
      }
    }

    "transportRegisteredCountry" - {

      "must return Some(Row)" - {
        s"when $CountryPage defined" in {
          val country = Country("GB", "United Kingdom")
          val answers = emptyUserAnswers
            .setValue(CountryPage(index), country)

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.transportRegisteredCountry(country)

          result mustBe
            SummaryListRow(
              key = Key("Registered country".toText),
              value = Value(countryDesc.toText),
              actions = countryAction
            )
        }
      }
    }

    "containerIdentificationNumber" - {

      val containerIdentificationNumber: String = Gen.alphaNumStr.sample.value

      "must return None" - {
        s"when $ContainerIdentificationNumberPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
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

          val userAnswers = emptyUserAnswers.copy(data = json)

          val helper = new UnloadingFindingsAnswersHelper(userAnswers)
          val result = helper.containerIdentificationNumber(index)

          result mustBe Some(
            SummaryListRow(
              key = Key("Container identification number".toText),
              value = Value(containerIdentificationNumber.toText),
              actions = containerIndicatorAction(index.display)
            )
          )
        }
      }
    }

    "transportEquipmentSections" - {
      "must return None" - {
        s"when no transport equipments defined" in {
          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
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

          val userAnswers = emptyUserAnswers.copy(data = json)

          val helper              = new UnloadingFindingsAnswersHelper(userAnswers)
          val transportEquipment1 = helper.transportEquipmentSections.head.rows

          val sealRow1 = transportEquipment1.head

          sealRow1 mustBe
            SummaryListRow(key = Key("Seal 1".toText), value = Value("seal1".toText), actions = sealsAction(index.display))

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

          val userAnswers = emptyUserAnswers.copy(data = json)

          val helper              = new UnloadingFindingsAnswersHelper(userAnswers)
          val transportEquipment1 = helper.transportEquipmentSections.head.rows

          val containerRow1 = transportEquipment1.head

          containerRow1 mustBe
            SummaryListRow(
              key = Key("Container identification number".toText),
              value = Value("container1".toText),
              actions = containerIndicatorAction(index.display)
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

          val userAnswers = emptyUserAnswers.copy(data = json)

          val helper              = new UnloadingFindingsAnswersHelper(userAnswers)
          val transportEquipment1 = helper.transportEquipmentSections.head.rows

          val containerRow1 = transportEquipment1.head
          val sealRow1      = transportEquipment1(1)

          containerRow1 mustBe
            SummaryListRow(
              key = Key("Container identification number".toText),
              value = Value("container1".toText),
              actions = containerIndicatorAction(index.display)
            )

          sealRow1 mustBe
            SummaryListRow(
              key = Key("Seal 1".toText),
              value = Value("seal1".toText),
              actions = sealsAction(index.display)
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

          val userAnswers = emptyUserAnswers.copy(data = json)

          val helper              = new UnloadingFindingsAnswersHelper(userAnswers)
          val transportEquipment1 = helper.transportEquipmentSections.head.rows
          val transportEquipment2 = helper.transportEquipmentSections(1).rows

          val containerRow1 = transportEquipment1.head
          val sealRow1      = transportEquipment1(1)
          val containerRow2 = transportEquipment2.head
          val sealRow2      = transportEquipment2(1)

          containerRow1 mustBe
            SummaryListRow(
              key = Key("Container identification number".toText),
              value = Value("container1".toText),
              actions = containerIndicatorAction(1)
            )

          sealRow1 mustBe
            SummaryListRow(
              key = Key("Seal 1".toText),
              value = Value("seal1".toText),
              actions = sealsAction(1)
            )

          containerRow2 mustBe
            SummaryListRow(
              key = Key("Container identification number".toText),
              value = Value("container2".toText),
              actions = containerIndicatorAction(2)
            )

          sealRow2 mustBe
            SummaryListRow(
              key = Key("Seal 1".toText),
              value = Value("seal2".toText),
              actions = sealsAction(1)
            )
        }
      }
    }

    "transportEquipmentSeal" - {

      val sealIdentifier = Gen.alphaNumStr.sample.value

      "must return None" - {
        s"when $SealIdentificationNumberPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.transportEquipmentSeal(equipmentIndex, sealIndex)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $SealIdentificationNumberPage defined" in {

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

          val userAnswers = emptyUserAnswers.copy(data = json)

          val helper = new UnloadingFindingsAnswersHelper(userAnswers)
          val result = helper.transportEquipmentSeal(equipmentIndex, sealIndex)

          result mustBe Some(
            SummaryListRow(
              key = Key(s"Seal ${sealIndex.display}".toText),
              value = Value(sealIdentifier.toText),
              actions = sealsAction(index.display)
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

          val userAnswers = emptyUserAnswers.copy(data = json)

          val helper   = new UnloadingFindingsAnswersHelper(userAnswers)
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

            val userAnswers = emptyUserAnswers.copy(data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers)
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

            val userAnswers = emptyUserAnswers.copy(data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers)
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

            val userAnswers = emptyUserAnswers.copy(data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers)
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

            val userAnswers = emptyUserAnswers.copy(data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers)
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

            val userAnswers = emptyUserAnswers.copy(data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers)
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

            val userAnswers = emptyUserAnswers.copy(data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers)
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

            val userAnswers = emptyUserAnswers.copy(data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers)
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

            val userAnswers = emptyUserAnswers.copy(data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers)
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

            val userAnswers = emptyUserAnswers.copy(data = json)

            val helper   = new UnloadingFindingsAnswersHelper(userAnswers)
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

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.totalGrossWeightRow(totalGrossWeight)

          result mustBe SummaryListRow(
            key = Key("Gross weight".toText),
            value = Value(s"${BigDecimal(totalGrossWeight)}kg".toText)
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
            key = Key("Net weight".toText),
            value = Value(s"${BigDecimal(totalNetWeight)}kg".toText)
          )
        }
      }
    }

    "additionalReference" - {
      "must return None" - {
        "when additionalReference is undefined" in {

          val answers = emptyUserAnswers

          val helper = new UnloadingFindingsAnswersHelper(answers, mockReferenceDataService)
          val result = helper.additionalReference(index)
          result mustBe None

        }
      }

      "must return Some(Row)" - {
        "when additionalReference is defined" in {
          forAll(arbitrary[AdditionalReferenceType], Gen.alphaNumStr) {
            (addRef, addRefNumber) =>
              val userAnswers = emptyUserAnswers
                .setValue(AdditionalReferenceTypePage(index), addRef)
                .setValue(AdditionalReferenceNumberPage(index), addRefNumber)
              val helper = new UnloadingFindingsAnswersHelper(userAnswers, mockReferenceDataService)
              val result = helper.additionalReference(index).get

              result.key.value mustBe "Additional Reference 1"
              result.value.value mustBe s"${addRef.documentType} - ${addRef.description} - $addRefNumber"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe "#"
              action.visuallyHiddenText.get mustBe "additional reference 1"
              action.id mustBe "change-additional-reference-1"
          }
        }
      }
    }

  }

}
