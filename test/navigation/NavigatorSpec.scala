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

package navigation

import base.SpecBase
import controllers.routes
import generators.Generators
import models.P5._
import models.{CheckMode, MovementReferenceNumber, NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.libs.json.{JsObject, JsValue, Json}

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

class NavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      val mode = NormalMode

      "must go from a page that doesn't exist in the route map to unloading summary" in {

        case object UnknownPage extends Page

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(UnknownPage, mode, answers)
              .mustBe(routes.SessionExpiredController.onPageLoad())
        }
      }

      "must go from date goods unloaded page" - {
        "to can seals be read page when seals exist" in {
          val json: JsObject = Json
            .parse(
              """{
                |            "TransitOperation": {
                |                "MRN": "38VYQTYFU3T0KUTUM3"
                |            },
                |            "preparationDateAndTime": "2007-10-26T07:36:28",
                |            "Consignment": {
                |               "HouseConsignment": [],
                |
                |                "TransportEquipment": [
                |                    {
                |                        "sequenceNumber": "te1",
                |                        "containerIdentificationNumber": "cin-1",
                |                        "numberOfSeals": 103,
                |                        "Seal": [
                |                            {
                |                                "sequenceNumber": "1001",
                |                                "identifier": "1002"
                |                            }
                |                        ]
                |
                |                    }
                |                ]
                |            },
                |            "CustomsOfficeOfDestinationActual": {
                |                "referenceNumber": "GB000008"
                |            }
                |
                |     }
                |""".stripMargin
            )
            .as[JsObject]

          val userAnswers = emptyUserAnswers.copy(data = json)

          navigator
            .nextPage(DateGoodsUnloadedPage, mode, userAnswers)
            .mustBe(controllers.routes.CanSealsBeReadController.onPageLoad(userAnswers.id, mode))
        }
      }

      "to additional comments page when seals does not exist" in {
        val json: JsObject = Json
          .parse(
            """{
              |            "TransitOperation": {
              |                "MRN": "38VYQTYFU3T0KUTUM3"
              |            },
              |            "preparationDateAndTime": "2007-10-26T07:36:28",
              |            "Consignment": {
              |               "HouseConsignment": [],
              |
              |                "TransportEquipment": [
              |                    {
              |                        "sequenceNumber": "te1",
              |                        "containerIdentificationNumber": "cin-1",
              |                        "numberOfSeals": 0
              |                    }
              |                ]
              |            },
              |            "CustomsOfficeOfDestinationActual": {
              |                "referenceNumber": "GB000008"
              |            }
              |
              |     }
              |""".stripMargin
          )
          .as[JsObject]

        val userAnswers = emptyUserAnswers.copy(data = json)

        navigator
          .nextPage(DateGoodsUnloadedPage, mode, userAnswers)
          .mustBe(controllers.routes.UnloadingCommentsController.onPageLoad(userAnswers.id, mode))
      }

      "must go from can seals be read page" ignore {
        "to Are any seals broken page when answer is Yes" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.setValue(CanSealsBeReadPage, true)
              navigator
                .nextPage(CanSealsBeReadPage, mode, updatedUserAnswers)
                .mustBe(controllers.routes.AreAnySealsBrokenController.onPageLoad(updatedUserAnswers.id, mode))
          }
        }

        "to Are any seals broken page  when the answer is No" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.setValue(CanSealsBeReadPage, false)
              navigator
                .nextPage(CanSealsBeReadPage, mode, updatedUserAnswers)
                .mustBe(controllers.routes.AreAnySealsBrokenController.onPageLoad(updatedUserAnswers.id, mode))
          }
        }

        "to additional comments page when the answer is empty" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.removeValue(CanSealsBeReadPage)
              navigator
                .nextPage(CanSealsBeReadPage, mode, updatedUserAnswers)
                .mustBe(routes.UnloadingCommentsController.onPageLoad(updatedUserAnswers.id, NormalMode))
          }
        }
      }

      "must go from are any seals broken page " ignore {
        "to unloading commentspage when the answer is No" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.setValue(AreAnySealsBrokenPage, false)

              navigator
                .nextPage(AreAnySealsBrokenPage, mode, updatedUserAnswers)
                .mustBe(routes.UnloadingCommentsController.onPageLoad(updatedUserAnswers.id, NormalMode))
          }
        }

        "to unloading summary page when the answer is Yes" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.setValue(AreAnySealsBrokenPage, true)

              navigator
                .nextPage(AreAnySealsBrokenPage, mode, updatedUserAnswers)
                .mustBe(routes.SessionExpiredController.onPageLoad())
          }
        }

        "to session expired page when the answer is empty" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.removeValue(AreAnySealsBrokenPage)

              navigator
                .nextPage(AreAnySealsBrokenPage, mode, updatedUserAnswers)
                .mustBe(routes.SessionExpiredController.onPageLoad())
          }
        }
      }

      "must go from New Seal Number page to unloading summary page" ignore {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(SealPage(equipmentIndex, sealIndex), mode, answers)
              .mustBe(routes.SessionExpiredController.onPageLoad())
        }
      }

      "from changes to report page to unloading summary page" ignore {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(UnloadingCommentsPage, mode, answers)
              .mustBe(routes.SessionExpiredController.onPageLoad())

        }
      }

      "in Check mode" ignore {

        val mode = CheckMode

        "must go from a page that doesn't exist in the edit route map  to Check Your Answers" in {

          case object UnknownPage extends Page

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(UnknownPage, mode, answers)
                .mustBe(routes.SessionExpiredController.onPageLoad())
          }
        }

        "must go from date goods unloaded page to check your answers page" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(DateGoodsUnloadedPage, mode, answers)
                .mustBe(routes.SessionExpiredController.onPageLoad())
          }
        }

        "must go from Vehicle Name Registration Reference page to check your answers page" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(VehicleIdentificationNumberPage, mode, answers)
                .mustBe(routes.SessionExpiredController.onPageLoad())
          }
        }

        "must go from Vehicle Registration Country page to check your answers page" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(VehicleIdentificationNumberPage, mode, answers)
                .mustBe(routes.SessionExpiredController.onPageLoad())
          }
        }

        "must go from Gross mass amount page to check your answers page" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(GrossWeightPage(itemIndex), mode, answers)
                .mustBe(routes.SessionExpiredController.onPageLoad())
          }
        }

        "must go from New Seal Number page to check your answers page" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(SealPage(equipmentIndex, sealIndex), mode, answers)
                .mustBe(routes.SessionExpiredController.onPageLoad())
          }
        }

        "must go from Remove comments page " - {
          "to check your answers page when the form is submitted" in {

            forAll(arbitrary[UserAnswers]) {
              answers =>
                navigator
                  .nextPage(ConfirmRemoveCommentsPage, mode, answers)
                  .mustBe(routes.SessionExpiredController.onPageLoad())
            }
          }
        }
      }
    }
  }
}
