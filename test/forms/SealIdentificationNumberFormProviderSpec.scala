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

import base.AppWithDefaultMockFixtures
import config.FrontendAppConfig
import forms.Constants.maxSealIdentificationLength
import forms.behaviours.StringFieldBehaviours
import models.messages.UnloadingRemarksRequest.alphaNumericWithSpacesRegex
import org.mockito.Mockito.when
import play.api.data.FormError

class SealIdentificationNumberFormProviderSpec extends StringFieldBehaviours with AppWithDefaultMockFixtures {

  private val prefix               = "transportEquipment.index.seal.identificationNumber"
  private val invalidCharactersKey = s"$prefix.error.invalidCharacters"
  private val requiredKey          = s"$prefix.error.required"
  private val maxLengthKey         = s"$prefix.error.length"
  val duplicateKey                 = s"$prefix.error.duplicate"
  private val maxLength            = 20

  private val mockFrontendAppConfig = mock[FrontendAppConfig]

  private val formProvider = app.injector.instanceOf[SealIdentificationNumberFormProvider]
  private val form         = formProvider.apply(requiredKey, Seq.empty)

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
      error = FormError(fieldName, invalidCharactersKey, Seq(alphaNumericWithSpacesRegex.regex)),
      maxSealIdentificationLength
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, maxLengthKey, Seq(maxLength))
    )

    "when phase 5" - {
      "must not bind if value exists in the list of other ids" in {
        when(mockFrontendAppConfig.phase6Enabled).thenReturn(false)
        val otherIds = Seq("foo", "bar")
        val form     = new SealIdentificationNumberFormProvider(mockFrontendAppConfig).apply(prefix, otherIds)
        val result   = form.bind(Map(fieldName -> "foo")).apply(fieldName)
        result.errors mustEqual Seq(FormError(fieldName, duplicateKey))
      }
    }

    "when phase 6" - {
      "must bind if value exists in the list of other ids" in {
        when(mockFrontendAppConfig.phase6Enabled).thenReturn(true)
        val otherIds = Seq("foo", "bar")
        val form     = new SealIdentificationNumberFormProvider(mockFrontendAppConfig).apply(prefix, otherIds)
        val dataItem = "foo"
        val result   = form.bind(Map(fieldName -> dataItem)).apply(fieldName)
        result.value.value mustEqual dataItem
        result.errors must be(empty)
      }
    }
  }
}
