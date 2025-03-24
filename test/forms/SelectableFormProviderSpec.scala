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
import models.reference.Country
import models.{NormalMode, SelectableList}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.FormError

class SelectableFormProviderSpec extends StringFieldBehaviours with Generators {

  private val prefix      = Gen.alphaNumStr.sample.value
  private val mode        = NormalMode
  private val requiredKey = s"$prefix.$mode.error.required"

  private val selectable1    = arbitrary[Country].sample.value
  private val selectable2    = arbitrary[Country].sample.value
  private val selectableList = SelectableList(Seq(selectable1, selectable2))
  private val arg            = Gen.alphaNumStr.sample.value

  private class FakeFormProvider extends SelectableFormProvider {
    override val field: String = "value"
  }

  private val formProvider = new FakeFormProvider()
  private val form         = formProvider.apply(mode, prefix, selectableList, arg)

  ".value" - {

    val fieldName = formProvider.field

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyString
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(arg))
    )

    "not bind if value does not exist in the list" in {
      val boundForm = form.bind(Map(fieldName -> "foobar"))
      val field     = boundForm(fieldName)
      field.errors mustNot be(empty)
    }

    "bind a value which is in the list" in {
      val boundForm = form.bind(Map(fieldName -> selectable1.value))
      val field     = boundForm(fieldName)
      field.errors must be(empty)
    }
  }
}
