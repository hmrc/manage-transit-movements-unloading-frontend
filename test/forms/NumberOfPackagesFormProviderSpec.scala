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

import forms.Constants.maxNumberOfPackages
import forms.behaviours.FieldBehaviours
import generators.Generators

class NumberOfPackagesFormProviderSpec extends FieldBehaviours with Generators {

  val prefix: String = "testPrefix"

  "NumberOfPackagesFormProvider" - {

    "bind successfully with valid input" in {
      val form     = new NumberOfPackagesFormProvider().apply(prefix)
      val formData = Map("value" -> "10")

      val boundForm = form.bind(formData)

      boundForm.hasErrors mustBe false
      boundForm.get mustBe BigInt(10)
    }

    "fail to bind with a negative value" in {
      val form     = new NumberOfPackagesFormProvider().apply(prefix)
      val formData = Map("value" -> "-5")

      val boundForm = form.bind(formData)

      boundForm.hasErrors mustBe true
      boundForm.errors.head.message mustBe s"$prefix.error.negative"
    }

    "fail to bind with a value exceeding the maximum" in {
      val form     = new NumberOfPackagesFormProvider().apply(prefix, maxNumberOfPackages)
      val formData = Map("value" -> "100000000000000000000")

      val boundForm = form.bind(formData)

      boundForm.hasErrors mustBe true
      boundForm.errors.head.message mustBe s"$prefix.error.maximum"
    }

    "fail to bind with a non-numeric value" in {
      val form     = new NumberOfPackagesFormProvider().apply(prefix)
      val formData = Map("value" -> "abc")

      val boundForm = form.bind(formData)

      boundForm.hasErrors mustBe true
      boundForm.errors.head.message mustBe s"$prefix.error.nonNumeric"
    }

    "fail to bind with a non-whole number value" in {
      val form     = new NumberOfPackagesFormProvider().apply(prefix)
      val formData = Map("value" -> "5.5")

      val boundForm = form.bind(formData)

      boundForm.hasErrors mustBe true
      boundForm.errors.head.message mustBe s"$prefix.error.wholeNumber"
    }

    "fail to bind with a required field missing" in {
      val form     = new NumberOfPackagesFormProvider().apply(prefix)
      val formData = Map.empty[String, String]

      val boundForm = form.bind(formData)

      boundForm.hasErrors mustBe true
      boundForm.errors.head.message mustBe s"$prefix.error.required"
    }

  }
}
