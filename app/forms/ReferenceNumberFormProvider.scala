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

package forms

import forms.Constants.maxDocumentRefNumberLength
import forms.mappings.Mappings
import models.messages.UnloadingRemarksRequest.alphaNumericWithSpacesRegex
import models.{Index, RichString}
import play.api.data.Form

import javax.inject.Inject

class ReferenceNumberFormProvider @Inject() extends Mappings {

  def apply(requiredError: String, houseConsignmentIndex: Index, otherDocumentReferenceNumbers: Seq[String]): Form[String] =
    Form(
      "value" -> adaptedText(s"$requiredError", args = Seq(houseConsignmentIndex.display))(_.removeSpaces())
        .verifying(
          forms.StopOnFirstFail[String](
            regexp(alphaNumericWithSpacesRegex, "houseConsignment.index.documents.referenceNumber.error.invalidCharacters"),
            maxLength(maxDocumentRefNumberLength, "houseConsignment.index.documents.referenceNumber.error.length"),
            valueIsNotInList(otherDocumentReferenceNumbers, "houseConsignment.index.documents.referenceNumber.error.duplicate")
          )
        )
    )
}
