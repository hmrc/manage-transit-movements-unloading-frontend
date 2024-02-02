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

import forms.Constants.maxPackageShippingMarkLength
import forms.behaviours.StringFieldBehaviours
import models.messages.UnloadingRemarksRequest.alphaNumericRegex
import org.scalacheck.Gen
import play.api.data.FormError

class PackageShippingMarkFormProviderSpec extends StringFieldBehaviours {

  private val invalidKey  = "houseConsignment.item.packageShippingMark.error.invalid"
  private val requiredKey = "houseConsignment.item.packageShippingMark.error.required"

  private val fieldName = "value"

  val form = new PackageShippingMarkFormProvider()(requiredKey)

  ".value" - {

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.chooseNum(0, maxPackageShippingMarkLength).toString
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, messages(requiredKey))
    )
  }

  behave like fieldWithInvalidCharacters(
    form,
    fieldName,
    error = FormError(fieldName, invalidKey, Seq(alphaNumericRegex)),
    maxPackageShippingMarkLength
  )
}
