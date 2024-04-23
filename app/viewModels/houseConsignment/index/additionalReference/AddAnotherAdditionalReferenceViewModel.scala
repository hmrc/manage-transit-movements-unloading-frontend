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

package viewModels.houseConsignment.index.additionalReference

import config.FrontendAppConfig
import controllers.houseConsignment.index.additionalReference.routes
import models.removable.AdditionalReference
import models.{ArrivalId, Index, Mode, RichOptionalJsArray, UserAnswers}
import pages.sections.houseConsignment.index.additionalReference.AdditionalReferenceListSection
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherAdditionalReferenceViewModel(
  listItems: Seq[ListItem],
  onSubmitCall: Call,
  nextIndex: Index,
  houseConsignmentIndex: Index
) extends AddAnotherViewModel {
  override val prefix: String = "houseConsignment.index.additionalReference.addAnotherAdditionalReference"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxAdditionalReferences

  override def title(implicit messages: Messages): String =
    messages(s"$prefix.$emptyOrSingularOrPlural.title", count, houseConsignmentIndex.display)

  override def heading(implicit messages: Messages): String =
    messages(s"$prefix.$emptyOrSingularOrPlural.heading", count, houseConsignmentIndex.display)

  override def legend(implicit messages: Messages): String = if (count > 0) {
    messages(s"$prefix.label", count, houseConsignmentIndex.display)
  } else { messages(s"$prefix.empty.label", count, houseConsignmentIndex.display) }

  override def maxLimitLabel(implicit messages: Messages): String =
    messages(s"$prefix.maxLimit.label", count, houseConsignmentIndex.display)

}

object AddAnotherAdditionalReferenceViewModel {

  class AddAnotherAdditionalReferenceViewModelProvider {

    def apply(
      userAnswers: UserAnswers,
      arrivalId: ArrivalId,
      mode: Mode,
      houseConsignmentIndex: Index
    ): AddAnotherAdditionalReferenceViewModel = {

      val array = userAnswers.get(AdditionalReferenceListSection(houseConsignmentIndex))

      val listItems = array.flatMapWithIndex {
        case (_, additionalReferenceIndex) =>
          AdditionalReference(userAnswers, houseConsignmentIndex, additionalReferenceIndex).map {
            additionalReference =>
              ListItem(
                name = additionalReference.forAddAnotherDisplay,
                changeUrl = None,
                removeUrl = None // TODO: Update once remove controller done
              )
          }
      }

      new AddAnotherAdditionalReferenceViewModel(
        listItems,
        onSubmitCall = routes.AddAnotherAdditionalReferenceController.onSubmit(arrivalId, mode, houseConsignmentIndex),
        nextIndex = array.nextIndex,
        houseConsignmentIndex
      )
    }
  }
}
