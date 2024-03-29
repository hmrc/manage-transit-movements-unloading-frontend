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

import forms.behaviours.StringFieldBehaviours
import models.Index
import models.messages.UnloadingRemarksRequest
import play.api.data.{Field, FormError}

class NetWeightFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey       = "netWeight.error.required"
  private val invalidCharacters = "netWeight.error.characters"
  private val decimalPoint      = "netWeight.error.decimal"
  private val maxLength         = UnloadingRemarksRequest.weightLength

  private val form      = new NetWeightFormProvider()(Index(0), Index(1))
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
      requiredError = FormError(fieldName, requiredKey, Seq(Index(1).display.toString, Index(0).display.toString))
    )
  }

  "must not bind strings wth too many decimal places" in {

    val invalidString      = "1.1234567"
    val validRegex: String = UnloadingRemarksRequest.weightRegex
    val expectedError      = FormError(fieldName, decimalPoint, Seq(validRegex))

    val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
    result.errors must contain(expectedError)
  }

  "must not bind strings wth invalid characters" in {

    val invalidString      = "abc"
    val validRegex: String = UnloadingRemarksRequest.weightCharsRegex
    val expectedError      = FormError(fieldName, invalidCharacters, Seq(validRegex))

    val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
    result.errors must contain(expectedError)
  }

}
