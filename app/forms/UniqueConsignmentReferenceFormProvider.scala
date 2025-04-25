/*
 * Copyright 2025 HM Revenue & Customs
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

import forms.Constants.maxUCRLength
import forms.mappings.Mappings
import models.{Mode, RichString}
import models.messages.UnloadingRemarksRequest.alphaNumericRegex
import play.api.data.Form

import javax.inject.Inject

class UniqueConsignmentReferenceFormProvider @Inject() extends Mappings {

  def apply(prefix: String, mode: Mode): Form[String] =
    Form(
      "value" -> adaptedText(s"$prefix.${mode.toString}.error.required")(_.removeSpaces())
        .verifying(
          forms.StopOnFirstFail[String](
            regexp(alphaNumericRegex, s"$prefix.${mode.toString}.error.invalid"),
            maxLength(maxUCRLength, s"$prefix.${mode.toString}.error.length")
          )
        )
    )

}
