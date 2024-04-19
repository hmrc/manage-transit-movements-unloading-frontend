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

import forms.behaviours.BigDecimalFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class NetWeightFormProviderSpec extends BigDecimalFieldBehaviours {

  private val requiredKey       = "netWeight.error.required"
  private val invalidCharacters = "netWeight.error.characters"
  private val decimalPoint      = "netWeight.error.decimal"
  private val maxLength         = "netWeight.error.length"

  val generatedBigDecimal: Gen[BigDecimal] = Gen.choose(BigDecimal(1), maxValue)

  private val form      = new NetWeightFormProvider()(houseConsignmentIndex, itemIndex)
  private val args      = Seq(s"${itemIndex.display}", s"${houseConsignmentIndex.display}")
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
      invalidFormatError = FormError(fieldName, decimalPoint),
      invalidValueError = FormError(fieldName, maxLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, args)
    )
  }

}
