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
import models.messages.UnloadingRemarksRequest
import models.messages.UnloadingRemarksRequest.numericRegex
import play.api.data.Form

import javax.inject.Inject

class CommodityCodeFormProvider @Inject() extends Mappings {

  def apply(requiredError: String): Form[String] = {
    val prefix = "houseConsignment.commodityCode"
    Form(
      "value" -> text(requiredError)
        .verifying(
          StopOnFirstFail[String](
            exactLength(UnloadingRemarksRequest.commodityCodeLength, s"$prefix.error.length"),
            regexp(numericRegex, s"$prefix.error.invalid", Seq.empty)
          )
        )
    )
  }
}
