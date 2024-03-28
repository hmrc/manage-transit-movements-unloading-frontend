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

package navigation.houseConsignment.index.items

import base.SpecBase
import generators.Generators
import models.DocType.{Previous, Support, Transport}
import models._
import models.reference.DocumentType
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.document.{AddAdditionalInformationYesNoPage, AdditionalInformationPage, DocumentReferenceNumberPage, TypePage}

class DocumentNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new DocumentNavigator

  "DocumentNavigation" - {

    "in Check mode" - {

      val mode = CheckMode

      "must go from AdditionalInformationPage to UnloadingFindingsController" - {

        val userAnswers = emptyUserAnswers.setValue(AdditionalInformationPage(houseConsignmentIndex, itemIndex, documentIndex), "Additional Information")

        navigator
          .nextPage(AdditionalInformationPage(houseConsignmentIndex, itemIndex, documentIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex))
      }

      "must go from DocumentReferenceNumberPage to UnloadingFindingsController" - {

        val userAnswers = emptyUserAnswers.setValue(DocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, documentIndex), "12345")

        navigator
          .nextPage(DocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, documentIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex))
      }

      "must go from TypePage to UnloadingFindingsController" - {

        val docType = arbitraryDocumentType.arbitrary.sample.value

        val userAnswers = emptyUserAnswers.setValue(TypePage(houseConsignmentIndex, itemIndex, documentIndex), docType)

        navigator
          .nextPage(TypePage(houseConsignmentIndex, itemIndex, documentIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex))
      }
    }

    "in Normal mode" - {

      val mode = NormalMode

      "must go from AddAdditionalInformationPage to AddAnotherDocumentPage" - {

        val userAnswers = emptyUserAnswers.setValue(AdditionalInformationPage(houseConsignmentIndex, itemIndex, documentIndex), "Additional Information")

        navigator
          .nextPage(AdditionalInformationPage(houseConsignmentIndex, itemIndex, documentIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.AddAnotherDocumentController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode)
          )
      }

      "must go from AddAdditionalInformationYesNoPage to AddAdditionalInformationPage when yes" - {

        val userAnswers = emptyUserAnswers.setValue(AddAdditionalInformationYesNoPage(houseConsignmentIndex, itemIndex, documentIndex), true)

        navigator
          .nextPage(AddAdditionalInformationYesNoPage(houseConsignmentIndex, itemIndex, documentIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.AdditionalInformationController
              .onPageLoad(arrivalId, mode, houseConsignmentIndex, itemIndex, documentIndex)
          )
      }

      "must go from AddAdditionalInformationYesNoPage to AddAnotherDocumentPage when no" - {

        val userAnswers = emptyUserAnswers.setValue(AddAdditionalInformationYesNoPage(houseConsignmentIndex, itemIndex, documentIndex), false)

        navigator
          .nextPage(AddAdditionalInformationYesNoPage(houseConsignmentIndex, itemIndex, documentIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.AddAnotherDocumentController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode)
          )
      }

      "must go from TypePage to AddAdditionalInformationYesNoPage when DocType is supporting" - {

        val userAnswers = emptyUserAnswers.setValue(TypePage(houseConsignmentIndex, itemIndex, documentIndex),
                                                    DocumentType(`type` = Support, code = "codeValue", description = "descriptionValue")
        )

        navigator
          .nextPage(TypePage(houseConsignmentIndex, itemIndex, documentIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.AddAdditionalInformationYesNoController
              .onPageLoad(arrivalId, mode, houseConsignmentIndex, itemIndex, documentIndex)
          )
      }

      "must go from TypePage to AddAnotherDocumentPage when DocType is not supporting" - {

        val userAnswers = emptyUserAnswers.setValue(TypePage(houseConsignmentIndex, itemIndex, documentIndex),
                                                    DocumentType(`type` = Transport, code = "codeValue", description = "descriptionValue")
        )
        navigator
          .nextPage(TypePage(houseConsignmentIndex, itemIndex, documentIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.AddAnotherDocumentController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode)
          )

      }

      "must redirect to the  TypePage when Previous DocType is selected" - {

        val userAnswers = emptyUserAnswers.setValue(TypePage(houseConsignmentIndex, itemIndex, documentIndex),
                                                    DocumentType(`type` = Previous, code = "codeValue", description = "descriptionValue")
        )
        navigator
          .nextPage(TypePage(houseConsignmentIndex, itemIndex, documentIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.TypeController
              .onPageLoad(arrivalId, mode, houseConsignmentIndex, itemIndex, documentIndex)
          )

      }

    }
  }
}
