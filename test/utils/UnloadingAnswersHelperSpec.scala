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
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

class UnloadingAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators with RowActions {

  "UnloadingAnswersHelper" - {

    "consignorName" - {
      "must return None" - {
        s"when consignorName is not defined" in {
          val helper = new UnloadingAnswersHelper(emptyUserAnswers)
          val result = helper.consignorName(index)
          result.isEmpty mustBe true
        }
      }

      "must return Some(Row)s" - {
        "when consignorName is defined" in {

          val json: JsObject = Json
            .parse(s"""
                 | {
                 |    "Consignment" : {
                 |      "HouseConsignment" : [
                 |        {
                 |          "Consignor" : {
                 |              "name" : "john doe"
                 |          }
                 |        }
                 |      ]
                 |    }
                 |}
                 |""".stripMargin)
            .as[JsObject]

          val userAnswers   = emptyUserAnswers.copy(data = json)
          val helper        = new UnloadingAnswersHelper(userAnswers)
          val consignorName = helper.consignorName(index).head

          consignorName mustBe
            SummaryListRow(
              key = Key("Consignor name".toText),
              value = Value("john doe".toText)
            )

        }
      }
    }

    "consignorIdentification" - {
      "must return None" - {
        s"when consignorIdentification is not defined" in {
          val helper = new UnloadingAnswersHelper(emptyUserAnswers)
          val result = helper.consignorIdentification(index)
          result.isEmpty mustBe true
        }
      }

      "must return Some(Row)s" - {
        "when consignorIdentification is defined" in {
          val json: JsObject = Json
            .parse(s"""
                 | {
                 |    "Consignment" : {
                 |      "HouseConsignment" : [
                 |        {
                 |          "Consignor" : {
                 |              "identificationNumber" : "csgor1"
                 |          }
                 |        }
                 |      ]
                 |    }
                 |}
                 |""".stripMargin)
            .as[JsObject]

          val userAnswers = emptyUserAnswers.copy(data = json)

          val helper        = new UnloadingAnswersHelper(userAnswers)
          val consignorName = helper.consignorIdentification(index).head

          consignorName mustBe
            SummaryListRow(
              key = Key("Consignor EORI number or Trader Identification Number (TIN)".toText),
              value = Value("csgor1".toText)
            )

        }
      }
    }

    "houseConsignmentTotalWeightRows" - {
      "must return None" - {
        s"when no weights are defined" in {
          val helper = new UnloadingAnswersHelper(emptyUserAnswers)
          val result = helper.houseConsignmentTotalWeightRows(index)
          result.isEmpty mustBe true
        }
      }

      "must return Some(Row)s" - {

        val grossWeight           = Gen.double.sample.value
        val grossWeightBigDecimal = BigDecimal.valueOf(grossWeight)
        val netWeight             = Gen.double.sample.value
        val netWeightBigDecimal   = BigDecimal.valueOf(netWeight)

        "when gross and net weight are defined" in {

          val json: JsObject = Json
            .parse(s"""
                 | {
                 |    "Consignment" : {
                 |      "HouseConsignment" : [
                 |        {
                 |          "ConsignmentItem" : [
                 |              {
                 |                  "Commodity" : {
                 |                      "GoodsMeasure" : {
                 |                          "grossMass" : $grossWeight,
                 |                          "netMass" : $netWeight
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

          val helper = new UnloadingAnswersHelper(userAnswers)
          val rows   = helper.houseConsignmentTotalWeightRows(index)

          rows.head mustBe
            SummaryListRow(
              key = Key("Gross weight".toText),
              value = Value(s"${grossWeightBigDecimal}kg".toText),
              actions = grossWeightAction
            )

          rows(1) mustBe
            SummaryListRow(
              key = Key("Net weight".toText),
              value = Value(s"${netWeightBigDecimal}kg".toText),
              actions = netWeightAction
            )

        }
        "when only gross weight is defined" in {

          val json: JsObject = Json
            .parse(s"""
                 | {
                 |    "Consignment" : {
                 |      "HouseConsignment" : [
                 |        {
                 |          "ConsignmentItem" : [
                 |              {
                 |                  "Commodity" : {
                 |                      "GoodsMeasure" : {
                 |                          "grossMass" : $grossWeight
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

          val helper = new UnloadingAnswersHelper(userAnswers)
          val rows   = helper.houseConsignmentTotalWeightRows(index)

          rows.head mustBe
            SummaryListRow(
              key = Key("Gross weight".toText),
              value = Value(s"${grossWeightBigDecimal}kg".toText),
              actions = grossWeightAction
            )

          rows.length mustBe 1
        }
        "when only net weight is defined" in {

          val json: JsObject = Json
            .parse(s"""
                 | {
                 |    "Consignment" : {
                 |      "HouseConsignment" : [
                 |        {
                 |          "ConsignmentItem" : [
                 |              {
                 |                  "Commodity" : {
                 |                      "GoodsMeasure" : {
                 |                          "netMass" : $netWeight
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
          val helper      = new UnloadingAnswersHelper(userAnswers)
          val rows        = helper.houseConsignmentTotalWeightRows(index)

          rows.head mustBe
            SummaryListRow(
              key = Key("Net weight".toText),
              value = Value(s"${netWeightBigDecimal}kg".toText),
              actions = netWeightAction
            )

          rows.length mustBe 1
        }

        "when the values of gross/net weight are both 0kg" in {
          val json: JsObject = Json
            .parse(s"""
                 | {
                 |    "Consignment" : {
                 |      "HouseConsignment" : [
                 |        {
                 |          "ConsignmentItem" : [
                 |              {
                 |                  "Commodity" : {
                 |                      "GoodsMeasure" : {
                 |                          "grossMass" : 0.00,
                 |                          "netMass" : 0.00
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

          val helper = new UnloadingAnswersHelper(userAnswers)
          val rows   = helper.houseConsignmentTotalWeightRows(index)

          rows.isEmpty mustBe true
        }
      }
    }

    "fetchWeightValues" - {
      "must return (Option[BigDecimal], Option[BigDecimal]) when gross and net weight defined" in {
        val grossWeight = Gen.double.sample.value
        val netWeight   = Gen.double.sample.value

        val json: JsObject = Json
          .parse(s"""
               | {
               |    "Consignment" : {
               |      "HouseConsignment" : [
               |        {
               |          "ConsignmentItem" : [
               |              {
               |                  "Commodity" : {
               |                      "GoodsMeasure" : {
               |                          "grossMass" : $grossWeight,
               |                          "netMass" : $netWeight
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

        val helper = new UnloadingAnswersHelper(userAnswers)
        val result = helper.fetchWeightValues(index, index)

        result mustBe (Some(BigDecimal.valueOf(grossWeight)), Some(BigDecimal.valueOf(netWeight)))

      }
      "must return (Option[BigDecimal], None) when only gross weight is defined" in {
        val grossWeight = Gen.double.sample.value

        val json: JsObject = Json
          .parse(s"""
               | {
               |    "Consignment" : {
               |      "HouseConsignment" : [
               |        {
               |          "ConsignmentItem" : [
               |              {
               |                  "Commodity" : {
               |                      "GoodsMeasure" : {
               |                          "grossMass" : $grossWeight
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

        val helper = new UnloadingAnswersHelper(userAnswers)
        val result = helper.fetchWeightValues(index, index)

        result mustBe (Some(BigDecimal.valueOf(grossWeight)), None)

      }
      "must return (None, Option[BigDecimal]) when only net weight is defined" in {
        val netWeight = Gen.double.sample.value

        val json: JsObject = Json
          .parse(s"""
               | {
               |    "Consignment" : {
               |      "HouseConsignment" : [
               |        {
               |          "ConsignmentItem" : [
               |              {
               |                  "Commodity" : {
               |                      "GoodsMeasure" : {
               |                          "netMass" : $netWeight
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
        val helper      = new UnloadingAnswersHelper(userAnswers)
        val result      = helper.fetchWeightValues(index, index)

        result mustBe (None, Some(BigDecimal.valueOf(netWeight)))

      }
      "must return (None, None) when no weights are defined" in {
        val answers = emptyUserAnswers

        val helper = new UnloadingAnswersHelper(answers)
        val result = helper.fetchWeightValues(index, index)

        result mustBe (None, None)

      }
    }

    "grossWeightRow" - {
      "must return None" - {
        s"when grossWeightRow is not defined" in {
          val helper = new UnloadingAnswersHelper(emptyUserAnswers)
          val result = helper.grossWeightRow(index, index)
          result.isEmpty mustBe true
        }
      }

      "must return Some(Row)s" - {
        "when grossWeightRow is defined" in {

          val grossWeight           = Gen.double.sample.value
          val grossWeightBigDecimal = BigDecimal.valueOf(grossWeight)

          val json: JsObject = Json
            .parse(s"""
                 | {
                 |    "Consignment" : {
                 |      "HouseConsignment" : [
                 |        {
                 |          "ConsignmentItem" : [
                 |              {
                 |                  "Commodity" : {
                 |                      "GoodsMeasure" : {
                 |                          "grossMass" : $grossWeight
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

          val helper         = new UnloadingAnswersHelper(userAnswers)
          val grossWeightRow = helper.grossWeightRow(index, index).head

          grossWeightRow mustBe
            SummaryListRow(
              key = Key("Gross weight".toText),
              value = Value(s"${grossWeightBigDecimal}kg".toText)
            )

        }
      }
    }

    "netWeightRow" - {
      "must return None" - {
        s"when netWeightRow is not defined" in {
          val helper = new UnloadingAnswersHelper(emptyUserAnswers)
          val result = helper.netWeightRow(index, index)
          result.isEmpty mustBe true
        }
      }

      "must return Some(Row)s" - {
        "when netWeightRow is defined" in {

          val netWeight           = Gen.double.sample.value
          val netWeightBigDecimal = BigDecimal.valueOf(netWeight)

          val json: JsObject = Json
            .parse(s"""
                 | {
                 |    "Consignment" : {
                 |      "HouseConsignment" : [
                 |        {
                 |          "ConsignmentItem" : [
                 |              {
                 |                  "Commodity" : {
                 |                      "GoodsMeasure" : {
                 |                          "netMass" : $netWeight
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

          val helper       = new UnloadingAnswersHelper(userAnswers)
          val netWeightRow = helper.netWeightRow(index, index).head

          netWeightRow mustBe
            SummaryListRow(
              key = Key("Net weight".toText),
              value = Value(s"${netWeightBigDecimal}kg".toText)
            )

        }
      }
    }

    "totalGrossWeightRow" - {

      "must return Some(Row)" - {
        s"when total gross weight is passed to totalGrossWeightRow" in {

          val totalGrossWeight = Gen.double.sample.value

          val answers = emptyUserAnswers

          val helper = new UnloadingAnswersHelper(answers)
          val result = helper.totalGrossWeightRow(totalGrossWeight, hcIndex)

          result mustBe Some(
            SummaryListRow(
              key = Key("Gross weight".toText),
              value = Value(s"${BigDecimal(totalGrossWeight)}kg".toText),
              actions = grossWeightAction
            )
          )
        }
      }
    }

    "totalNetWeightRow" - {

      "must return Some(Row)" - {
        s"when total net weight is passed to totalNetWeightRow" in {

          val totalNetWeight = Gen.double.sample.value

          val answers = emptyUserAnswers

          val helper = new UnloadingAnswersHelper(answers)
          val result = helper.totalNetWeightRow(totalNetWeight, hcIndex)

          result mustBe Some(
            SummaryListRow(
              key = Key("Net weight".toText),
              value = Value(s"${BigDecimal(totalNetWeight)}kg".toText),
              actions = netWeightAction
            )
          )
        }
      }
    }

  }

}
