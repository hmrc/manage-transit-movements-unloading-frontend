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
import models.NormalMode
import models.messages.UnloadingRemarksRequest
import org.scalacheck.Gen
import play.api.data.{Field, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

class VehicleIdentificationNumberFormProviderSpec extends StringFieldBehaviours {

  private val prefix: String = Gen.alphaNumStr.sample.value
  private val mode           = NormalMode
  private val requiredKey    = s"$prefix.NormalMode.error.required"
  private val maxLength      = UnloadingRemarksRequest.vehicleIdentificationNumberMaxLength
  private val invalidKey     = s"$prefix.error.invalid"
  private val form           = new VehicleIdentificationNumberFormProvider()(prefix, mode)
  private val fieldName      = "value"

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

    "must not bind strings that do not match regex" in {

      val expectedError          = FormError(fieldName, invalidKey)
      val generator: Gen[String] = RegexpGen.from(s"[!£^*(){}_+=:;|`~,±üçñèé@]{35}")

      forAll(generator) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }
  }
}
