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
import forms.behaviours.StringFieldBehaviours
import models.messages.UnloadingRemarksRequest.alphaNumericWithFullStopsRegex
import play.api.data.FormError

class ReferenceNumberFormProviderSpec extends StringFieldBehaviours {

  private val prefix               = "houseConsignment.index.documents.referenceNumber"
  private val invalidCharactersKey = s"$prefix.error.invalidCharacters"
  private val requiredKey          = s"$prefix.error.required"
  private val maxLengthKey         = s"$prefix.error.length"
  private val duplicateKey         = s"$prefix.error.duplicate"
  private val maxLength            = 70

  val form = new ReferenceNumberFormProvider()(requiredKey, hcIndex, Seq.empty)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldThatRemovesSpaces(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithInvalidCharacters(
      form,
      fieldName,
      error = FormError(fieldName, invalidCharactersKey, Seq(alphaNumericWithFullStopsRegex.regex)),
      maxDocumentRefNumberLength
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(houseConsignmentIndex.display))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, maxLengthKey, Seq(maxLength))
    )

    "must not bind if value exists in the list of other ids" in {
      val otherIds  = Seq("foo", "bar")
      val form      = new ReferenceNumberFormProvider()(prefix, hcIndex, otherIds)
      val boundForm = form.bind(Map("value" -> "foo"))
      val field     = boundForm("value")
      field.errors mustEqual Seq(FormError(fieldName, duplicateKey))
    }
  }
}
