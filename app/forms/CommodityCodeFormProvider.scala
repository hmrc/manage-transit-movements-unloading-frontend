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
import models.Index
import models.messages.UnloadingRemarksRequest
import models.messages.UnloadingRemarksRequest.alphaNumericRegex
import play.api.data.Form

import javax.inject.Inject

class CommodityCodeFormProvider @Inject() extends Mappings {

  def apply(houseConsignmentIndex: Index, itemIndex: Index): Form[String] =
    Form(
      "value" -> text("commodityCode.error.required", Seq(houseConsignmentIndex.display.toString, itemIndex.display.toString))
        .verifying(
          StopOnFirstFail[String](
            exactLength(UnloadingRemarksRequest.commodityCodeLength, "commodityCode.error.length"),
            regexp(alphaNumericRegex.r, "commodityCode.error.invalid", Seq.empty)
          )
        )
    )
}
