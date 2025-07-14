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
import generators.Generators
import models._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.documents._

class DocumentNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new DocumentNavigator

  "DocumentNavigation" - {

    "in Normal mode" - {

      val mode = NormalMode

      "must go from TypePage to DocumentReferenceNumberPage" in {

        val docType = arbitraryTransportOrSupportDocument.arbitrary.sample.value

        val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), docType)

        navigator
          .nextPage(TypePage(documentIndex), mode, userAnswers)
          .mustEqual(controllers.documents.routes.DocumentReferenceNumberController.onPageLoad(arrivalId, mode, documentIndex))
      }

      "must go from DocumentReferenceNumberPage to AddAdditionalInformationYesNoPage when Document Type is Support" in {

        val docType = arbitrarySupportDocument.arbitrary.sample.value

        val userAnswers = emptyUserAnswers
          .setValue(TypePage(documentIndex), docType)
          .setValue(DocumentReferenceNumberPage(documentIndex), "docRef")

        navigator
          .nextPage(DocumentReferenceNumberPage(documentIndex), mode, userAnswers)
          .mustEqual(controllers.documents.routes.AddAdditionalInformationYesNoController.onPageLoad(arrivalId, mode, documentIndex))
      }

      "must go from DocumentReferenceNumberPage to AddAnotherDocumentPage when Document Type is Transport" in {

        val docType = arbitraryTransportDocument.arbitrary.sample.value

        val userAnswers = emptyUserAnswers
          .setValue(TypePage(documentIndex), docType)
          .setValue(DocumentReferenceNumberPage(documentIndex), "docRef")

        navigator
          .nextPage(DocumentReferenceNumberPage(documentIndex), mode, userAnswers)
          .mustEqual(controllers.documents.routes.AddAnotherDocumentController.onPageLoad(arrivalId, mode))
      }

      "must go from DocumentReferenceNumberPage to TypePage when Document Type is unexpectedly Previous" in {

        val docType = arbitraryPreviousDocument.arbitrary.sample.value

        val userAnswers = emptyUserAnswers
          .setValue(TypePage(documentIndex), docType)
          .setValue(DocumentReferenceNumberPage(documentIndex), "docRef")

        navigator
          .nextPage(DocumentReferenceNumberPage(documentIndex), mode, userAnswers)
          .mustEqual(controllers.documents.routes.TypeController.onPageLoad(arrivalId, mode, documentIndex))
      }

      "must go from AddAdditionalInformationYesNoPage to AdditionalInformationPage when answer is true" in {

        val docType = arbitraryTransportOrSupportDocument.arbitrary.sample.value

        val userAnswers = emptyUserAnswers
          .setValue(TypePage(documentIndex), docType)
          .setValue(AddAdditionalInformationYesNoPage(documentIndex), true)

        navigator
          .nextPage(AddAdditionalInformationYesNoPage(documentIndex), mode, userAnswers)
          .mustEqual(controllers.documents.routes.AdditionalInformationController.onPageLoad(arrivalId, mode, documentIndex))
      }

      "must go from AddAdditionalInformationYesNoPage to AddAnotherDocumentPage when answer is false" in {

        val docType = arbitraryTransportOrSupportDocument.arbitrary.sample.value

        val userAnswers = emptyUserAnswers
          .setValue(TypePage(documentIndex), docType)
          .setValue(AddAdditionalInformationYesNoPage(documentIndex), false)

        navigator
          .nextPage(AddAdditionalInformationYesNoPage(documentIndex), mode, userAnswers)
          .mustEqual(controllers.documents.routes.AddAnotherDocumentController.onPageLoad(arrivalId, mode))
      }
    }

    "in Check mode" - {

      val mode = CheckMode

      "must go from AdditionalInformationPage to UnloadingFindingsController" in {

        val userAnswers = emptyUserAnswers.setValue(AdditionalInformationPage(documentIndex), "Additional Information")

        navigator
          .nextPage(AdditionalInformationPage(documentIndex), mode, userAnswers)
          .mustEqual(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
      }

      "must go from DocumentReferenceNumberPage to UnloadingFindingsController" in {

        val userAnswers = emptyUserAnswers.setValue(DocumentReferenceNumberPage(documentIndex), "12345")

        navigator
          .nextPage(DocumentReferenceNumberPage(documentIndex), mode, userAnswers)
          .mustEqual(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
      }

      "must go from TypePage to UnloadingFindingsController" in {

        val docType = arbitraryDocumentType.arbitrary.sample.value

        val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), docType)

        navigator
          .nextPage(TypePage(documentIndex), mode, userAnswers)
          .mustEqual(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
      }
    }
  }
}
