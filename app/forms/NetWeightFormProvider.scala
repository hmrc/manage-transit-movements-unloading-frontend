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
import play.api.data.Form

import javax.inject.Inject

class NetWeightFormProvider @Inject() extends Mappings {

  def apply(houseConsignmentIndex: Index = Index(0), index: Index = Index(0)): Form[String] =
    Form(
      "value" -> text(s"netWeight.error.required", args = Seq(s"${index.display}", s"${houseConsignmentIndex.display}"))
        .verifying(
          StopOnFirstFail[String](
            maxLength(UnloadingRemarksRequest.weightLength, "netWeight.error.length"),
            regexp(UnloadingRemarksRequest.weightCharsRegex, "netWeight.error.characters"),
            regexp(UnloadingRemarksRequest.weightRegex, "netWeight.error.decimal")
          )
        )
    )
}
