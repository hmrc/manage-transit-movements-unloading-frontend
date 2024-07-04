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

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class WeightFormProvider @Inject() extends Mappings {

  def apply(prefix: String, decimalPlaceCount: Int, characterCount: Int, isZeroAllowed: Boolean, args: Any*): Form[BigDecimal] =
    apply(prefix, s"$prefix.error.required", decimalPlaceCount, characterCount, isZeroAllowed, args: _*)

  def apply(prefix: String, requiredError: String, decimalPlaceCount: Int, characterCount: Int, isZeroAllowed: Boolean, args: Any*): Form[BigDecimal] =
    Form(
      "value" -> bigDecimal(
        decimalPlaceCount,
        characterCount,
        isZeroAllowed,
        s"$prefix.error",
        requiredError,
        args = args.map(_.toString)
      )
    )
}
