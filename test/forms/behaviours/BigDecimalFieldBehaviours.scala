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

package forms.behaviours

import play.api.data.{Form, FormError}

trait BigDecimalFieldBehaviours extends FieldBehaviours {

  val maxValue: BigDecimal = BigDecimal("10000000000000000")

  def bigDecimalField(
    form: Form[_],
    fieldName: String,
    invalidCharactersError: FormError,
    invalidFormatError: FormError,
    invalidValueError: FormError
  ): Unit = {

    "must not bind non-numeric numbers" in {
      forAll(nonNumerics -> "nonNumeric") {
        nonNumeric =>
          val result = form.bind(Map(fieldName -> nonNumeric)).apply(fieldName)
          result.errors mustEqual Seq(invalidCharactersError)
      }
    }

    "must bind decimals" in {
      forAll(decimals -> "decimal") {
        decimal =>
          val result = form.bind(Map(fieldName -> decimal)).apply(fieldName)
          result.value.value mustBe decimal
      }
    }

    s"must not bind values greater than or equal to $maxValue" in {
      val result = form.bind(Map(fieldName -> maxValue.toString)).apply(fieldName)
      result.errors mustEqual Seq(invalidValueError)
    }

    "must not bind negative values" in {
      val result = form.bind(Map(fieldName -> BigDecimal(-1).toString)).apply(fieldName)
      result.errors mustEqual Seq(invalidCharactersError)
    }

    "must not bind values that end in a full stop" in {
      val result = form.bind(Map(fieldName -> "123456789012345.")).apply(fieldName)
      result.errors mustEqual Seq(invalidFormatError)
    }

    "must not bind values with more than 6 decimal places" in {
      val result = form.bind(Map(fieldName -> BigDecimal(1.1234567).toString)).apply(fieldName)
      result.errors mustEqual Seq(invalidFormatError)
    }

    "must not bind values with more than 16 characters" in {
      val result = form.bind(Map(fieldName -> BigDecimal(1234567890123.456).toString)).apply(fieldName)
      result.errors mustEqual Seq(invalidValueError)
    }
  }
}
