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
import pages.documents.{AdditionalInformationPage, DocumentReferenceNumberPage, TypePage}

class DocumentNavigationSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new DocumentNavigation

  "DocumentNavigation" - {

    "in Check mode" - {

      val mode = CheckMode

      "must go from AdditionalInformationPage to UnloadingFindingsController" - {

        val userAnswers = emptyUserAnswers.setValue(AdditionalInformationPage(documentIndex), "Additional Information")

        navigator
          .nextPage(AdditionalInformationPage(documentIndex), mode, userAnswers)
          .mustBe(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
      }

      "must go from DocumentReferenceNumberPage to UnloadingFindingsController" - {

        val userAnswers = emptyUserAnswers.setValue(DocumentReferenceNumberPage(documentIndex), "12345")

        navigator
          .nextPage(DocumentReferenceNumberPage(documentIndex), mode, userAnswers)
          .mustBe(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
      }

      "must go from TypePage to UnloadingFindingsController" - {

        val docType = arbitraryDocumentType.arbitrary.sample.value

        val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), docType)

        navigator
          .nextPage(TypePage(documentIndex), mode, userAnswers)
          .mustBe(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
      }
    }
  }
}
