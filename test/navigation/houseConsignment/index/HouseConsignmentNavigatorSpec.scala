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

package navigation.houseConsignment.index

import base.SpecBase
import generators.Generators
import models._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index._
import pages.houseConsignment.index.items.AddItemYesNoPage

class HouseConsignmentNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator = new HouseConsignmentNavigator

  "GrossWeightNavigator" - {

    "in Check mode" - {
      val mode = CheckMode

      "must go from gross weight page to house consignment cross-check page" in {
        val page = GrossWeightPage(hcIndex)

        val userAnswers = emptyUserAnswers
          .setValue(page, BigDecimal("12.0"))

        navigator
          .nextPage(page, mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }
    }

    "in Normal mode" - {
      val mode = NormalMode

      "must go from gross weight page to add departure means of transport yes/no page" in {
        val page = GrossWeightPage(hcIndex)

        val userAnswers = emptyUserAnswers
          .setValue(page, BigDecimal("12.0"))

        navigator
          .nextPage(page, mode, userAnswers)
          .mustBe(controllers.houseConsignment.index.routes.AddDepartureTransportMeansYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, mode))
      }

      "must go from add departure means of transport yes/no page" - {
        val page = AddDepartureTransportMeansYesNoPage(hcIndex)

        "when yes selected" - {
          "to departure means of transport type of identification page" in {
            val userAnswers = emptyUserAnswers
              .setValue(page, true)

            navigator
              .nextPage(page, mode, userAnswers)
              .mustBe(
                controllers.houseConsignment.index.departureMeansOfTransport.routes.IdentificationController
                  .onPageLoad(arrivalId, houseConsignmentIndex, Index(0), mode, NormalMode)
              )
          }
        }

        "when no selected" - {
          "to add documents yes/no page" in {
            val userAnswers = emptyUserAnswers
              .setValue(page, false)

            navigator
              .nextPage(page, mode, userAnswers)
              .mustBe(
                controllers.houseConsignment.index.routes.AddDocumentsYesNoController
                  .onPageLoad(arrivalId, mode, houseConsignmentIndex)
              )
          }
        }
      }

      "must go from add documents yes/no page" - {
        val page = AddDocumentYesNoPage(hcIndex)

        "when yes selected" - {
          "to document type page" in {
            val userAnswers = emptyUserAnswers
              .setValue(page, true)

            navigator
              .nextPage(page, mode, userAnswers)
              .mustBe(
                controllers.houseConsignment.index.documents.routes.TypeController
                  .onPageLoad(arrivalId, mode, NormalMode, houseConsignmentIndex, Index(0))
              )
          }
        }

        "when no selected" - {
          "to add additional references yes/no page" in {
            val userAnswers = emptyUserAnswers
              .setValue(page, false)

            navigator
              .nextPage(page, mode, userAnswers)
              .mustBe(
                controllers.houseConsignment.index.routes.AddAdditionalReferenceYesNoController
                  .onPageLoad(arrivalId, mode, houseConsignmentIndex)
              )
          }
        }
      }

      "must go from add additional references yes/no page" - {
        val page = AddAdditionalReferenceYesNoPage(hcIndex)

        "when yes selected" - {
          "to additional reference type page" in {
            val userAnswers = emptyUserAnswers
              .setValue(page, true)

            navigator
              .nextPage(page, mode, userAnswers)
              .mustBe(
                controllers.houseConsignment.index.additionalReference.routes.AdditionalReferenceTypeController
                  .onPageLoad(arrivalId, mode, NormalMode, houseConsignmentIndex, Index(0))
              )
          }
        }

        "when no selected" - {
          "to add items yes/no page" in {
            val userAnswers = emptyUserAnswers
              .setValue(page, false)

            navigator
              .nextPage(page, mode, userAnswers)
              .mustBe(
                controllers.houseConsignment.index.items.routes.AddItemYesNoController
                  .onPageLoad(arrivalId, houseConsignmentIndex, mode)
              )
          }
        }
      }

      "must go from add items yes/no page" - {
        val page = AddItemYesNoPage(hcIndex)

        "when yes selected" - {
          "to item description page" in {
            val userAnswers = emptyUserAnswers
              .setValue(page, true)

            navigator
              .nextPage(page, mode, userAnswers)
              .mustBe(
                controllers.houseConsignment.index.items.routes.DescriptionController
                  .onPageLoad(arrivalId, mode, NormalMode, houseConsignmentIndex, Index(0))
              )
          }
        }

        "when no selected" - {
          "to add another house consignment page" in {
            val userAnswers = emptyUserAnswers
              .setValue(page, false)

            navigator
              .nextPage(page, mode, userAnswers)
              .mustBe(
                controllers.houseConsignment.routes.AddAnotherHouseConsignmentController
                  .onPageLoad(arrivalId, mode)
              )
          }
        }
      }
    }
  }
}
