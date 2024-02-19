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
import models.Index
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.Country
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{DepartureTransportMeansCountryPage, DepartureTransportMeansIdentificationNumberPage, DepartureTransportMeansIdentificationTypePage}
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{JsObject, Json}
import viewModels.HouseConsignmentViewModel.HouseConsignmentViewModelProvider
import viewModels.sections.Section.{AccordionSection, StaticSection}

class HouseConsignmentViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val country                     = Country("GB", "United Kingdom")
  private val vehicleIdentificationNumber = "12324"
  private val identificationType          = TransportMeansIdentification("51", "description")

  "HouseConsignmentViewModel" - {

    "must render Means of Transport section" - {
      "when there is one" in {

        val answers = emptyUserAnswers
          .setValue(DepartureTransportMeansIdentificationNumberPage(Index(0), index), vehicleIdentificationNumber)
          .setValue(DepartureTransportMeansIdentificationTypePage(Index(0), index), identificationType)
          .setValue(DepartureTransportMeansCountryPage(Index(0), index), country)

        setExistingUserAnswers(answers)

        val viewModelProvider = new HouseConsignmentViewModelProvider()
        val result            = viewModelProvider.apply(answers, index)
        val section           = result.sections.head

        section.sectionTitle.value mustBe "Departure means of transport 1"
        section.rows.size mustBe 2
        section.viewLink must not be defined
      }

      "when there is multiple" in {

        val answers = emptyUserAnswers
          .setValue(DepartureTransportMeansIdentificationNumberPage(hcIndex, Index(0)), vehicleIdentificationNumber)
          .setValue(DepartureTransportMeansIdentificationTypePage(hcIndex, Index(0)), identificationType)
          .setValue(DepartureTransportMeansCountryPage(hcIndex, Index(0)), country)
          .setValue(DepartureTransportMeansIdentificationNumberPage(hcIndex, Index(1)), vehicleIdentificationNumber)
          .setValue(DepartureTransportMeansIdentificationTypePage(hcIndex, Index(1)), identificationType)
          .setValue(DepartureTransportMeansCountryPage(hcIndex, Index(1)), country)

        setExistingUserAnswers(answers)

        val viewModelProvider = new HouseConsignmentViewModelProvider()
        val result            = viewModelProvider.apply(answers, index)
        val section1          = result.sections.head
        val section2          = result.sections(1)

        section1.sectionTitle.value mustBe "Departure means of transport 1"
        section1.rows.size mustBe 2
        section1.viewLink must not be defined

        section2.sectionTitle.value mustBe "Departure means of transport 2"
        section2.rows.size mustBe 2
        section2.viewLink must not be defined
      }
    }

    "must render house consignment section" - {
      "when there is one" in {

        val json: JsObject = Json
          .parse("""
                   | {
                   |    "Consignment" : {
                   |      "HouseConsignment" : [
                   |        {
                   |          "Consignor" : {
                   |              "identificationNumber" : "csgr1",
                   |              "name" : "michael doe"
                   |          },
                   |          "Consignee" : {
                   |              "identificationNumber" : "csgee1",
                   |              "name" : "John Smith"
                   |          },
                   |          "ConsignmentItem" : [
                   |              {
                   |                  "goodsItemNumber" : "6",
                   |                  "declarationGoodsItemNumber" : 100,
                   |                  "Commodity" : {
                   |                      "descriptionOfGoods" : "shirts",
                   |                      "GoodsMeasure" : {
                   |                          "grossMass" : 123.45,
                   |                          "netMass" : 123.45
                   |                      }
                   |                  }
                   |              }
                   |          ]
                   |        }
                   |      ]
                   |    }
                   |}
                   |""".stripMargin)
          .as[JsObject]

        val userAnswers = emptyUserAnswers.copy(data = json)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new HouseConsignmentViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers, index)

        result.sections.head mustBe a[AccordionSection]
        result.sections.head.sectionTitle.value mustBe "Item 1"
        result.sections.head.rows.size mustBe 3

        result.sections(1) mustBe a[StaticSection]
        result.sections(1).sectionTitle must not be defined
        result.sections(1).rows.size mustBe 6
      }
    }

    "must render item section" - {
      "when there is one" in {

        val json: JsObject = Json
          .parse("""
                   | {
                   |    "Consignment" : {
                   |      "HouseConsignment" : [
                   |        {
                   |          "ConsignmentItem" : [
                   |           {
                   |               "Commodity" : {
                   |                   "descriptionOfGoods" : "shirts",
                   |                   "GoodsMeasure" : {
                   |                       "grossMass" : 123.45,
                   |                       "netMass" : 123.45
                   |                   }
                   |               }
                   |           }
                   |          ]
                   |        }
                   |      ]
                   |    }
                   |}
                   |""".stripMargin)
          .as[JsObject]

        val userAnswers = emptyUserAnswers.copy(data = json)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new HouseConsignmentViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers, index)
        val section           = result.sections.head

        section.sectionTitle.value mustBe "Item 1"
        section.rows.size mustBe 3
      }

      "when there are multiple" in {

        val json: JsObject = Json
          .parse("""
                   | {
                   |    "Consignment" : {
                   |      "HouseConsignment" : [
                   |        {
                   |          "ConsignmentItem" : [
                   |           {
                   |               "Commodity" : {
                   |                   "descriptionOfGoods" : "shirts",
                   |                   "GoodsMeasure" : {
                   |                       "grossMass" : 123.45,
                   |                       "netMass" : 123.45
                   |                   }
                   |               }
                   |           },
                   |           {
                   |               "Commodity" : {
                   |                   "descriptionOfGoods" : "shirts",
                   |                   "GoodsMeasure" : {
                   |                       "grossMass" : 123.45,
                   |                       "netMass" : 123.45
                   |                   }
                   |               }
                   |           }
                   |          ]
                   |        }
                   |      ]
                   |    }
                   |}
                   |""".stripMargin)
          .as[JsObject]

        val userAnswers = emptyUserAnswers.copy(data = json)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new HouseConsignmentViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers, index)

        result.sections.head mustBe a[AccordionSection]
        result.sections.head.sectionTitle.value mustBe "Item 1"
        result.sections.head.rows.size mustBe 3

        result.sections(1) mustBe a[AccordionSection]
        result.sections(1).sectionTitle.value mustBe "Item 2"
        result.sections(1).rows.size mustBe 3

        result.sections(2) mustBe a[StaticSection]
        result.sections(2).sectionTitle must not be defined
        result.sections(2).rows.size mustBe 2
      }
    }
  }
}
