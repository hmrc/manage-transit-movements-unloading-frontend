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

package viewModels.additionalReference.index

import models.{CheckMode, Index, Mode, NormalMode, RichOptionalJsArray, UserAnswers}
import pages.additionalReference.AdditionalReferenceTypePage
import pages.sections.additionalReference.AdditionalReferencesSection
import play.api.i18n.Messages
import viewModels.ModeViewModelProvider

import javax.inject.Inject

case class AdditionalReferenceNumberViewModel(heading: String, title: String, requiredError: String, isParagraphRequired: Boolean)

object AdditionalReferenceNumberViewModel {

  class AdditionalReferenceNumberViewModelProvider @Inject() extends ModeViewModelProvider {
    override val prefix = "additionalReferenceNumber.index"

    def apply(mode: Mode, additionalReferenceIndex: Index, userAnswers: UserAnswers)(implicit messages: Messages): AdditionalReferenceNumberViewModel =
      new AdditionalReferenceNumberViewModel(
        heading(mode),
        title(mode),
        requiredError(mode),
        isParagraphRequired(mode, additionalReferenceIndex, userAnswers)
      )

    private def isParagraphRequired(mode: Mode, additionalReferenceIndex: Index, userAnswers: UserAnswers) =
      mode match {
        case CheckMode => false
        case NormalMode =>
          val additionalReferenceTypes = {
            val numberOfAdditionalReferences = userAnswers.get(AdditionalReferencesSection).length
            (0 until numberOfAdditionalReferences)
              .map(Index(_))
              .filterNot(_ == additionalReferenceIndex)
              .map(AdditionalReferenceTypePage)
              .flatMap(userAnswers.get(_))
          }
          userAnswers.get(AdditionalReferenceTypePage(additionalReferenceIndex)).exists(additionalReferenceTypes.contains)
      }
  }

}
