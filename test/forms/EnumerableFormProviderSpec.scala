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

import forms.behaviours.OptionFieldBehaviours
import models.{EnumerableType, Radioable}
import org.scalacheck.Gen
import play.api.data.FormError

class EnumerableFormProviderSpec extends OptionFieldBehaviours {

  private val prefix: String = Gen.alphaNumStr.sample.value

  sealed private trait FakeEnum extends Radioable[FakeEnum] {
    override lazy val messageKeyPrefix: String = FakeEnum.messageKeyPrefix
  }

  private object FakeEnum extends EnumerableType[FakeEnum] {
    lazy val messageKeyPrefix: String = prefix

    case object Foo extends FakeEnum
    case object Bar extends FakeEnum

    override val values: Seq[FakeEnum] = Seq(Foo, Bar)
  }

  private val formProvider = new EnumerableFormProvider()
  private val form         = formProvider.apply[FakeEnum](prefix)

  ".value" - {

    val fieldName   = "value"
    val requiredKey = s"$prefix.error.required"

    behave like optionsField[FakeEnum](
      form,
      fieldName,
      validValues = FakeEnum.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
