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

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form

class NumberOfPackagesFormProvider @Inject() extends Mappings {

  def apply(prefix: String, minimum: BigInt = 0, args: Seq[String] = Seq.empty): Form[BigInt] =
    Form(
      "value" -> bigInt(s"$prefix.error.required", s"$prefix.error.wholeNumber", s"$prefix.error.nonNumeric", args = args)
        .verifying(
          StopOnFirstFail[BigInt](
            minimumValue(minimum, s"$prefix.error.negative"),
            maximumValue(maxNumberOfPackages, s"$prefix.error.maximum")
          )
        )
    )
}
