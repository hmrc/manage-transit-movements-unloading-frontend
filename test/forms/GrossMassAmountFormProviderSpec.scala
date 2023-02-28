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

import models.messages.UnloadingRemarksRequest
import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.{Field, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

class GrossWeightAmountFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey       = "grossWeightAmount.error.required"
  private val invalidCharacters = "grossWeightAmount.error.characters"
  private val decimalPoint      = "grossWeightAmount.error.decimal"
  private val maxLength         = UnloadingRemarksRequest.grossWeightLength

  private val form      = new GrossWeightAmountFormProvider()()
  private val fieldName = "value"

  ".value" - {

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  "must not bind strings wth too many decimal places" in {

    val invalidString      = "1.1234567"
    val validRegex: String = UnloadingRemarksRequest.grossWeightRegex
    val expectedError      = FormError(fieldName, decimalPoint, Seq(validRegex))

    val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
    result.errors should contain(expectedError)
  }

  "must not bind strings wth invalid characters" in {

    val invalidString      = "abc"
    val validRegex: String = UnloadingRemarksRequest.grossWeightCharsRegex
    val expectedError      = FormError(fieldName, invalidCharacters, Seq(validRegex))

    val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
    result.errors should contain(expectedError)
  }

}
