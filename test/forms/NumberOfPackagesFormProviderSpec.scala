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

import forms.behaviours.FieldBehaviours
import generators.Generators
import models.messages.UnloadingRemarksRequest
import org.scalacheck.Gen
import play.api.data.{Field, Form, FormError}

import scala.util.matching.Regex

class NumberOfPackagesFormProviderSpec extends FieldBehaviours with Generators {

  private val maxLength   = UnloadingRemarksRequest.numberOfPackagesLength
  private val invalidKey  = "houseConsignment.index.item.numberOfPackages.error.nonNumeric"
  private val requiredKey = "houseConsignment.index.item.numberOfPackages.error.required"

  val form: Form[String] = new NumberOfPackagesFormProvider()(requiredKey)
  val fieldName          = "value"

  ".value" - {

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.chooseNum(0, maxLength).toString
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  "must not bind strings that do not match regex" in {

    val expectedError = FormError(fieldName, invalidKey, Seq(UnloadingRemarksRequest.numericRegex.regex))
    val regex         = new Regex("[A-Za-z@~{}><!*&%$]{5}")

    forAll(stringsThatMatchRegex(regex)) {
      invalidString =>
        val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
        result.errors must contain(expectedError)
    }
  }
}
