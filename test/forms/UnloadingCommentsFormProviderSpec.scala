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
import models.messages.UnloadingRemarksRequest.stringFieldRegexComma
import org.scalacheck.Gen
import play.api.data.{Field, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

import scala.util.Random

class UnloadingCommentsFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "changesToReport.error.required"
  val lengthKey   = "changesToReport.error.length"
  val invalidKey  = "changesToReport.error.invalid"
  val maxLength   = 512

  val form = new UnloadingCommentsFormProvider()()

  ".value" - {

    val fieldName = "value"

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
    "must not bind strings with invalid characters" in {

      val generator: Gen[String] = RegexpGen.from(s"[!£^*(){}_+=:;|`~<>±üçñèé]{$length}")

      forAll(generator) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(FormError(fieldName, invalidKey, Seq(stringFieldRegexComma.toString())))
      }
    }

    s"must not bind strings longer than $maxLength characters" in {
      val invalidString = Random.alphanumeric.take(maxLength + 1).mkString

      val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)

      result.errors must contain(FormError(fieldName, lengthKey, List(maxLength)))
    }

  }
}
