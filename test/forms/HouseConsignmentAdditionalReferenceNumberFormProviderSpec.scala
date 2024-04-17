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

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.Constants.maxAdditionalReferenceNumLength
import forms.behaviours.StringFieldBehaviours
import models.messages.UnloadingRemarksRequest.stringFieldRegexComma
import play.api.data.FormError

class HouseConsignmentAdditionalReferenceNumberFormProviderSpec extends SpecBase with AppWithDefaultMockFixtures with StringFieldBehaviours {

  private val prefix      = nonEmptyString.sample.value
  private val requiredKey = s"$prefix.error.required"
  private val invalidKey  = s"$prefix.error.invalidCharacters"
  private val lengthKey   = s"$prefix.error.length"

  private val form = new HouseConsignmentAdditionalReferenceNumberFormProvider()(prefix)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxAdditionalReferenceNumLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxAdditionalReferenceNumLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxAdditionalReferenceNumLength))
    )

    behave like fieldWithInvalidCharacters(
      form,
      fieldName,
      error = FormError(fieldName, invalidKey, Seq(stringFieldRegexComma.regex)),
      maxAdditionalReferenceNumLength
    )
  }
}
