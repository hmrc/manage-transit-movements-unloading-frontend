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

import forms.Constants.maxSealIdentificationLength
import forms.mappings.Mappings
import models.RichString
import models.messages.UnloadingRemarksRequest.alphaNumericWithSpacesRegex
import play.api.data.Form

import javax.inject.Inject

class SealIdentificationNumberFormProvider @Inject() extends Mappings {

  def apply(requiredError: String, otherSealIdentificationNumbers: Seq[String]): Form[String] =
    Form(
      "value" -> adaptedText(requiredError)(_.removeSpaces())
        .verifying(
          forms.StopOnFirstFail[String](
            regexp(alphaNumericWithSpacesRegex, "transportEquipment.index.seal.identificationNumber.error.invalidCharacters"),
            maxLength(maxSealIdentificationLength, "transportEquipment.index.seal.identificationNumber.error.length"),
            valueIsNotInList(otherSealIdentificationNumbers, "transportEquipment.index.seal.identificationNumber.error.duplicate")
          )
        )
    )
}
