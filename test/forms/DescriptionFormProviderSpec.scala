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

import forms.Constants.maxItemDescriptionLength
import forms.behaviours.StringFieldBehaviours
import models.messages.UnloadingRemarksRequest.stringFieldRegexComma
import org.scalacheck.Gen
import play.api.data.{Field, FormError}

class DescriptionFormProviderSpec extends StringFieldBehaviours {

  private val prefix      = "houseConsignment.item.description"
  private val requiredKey = s"$prefix.error.required"
  private val invalidKey  = s"$prefix.error.invalidCharacters"
  private val lengthKey   = s"$prefix.error.length"

  private val fieldName = "value"
  private val form      = new DescriptionFormProvider()(requiredKey)

  val overLength: Gen[String] = stringsLongerThan(maxItemDescriptionLength)

  ".value" - {

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxItemDescriptionLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidCharacters(
      form,
      fieldName,
      error = FormError(fieldName, invalidKey, Seq(stringFieldRegexComma.regex)),
      maxItemDescriptionLength
    )

    "must not bind strings over max length" in {
      val expectedError = FormError(fieldName, lengthKey, Seq(maxItemDescriptionLength))

      val gen = for {
        description <- stringsLongerThan(maxItemDescriptionLength, Gen.alphaNumChar)
      } yield description

      forAll(gen) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }
  }
}
