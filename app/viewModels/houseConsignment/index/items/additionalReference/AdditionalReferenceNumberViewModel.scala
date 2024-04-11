/*
 * Copyright 2024 HM Revenue & Customs
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

package viewModels.houseConsignment.index.items.additionalReference

import models.{ArrivalId, Index, Mode}
import play.api.i18n.Messages
import viewModels.ModeViewModelProvider

import javax.inject.Inject

case class AdditionalReferenceNumberViewModel(heading: String,
                                              title: String,
                                              requiredError: String,
                                              arrivalId: ArrivalId,
                                              mode: Mode,
                                              houseConsignmentIndex: Index,
                                              itemIndex: Index,
                                              additionalReferenceIndex: Index
)

object AdditionalReferenceNumberViewModel {

  class AdditionalReferenceNumberViewModelProvider @Inject() extends ModeViewModelProvider {

    override val prefix = "houseConsignment.index.items.additionalReference.additionalReferenceNumber"

    def apply(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, itemIndex: Index, additionalReferenceIndex: Index)(implicit
      message: Messages
    ): AdditionalReferenceNumberViewModel =
      new AdditionalReferenceNumberViewModel(
        heading(mode, houseConsignmentIndex, itemIndex),
        title(mode, houseConsignmentIndex, itemIndex),
        requiredError(mode, houseConsignmentIndex, itemIndex),
        arrivalId,
        mode,
        houseConsignmentIndex,
        itemIndex,
        additionalReferenceIndex
      )

  }
}
