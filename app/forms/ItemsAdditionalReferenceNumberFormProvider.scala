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

import forms.Constants.maxAdditionalReferenceNumLength
import forms.mappings.Mappings
import models.messages.UnloadingRemarksRequest.stringFieldRegexComma
import play.api.data.Form

import javax.inject.Inject

class ItemsAdditionalReferenceNumberFormProvider @Inject() extends Mappings {

  def apply(requiredError: String, isDocumentInCL234: Boolean): Form[String] =
    Form(
      "value" -> text(requiredError)
        .verifying(
          forms.StopOnFirstFail[String](
            regexp(stringFieldRegexComma, "houseConsignment.index.items.additionalReference.additionalReferenceNumber.error.invalidCharacters"),
            maxLength(maxAdditionalReferenceNumLength, "houseConsignment.index.items.additionalReference.additionalReferenceNumber.error.length"),
            cl234Constraint(isDocumentInCL234, "houseConsignment.index.items.additionalReference.additionalReferenceNumber.error.cl234Constraint")
          )
        )
    )
}
