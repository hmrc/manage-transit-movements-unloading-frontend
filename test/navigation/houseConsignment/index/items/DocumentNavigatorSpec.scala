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
import navigation.houseConsignment.index.items.DocumentNavigator.DocumentNavigatorProvider
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.document.{AddAdditionalInformationYesNoPage, AdditionalInformationPage, DocumentReferenceNumberPage, TypePage}

class DocumentNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigatorProvider = new DocumentNavigatorProvider

  "DocumentNavigation" - {

    "in Check mode" - {

      val houseConsignmentMode = CheckMode
      val itemMode             = CheckMode
      val documentMode         = CheckMode
      val navigator            = navigatorProvider.apply(houseConsignmentMode, itemMode)

      "must go from AdditionalInformationPage to UnloadingFindingsController" - {

        val userAnswers = emptyUserAnswers.setValue(AdditionalInformationPage(houseConsignmentIndex, itemIndex, documentIndex), "Additional Information")

        navigator
          .nextPage(AdditionalInformationPage(houseConsignmentIndex, itemIndex, documentIndex), documentMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex))
      }

      "must go from DocumentReferenceNumberPage to UnloadingFindingsController" - {

        val userAnswers = emptyUserAnswers.setValue(DocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, documentIndex), "12345")

        navigator
          .nextPage(DocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, documentIndex), documentMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex))
      }

      "must go from TypePage to UnloadingFindingsController" - {

        val docType = arbitraryDocumentType.arbitrary.sample.value

        val userAnswers = emptyUserAnswers.setValue(TypePage(houseConsignmentIndex, itemIndex, documentIndex), docType)

        navigator
          .nextPage(TypePage(houseConsignmentIndex, itemIndex, documentIndex), documentMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex))
      }
    }

    "in Normal mode" - {
      val (houseConsignmentMode, itemMode) = arbitrary[(Mode, Mode)]
        .retryUntil {
          case (NormalMode, CheckMode) => false
          case _                       => true
        }
        .sample
        .value

      val documentMode = NormalMode
      val navigator    = navigatorProvider.apply(houseConsignmentMode, itemMode)

      "must go from AddAdditionalInformationPage to AddAnotherDocumentPage" - {

        val userAnswers = emptyUserAnswers.setValue(AdditionalInformationPage(houseConsignmentIndex, itemIndex, documentIndex), "Additional Information")

        navigator
          .nextPage(AdditionalInformationPage(houseConsignmentIndex, itemIndex, documentIndex), documentMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.AddAnotherDocumentController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
          )
      }

      "must go from AddAdditionalInformationYesNoPage to AddAdditionalInformationPage when yes" - {

        val userAnswers = emptyUserAnswers.setValue(AddAdditionalInformationYesNoPage(houseConsignmentIndex, itemIndex, documentIndex), true)

        navigator
          .nextPage(AddAdditionalInformationYesNoPage(houseConsignmentIndex, itemIndex, documentIndex), documentMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.AdditionalInformationController
              .onPageLoad(arrivalId, houseConsignmentMode, itemMode, documentMode, houseConsignmentIndex, itemIndex, documentIndex)
          )
      }

      "must go from AddAdditionalInformationYesNoPage to AddAnotherDocumentPage when no" - {

        val userAnswers = emptyUserAnswers.setValue(AddAdditionalInformationYesNoPage(houseConsignmentIndex, itemIndex, documentIndex), false)

        navigator
          .nextPage(AddAdditionalInformationYesNoPage(houseConsignmentIndex, itemIndex, documentIndex), documentMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.AddAnotherDocumentController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
          )
      }

      "must go from TypePage to DocumentReferenceNumberController when DocType is supporting" - {

        val userAnswers = emptyUserAnswers.setValue(
          TypePage(houseConsignmentIndex, itemIndex, documentIndex),
          DocumentType(`type` = Support, code = "codeValue", description = "descriptionValue")
        )

        navigator
          .nextPage(TypePage(houseConsignmentIndex, itemIndex, documentIndex), documentMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.DocumentReferenceNumberController
              .onPageLoad(arrivalId, houseConsignmentMode, itemMode, documentMode, houseConsignmentIndex, itemIndex, documentIndex)
          )
      }

      "must go from TypePage to DocumentReferenceNumberController when DocType is not supporting" - {

        val userAnswers = emptyUserAnswers.setValue(
          TypePage(houseConsignmentIndex, itemIndex, documentIndex),
          DocumentType(`type` = Transport, code = "codeValue", description = "descriptionValue")
        )
        navigator
          .nextPage(TypePage(houseConsignmentIndex, itemIndex, documentIndex), documentMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.DocumentReferenceNumberController
              .onPageLoad(arrivalId, houseConsignmentMode, itemMode, documentMode, houseConsignmentIndex, itemIndex, documentIndex)
          )

      }

      "must redirect to the TypePage when Previous DocType is selected" - {

        val userAnswers = emptyUserAnswers.setValue(
          TypePage(houseConsignmentIndex, itemIndex, documentIndex),
          DocumentType(`type` = Previous, code = "codeValue", description = "descriptionValue")
        )
        navigator
          .nextPage(TypePage(houseConsignmentIndex, itemIndex, documentIndex), documentMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.TypeController
              .onPageLoad(arrivalId, houseConsignmentMode, itemMode, documentMode, houseConsignmentIndex, itemIndex, documentIndex)
          )

      }

    }
  }
}
