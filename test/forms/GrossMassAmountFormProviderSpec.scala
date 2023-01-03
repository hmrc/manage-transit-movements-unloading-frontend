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

class GrossMassAmountFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "grossMassAmount.error.required"
  private val invalidKey  = "grossMassAmount.error.characters"
  private val maxLength   = UnloadingRemarksRequest.grossMassLength

  private val form      = new GrossMassAmountFormProvider()()
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

  "must not bind strings that do not match regex" in {

    val generator: Gen[String] = RegexpGen.from("""[^\d{1,15}|(\d{0,15}.{1}\d{1,3}){1}]""")
    val validRegex: String     = UnloadingRemarksRequest.grossMassRegex
    val expectedError          = FormError(fieldName, invalidKey, Seq(validRegex))

    forAll(generator) {
      invalidString =>
        val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
        result.errors should contain(expectedError)
    }
  }

}
