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
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.Messages
import viewModels.houseConsignment.index.items.NumberOfPackagesViewModel

import javax.inject.Inject

class NumberOfPackagesFormProvider @Inject() extends Mappings {

  def apply(mode: Mode, houseConsignmentIndex: Index, itemIndex: Index)(implicit messages: Messages): Form[String] =
    Form(
      "value" -> text(NumberOfPackagesViewModel.requiredError(mode, houseConsignmentIndex, itemIndex))
        .verifying(
          forms.StopOnFirstFail[String](
            regexp(UnloadingRemarksRequest.numericRegex,
                   "numberOfPackages.error.nonNumeric",
                   Seq(houseConsignmentIndex.display.toString, itemIndex.display.toString)
            ),
            maxLength(UnloadingRemarksRequest.numberOfPackagesLength, "numberOfPackages.error.outOfRange")
          )
        )
    )
}
