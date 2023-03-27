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
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.libs.json.{JsObject, Json}

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

      "to additional comments yes no page when seals does not exist" in {
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
          .mustBe(routes.AddUnloadingCommentsYesNoController.onPageLoad(arrivalId, mode))
      }

      "must go from can seals be read page" - {
        "to Are any seals broken page when answer is Yes" in {

          val userAnswers = emptyUserAnswers.setValue(CanSealsBeReadPage, true)
          navigator
            .nextPage(CanSealsBeReadPage, mode, userAnswers)
            .mustBe(controllers.routes.AreAnySealsBrokenController.onPageLoad(userAnswers.id, mode))
        }

        "to Are any seals broken page  when the answer is No" in {

          val userAnswers = emptyUserAnswers.setValue(CanSealsBeReadPage, false)
          navigator
            .nextPage(CanSealsBeReadPage, mode, userAnswers)
            .mustBe(controllers.routes.AreAnySealsBrokenController.onPageLoad(userAnswers.id, mode))
        }
      }

      "must go from can unloading comment page to check your answers page" in {

        val userAnswers = emptyUserAnswers.setValue(UnloadingCommentsPage, "test")
        navigator
          .nextPage(UnloadingCommentsPage, mode, userAnswers)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
      }

      "must go from are any seals broken page " - {
        "to unloading comments yes no page when the answer is No" in {

          val userAnswers = emptyUserAnswers.setValue(AreAnySealsBrokenPage, false)

          navigator
            .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
            .mustBe(routes.AddUnloadingCommentsYesNoController.onPageLoad(arrivalId, mode))
        }

        "to unloading comments yes no page when the answer is Yes" in {

          val userAnswers = emptyUserAnswers.setValue(AreAnySealsBrokenPage, true)

          navigator
            .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
            .mustBe(routes.AddUnloadingCommentsYesNoController.onPageLoad(arrivalId, mode))
        }
      }
      "must go from unloading comments yes no page" - {
        "when answer is true to unloading comments controller" in {
          val userAnswers = emptyUserAnswers.setValue(AddUnloadingCommentsYesNoPage, true)

          navigator
            .nextPage(AddUnloadingCommentsYesNoPage, mode, userAnswers)
            .mustBe(routes.UnloadingCommentsController.onPageLoad(arrivalId, mode))
        }
        "when answer is false to check your answers controller" in {
          val userAnswers = emptyUserAnswers.setValue(AddUnloadingCommentsYesNoPage, false)

          navigator
            .nextPage(AddUnloadingCommentsYesNoPage, mode, userAnswers)
            .mustBe(routes.CheckYourAnswersController.onPageLoad(arrivalId))
        }
        "to session expired controller when no exisiting answers found" in {
          navigator
            .nextPage(AddUnloadingCommentsYesNoPage, mode, emptyUserAnswers)
            .mustBe(routes.SessionExpiredController.onPageLoad())
        }
      }

      "must go from New Seal Number page to unloading summary page" ignore {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(SealPage(equipmentIndex, sealIndex), mode, answers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(arrivalId))
        }
      }

      "in Check mode" - {

        val mode = CheckMode

        "must go from a page that doesn't exist in the edit route map  to Check Your Answers" in {

          case object UnknownPage extends Page

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(UnknownPage, mode, answers)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(arrivalId))
          }
        }

        "must go from unloading comments yes no page" - {
          "to check your answers page if no selected" in {

            val userAnswers = emptyUserAnswers
              .setValue(AddUnloadingCommentsYesNoPage, false)

            navigator
              .nextPage(AddUnloadingCommentsYesNoPage, mode, userAnswers)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(arrivalId))
          }
          "to additional comments page if yes is selected and no comments found" in {

            val userAnswers = emptyUserAnswers
              .setValue(AddUnloadingCommentsYesNoPage, true)

            navigator
              .nextPage(AddUnloadingCommentsYesNoPage, mode, userAnswers)
              .mustBe(controllers.routes.UnloadingCommentsController.onPageLoad(arrivalId, mode))
          }
          "to additional comments page if yes is selected and comments found" in {

            val userAnswers = emptyUserAnswers
              .setValue(AddUnloadingCommentsYesNoPage, true)
              .setValue(UnloadingCommentsPage, "comment")

            navigator
              .nextPage(AddUnloadingCommentsYesNoPage, mode, userAnswers)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(arrivalId))
          }
          "to session expired controller when no existing answers found" in {
            navigator
              .nextPage(AddUnloadingCommentsYesNoPage, mode, emptyUserAnswers)
              .mustBe(routes.SessionExpiredController.onPageLoad())
          }
        }

        "must go from date goods unloaded page to check your answers page" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(DateGoodsUnloadedPage, mode, answers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(arrivalId))
          }
        }

        "must go from Gross mass amount page to check your answers page" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(GrossWeightPage(index, itemIndex), mode, answers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(arrivalId))
          }
        }

        "must go from New Seal Number page to check your answers page" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(SealPage(equipmentIndex, sealIndex), mode, answers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(arrivalId))
          }
        }

        "must go from Remove comments page " - {
          "to check your answers page when the form is submitted" in {

            forAll(arbitrary[UserAnswers]) {
              answers =>
                navigator
                  .nextPage(ConfirmRemoveCommentsPage, mode, answers)
                  .mustBe(routes.CheckYourAnswersController.onPageLoad(arrivalId))
            }
          }
        }
      }
    }
  }
}
