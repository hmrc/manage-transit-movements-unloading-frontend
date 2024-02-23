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

import models.{ArrivalId, CheckMode, Index, Mode, NormalMode, RichOptionalJsArray, UserAnswers}
import pages.houseConsignment.index.items.additionalReference.AdditionalReferencePage
import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferencesSection
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
                                              additionalReferenceIndex: Index,
                                              isParagraphRequired: Boolean
)

object AdditionalReferenceNumberViewModel {

  class AdditionalReferenceNumberViewModelProvider @Inject() extends ModeViewModelProvider {

    override val prefix = "houseConsignment.index.items.additionalReference.additionalReferenceNumber"

    def apply(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, itemIndex: Index, additionalReferenceIndex: Index, userAnswers: UserAnswers)(
      implicit message: Messages
    ): AdditionalReferenceNumberViewModel =
      new AdditionalReferenceNumberViewModel(
        heading(mode, houseConsignmentIndex, itemIndex),
        title(mode, houseConsignmentIndex, itemIndex),
        requiredError(mode, houseConsignmentIndex, itemIndex),
        arrivalId,
        mode,
        houseConsignmentIndex,
        itemIndex,
        houseConsignmentIndex,
        isParagraphRequired(mode, houseConsignmentIndex, itemIndex, additionalReferenceIndex, userAnswers)
      )

    private def isParagraphRequired(mode: Mode, houseConsignmentIndex: Index, itemIndex: Index, additionalReferenceIndex: Index, userAnswers: UserAnswers) =
      mode match {
        case CheckMode => false
        case NormalMode =>
          val additionalReferenceTypes = {
            val numberOfAdditionalReferences = userAnswers.get(AdditionalReferencesSection(houseConsignmentIndex, itemIndex)).length
            (0 until numberOfAdditionalReferences)
              .map(Index(_))
              .filterNot(_ == houseConsignmentIndex)
              .map(AdditionalReferencePage(houseConsignmentIndex, itemIndex, _))
              .flatMap(userAnswers.get(_))
          }
          userAnswers.get(AdditionalReferencePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)).exists(additionalReferenceTypes.contains)
      }

  }
}
