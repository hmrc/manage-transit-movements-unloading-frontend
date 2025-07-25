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
import models.messages.UnloadingRemarksRequest.{commodityCodeLength, numericRegex}
import org.scalacheck.Gen
import play.api.data.{Field, Form, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

class CommodityCodeFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "houseConsignment.commodityCode.error.required"
  private val lengthKey   = "houseConsignment.commodityCode.error.length"
  private val invalidKey  = "houseConsignment.commodityCode.error.invalid"

  def form: Form[String] = new CommodityCodeFormProvider()(requiredKey)
  private val fieldName  = "value"

  ".value" - {

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(commodityCodeLength)
    )

    behave like fieldWithExactLength(
      form,
      fieldName,
      exactLength = commodityCodeLength,
      lengthError = FormError(fieldName, lengthKey, Seq(commodityCodeLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind strings that do not match regex" in {

      val expectedError          = FormError(fieldName, invalidKey, Seq(numericRegex.regex))
      val generator: Gen[String] = RegexpGen.from(s"[!£^*(){}_+=:;|`~,±üçñèé@]{6}")

      forAll(generator) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }

    "must result in 'invalid characters' error when input is incorrect length and has invalid characters" in {
      val result: Field = form.bind(Map(fieldName -> "abcdefg")).apply(fieldName)
      result.errors mustEqual Seq(
        FormError(fieldName, invalidKey, Seq(numericRegex.regex))
      )
    }
  }
}
