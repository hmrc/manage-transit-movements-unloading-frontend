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
import generators.Generators
import models.Index
import models.Constants._
import org.scalacheck.Gen
import play.api.data.{Field, FormError}

import scala.util.matching.Regex

class TotalNumberOfPackagesFormProviderSpec extends StringFieldBehaviours with Generators {

  private val requiredKey = "totalNumberOfPackages.error.required"
  private val invalidKey  = "totalNumberOfPackages.error.nonNumeric"
  private val maxLength   = numberOfPackagesLength

  private val index = Index(0)

  private val form      = new TotalNumberOfPackagesFormProvider()(index)
  private val fieldName = "value"

  ".value" - {

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.chooseNum(0, maxLength).toString
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(index.display.toString))
    )
  }

  "must not bind strings that do not match regex" in {

    val expectedError = FormError(fieldName, invalidKey, Seq(index.display))
    val regex         = new Regex("[A-Za-z@~{}><!*&%$]{5}")

    forAll(stringsThatMatchRegex(regex)) {
      invalidString =>
        val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
        result.errors should contain(expectedError)
    }

  }
}
