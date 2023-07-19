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

package viewModels

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import services.ReferenceDataService
import viewModels.UnloadingFindingsViewModel.UnloadingFindingsViewModelProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnloadingFindingsViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]
  private val countryDesc                            = "Great Britain"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "Unloading findings sections" - {

    "must render Means of Transport section" - {
      "when there is one" in {

        val json = Json
          .parse(
            """
            |{
            |   "Consignment" : {
            |       "DepartureTransportMeans" : [
            |           {
            |               "sequenceNumber" : "dtm-1",
            |               "typeOfIdentification" : "4",
            |               "identificationNumber" : "28",
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

        setExistingUserAnswers(userAnswers)
        when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

        val viewModelProvider = new UnloadingFindingsViewModelProvider(mockReferenceDataService)
        val result            = viewModelProvider.apply(userAnswers).futureValue
        val section           = result.section.head

        section.sectionTitle.value mustBe "Departure means of transport 1"
        section.rows.size mustBe 2
        section.viewLink must not be defined
      }

      "when there is multiple" in {

        val json = Json
          .parse(
            """
              |{
              |   "Consignment" : {
              |       "DepartureTransportMeans" : [
              |           {
              |               "sequenceNumber" : "dtm-1",
              |               "typeOfIdentification" : "4",
              |               "identificationNumber" : "28",
              |               "nationality" : "GB"
              |           },
              |           {
              |               "sequenceNumber" : "dtm-1",
              |               "typeOfIdentification" : "4",
              |               "identificationNumber" : "28",
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

        setExistingUserAnswers(userAnswers)
        when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

        val viewModelProvider = new UnloadingFindingsViewModelProvider(mockReferenceDataService)
        val result            = viewModelProvider.apply(userAnswers).futureValue
        val section           = result.section(1)

        section.sectionTitle.value mustBe "Departure means of transport 2"
        section.rows.size mustBe 2
        section.viewLink must not be defined
      }
    }

    "must render transport equipment section" - {
      "when there is one" - {

        "with no seals" in {

          val json = Json
            .parse(
              """
                |{
                |   "Consignment" : {
                |       "TransportEquipment" : [
                |           {
                |               "sequenceNumber" : "te1",
                |               "containerIdentificationNumber" : "cin-1"
                |           }
                |       ]
                |   }
                |}
                |
                |""".stripMargin
            )
            .as[JsObject]

          val userAnswers = emptyUserAnswers.copy(ie043Data = json)

          setExistingUserAnswers(userAnswers)
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val viewModelProvider = new UnloadingFindingsViewModelProvider(mockReferenceDataService)
          val result            = viewModelProvider.apply(userAnswers).futureValue
          val section           = result.section.head

          section.sectionTitle.value mustBe "Transport equipment 1"
          section.rows.size mustBe 1
          section.viewLink must not be defined
        }

        "with seals" in {

          val json = Json
            .parse(
              """
                |{
                |   "Consignment" : {
                |       "TransportEquipment" : [
                |           {
                |               "sequenceNumber" : "te1",
                |               "containerIdentificationNumber" : "cin-1",
                |               "numberOfSeals" : 103,
                |               "Seal" : [
                |                   {
                |                       "sequenceNumber" : "1001",
                |                       "identifier" : "1002"
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

          setExistingUserAnswers(userAnswers)
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val viewModelProvider = new UnloadingFindingsViewModelProvider(mockReferenceDataService)
          val result            = viewModelProvider.apply(userAnswers).futureValue
          val section           = result.section.head

          section.sectionTitle.value mustBe "Transport equipment 1"
          section.rows.size mustBe 2
          section.viewLink must not be defined
        }
      }
      "when there is multiple" - {

        "with no seals" in {

          val json = Json
            .parse(
              """
                |{
                |   "Consignment" : {
                |       "TransportEquipment" : [
                |           {
                |               "sequenceNumber" : "te1",
                |               "containerIdentificationNumber" : "cin-1"
                |           },
                |           {
                |               "sequenceNumber" : "te1",
                |               "containerIdentificationNumber" : "cin-1"
                |           }
                |       ]
                |   }
                |}
                |
                |""".stripMargin
            )
            .as[JsObject]

          val userAnswers = emptyUserAnswers.copy(ie043Data = json)

          setExistingUserAnswers(userAnswers)
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val viewModelProvider = new UnloadingFindingsViewModelProvider(mockReferenceDataService)
          val result            = viewModelProvider.apply(userAnswers).futureValue
          val section           = result.section(1)

          section.sectionTitle.value mustBe "Transport equipment 2"
          section.rows.size mustBe 1
          section.viewLink must not be defined
        }

        "with seals" in {

          val json = Json
            .parse(
              """
                |{
                |   "Consignment" : {
                |       "TransportEquipment" : [
                |           {
                |               "sequenceNumber" : "te1",
                |               "containerIdentificationNumber" : "cin-1",
                |               "numberOfSeals" : 103,
                |               "Seal" : [
                |                   {
                |                       "sequenceNumber" : "1001",
                |                       "identifier" : "1002"
                |                   }
                |               ]
                |           },
                |           {
                |               "sequenceNumber" : "te1",
                |               "containerIdentificationNumber" : "cin-1",
                |               "numberOfSeals" : 103,
                |               "Seal" : [
                |                   {
                |                       "sequenceNumber" : "1001",
                |                       "identifier" : "1002"
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

          setExistingUserAnswers(userAnswers)
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val viewModelProvider = new UnloadingFindingsViewModelProvider(mockReferenceDataService)
          val result            = viewModelProvider.apply(userAnswers).futureValue
          val section           = result.section(1)

          section.sectionTitle.value mustBe "Transport equipment 2"
          section.rows.size mustBe 2
          section.viewLink must not be defined
        }
      }
    }

    "must render house consignment sections" - {
      "when there is one" in {

        val json = Json
          .parse(
            """
            |{
            |   "Consignment" : {
            |       "HouseConsignment" : [
            |           {
            |               "sequenceNumber" : "hc1",
            |               "Consignor" : {
            |                   "identificationNumber" : "csgr1",
            |                   "name" : "michael doe"
            |               },
            |               "Consignee" : {
            |                   "identificationNumber" : "csgee1",
            |                   "name" : "John Smith"
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
            |                               "grossMass" : 123.45,
            |                               "netMass" : 123.45
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

        setExistingUserAnswers(userAnswers)
        when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

        val viewModelProvider = new UnloadingFindingsViewModelProvider(mockReferenceDataService)
        val result            = viewModelProvider.apply(userAnswers).futureValue
        val section           = result.section.head

        section.sectionTitle.value mustBe "House consignment 1"
        section.rows.size mustBe 6
        section.viewLink mustBe defined
      }
      "when there is multiple" in {
        val json = Json
          .parse(
            """
            |{
            |   "Consignment" : {
            |       "HouseConsignment" : [
            |           {
            |               "sequenceNumber" : "hc1",
            |               "Consignor" : {
            |                   "identificationNumber" : "csgr1",
            |                   "name" : "michael doe"
            |               },
            |               "Consignee" : {
            |                   "identificationNumber" : "csgee1",
            |                   "name" : "John Smith"
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
            |                               "grossMass" : 123.45,
            |                               "netMass" : 123.45
            |                           }
            |                       }
            |                   }
            |               ]
            |           },
            |           {
            |               "sequenceNumber" : "hc1",
            |               "Consignor" : {
            |                   "identificationNumber" : "csgr1",
            |                   "name" : "michael doe"
            |               },
            |               "Consignee" : {
            |                   "identificationNumber" : "csgee1",
            |                   "name" : "John Smith"
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
            |                               "grossMass" : 123.45,
            |                               "netMass" : 123.45
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

        setExistingUserAnswers(userAnswers)
        when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

        val viewModelProvider = new UnloadingFindingsViewModelProvider(mockReferenceDataService)
        val result            = viewModelProvider.apply(userAnswers).futureValue
        val section           = result.section(1)

        section.sectionTitle.value mustBe "House consignment 2"
        section.rows.size mustBe 6
        section.viewLink mustBe defined
      }
    }
  }
}
