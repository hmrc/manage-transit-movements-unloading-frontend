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
import models.DocType.{Support, Transport}
import models._
import models.reference.DocumentType
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.documents
import pages.houseConsignment.index.documents._

class HouseConsignmentDocumentNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new HouseConsignmentDocumentNavigator

  "HouseConsignmentDocumentNavigator" - {

    "in Normal Mode" - {

      val mode = NormalMode

      "must go from TypePage to Reference number page" in {
        forAll(arbitrary[DocumentType](arbitrarySupportDocument)) {
          document =>
            val userAnswers = emptyUserAnswers
              .setValue(TypePage(hcIndex, documentIndex), document)

            navigator
              .nextPage(TypePage(hcIndex, documentIndex), mode, userAnswers)
              .mustBe(
                controllers.houseConsignment.index.documents.routes.ReferenceNumberController
                  .onPageLoad(arrivalId, NormalMode, houseConsignmentIndex, documentIndex)
              )

        }
      }

      "must go from DocumentReferenceNumberPage to AddAdditionalInformationYesNoController when DocType is supporting" in {
        forAll(arbitrary[String]) {
          ref =>
            val userAnswers = emptyUserAnswers
              .setValue(TypePage(houseConsignmentIndex, documentIndex), DocumentType(`type` = Support, code = "codeValue", description = "descriptionValue"))
              .setValue(DocumentReferenceNumberPage(hcIndex, documentIndex), ref)

            navigator
              .nextPage(DocumentReferenceNumberPage(hcIndex, documentIndex), mode, userAnswers)
              .mustBe(
                controllers.houseConsignment.index.documents.routes.AddAdditionalInformationYesNoController
                  .onPageLoad(arrivalId, houseConsignmentIndex, documentIndex)
              )

        }
      }

      "must go from DocumentReferenceNumberPage to AddAnotherDocumentController when DocType is Transport" in {
        forAll(arbitrary[String]) {
          ref =>
            val userAnswers = emptyUserAnswers
              .setValue(TypePage(houseConsignmentIndex, documentIndex), DocumentType(`type` = Transport, code = "codeValue", description = "descriptionValue"))
              .setValue(DocumentReferenceNumberPage(hcIndex, documentIndex), ref)

            navigator
              .nextPage(DocumentReferenceNumberPage(hcIndex, documentIndex), mode, userAnswers)
              .mustBe(
                controllers.houseConsignment.index.documents.routes.AddAnotherDocumentController
                  .onPageLoad(arrivalId, houseConsignmentIndex, mode)
              )

        }
      }

      "must go from AddAdditionalInformationYesNo page to AdditionalInformationController when user answers yes" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddAdditionalInformationYesNoPage(hcIndex, documentIndex), true)

        navigator
          .nextPage(documents.AddAdditionalInformationYesNoPage(hcIndex, documentIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.documents.routes.AdditionalInformationController
              .onPageLoad(arrivalId, mode, houseConsignmentIndex, documentIndex)
          )

      }

      "must go from AddAdditionalInformationYesNo page to AddAnotherDocumentController when user answers No" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddAdditionalInformationYesNoPage(hcIndex, documentIndex), false)

        navigator
          .nextPage(documents.AddAdditionalInformationYesNoPage(hcIndex, documentIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.documents.routes.AddAnotherDocumentController
              .onPageLoad(arrivalId, houseConsignmentIndex, mode)
          )

      }

      "must go from AdditionalInformation page to AddAnotherDocumentController " in {

        val userAnswers = emptyUserAnswers
          .setValue(AdditionalInformationPage(hcIndex, documentIndex), "document details")

        navigator
          .nextPage(documents.AdditionalInformationPage(hcIndex, documentIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.documents.routes.AddAnotherDocumentController
              .onPageLoad(arrivalId, houseConsignmentIndex, mode)
          )

      }

    }
    "in Check mode" - {

      val mode = CheckMode

      "must go from TypePage to HouseConsignmentController" in {
        forAll(arbitrary[DocumentType](arbitrarySupportDocument)) {
          document =>
            val userAnswers = emptyUserAnswers
              .setValue(TypePage(hcIndex, documentIndex), document)

            navigator
              .nextPage(TypePage(hcIndex, documentIndex), mode, userAnswers)
              .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
        }
      }

      "must go from DocumentReferenceNumberPage to HouseConsignmentController" in {
        forAll(arbitrary[DocumentType](arbitrarySupportDocument)) {
          document =>
            val userAnswers = emptyUserAnswers
              .setValue(TypePage(hcIndex, documentIndex), document)

            navigator
              .nextPage(DocumentReferenceNumberPage(hcIndex, documentIndex), mode, userAnswers)
              .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
        }
      }

      "must go from AdditionalInformationPage to HouseConsignmentController" in {
        forAll(arbitrary[DocumentType](arbitrarySupportDocument)) {
          document =>
            val userAnswers = emptyUserAnswers
              .setValue(TypePage(hcIndex, documentIndex), document)

            navigator
              .nextPage(AdditionalInformationPage(hcIndex, documentIndex), mode, userAnswers)
              .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
        }
      }

    }
  }
}
