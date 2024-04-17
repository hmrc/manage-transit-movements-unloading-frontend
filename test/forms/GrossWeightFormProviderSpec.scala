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

import forms.Constants._
import forms.behaviours.BigDecimalFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class GrossWeightFormProviderSpec extends BigDecimalFieldBehaviours {

  private val prefix            = Gen.alphaNumStr.sample.value
  private val requiredKey       = s"$prefix.error.required"
  private val invalidCharacters = s"$prefix.error.invalidCharacters"
  private val invalidFormat     = s"$prefix.error.invalidFormat"
  private val invalidValue      = s"$prefix.error.invalidValue"

  val generatedBigDecimal: Gen[BigDecimal] = Gen.choose(BigDecimal(1), maxValue)

  private val form      = new GrossWeightFormProvider()(prefix, grossWeightDecimalPlaces, grossWeightIntegerLength)
  private val fieldName = "value"

  ".value" - {

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      generatedBigDecimal.toString
    )

    behave like bigDecimalField(
      form,
      fieldName,
      invalidCharactersError = FormError(fieldName, invalidCharacters),
      invalidFormatError = FormError(fieldName, invalidFormat),
      invalidValueError = FormError(fieldName, invalidValue)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
