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

package viewModels.houseConsignment.index.items.additionalReference

import config.FrontendAppConfig
import models.{ArrivalId, Index, Mode, RichOptionalJsArray, UserAnswers}
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}
import controllers.houseConsignment.index.items.additionalReference.routes
import pages.houseConsignment.index.items.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferencesSection

case class AddAnotherAdditionalReferenceViewModel(listItems: Seq[ListItem],
                                                  onSubmitCall: Call,
                                                  nextIndex: Index,
                                                  houseConsignmentIndex: Index,
                                                  itemIndex: Index
) extends AddAnotherViewModel {
  override val prefix: String                                    = "houseConsignment.index.items.additionalReference.addAnotherAdditionalReference"
  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxAdditionalReferences

  override def title(implicit messages: Messages): String =
    messages(s"$prefix.$emptyOrSingularOrPlural.title", count, houseConsignmentIndex.display, itemIndex.display)

  override def heading(implicit messages: Messages): String =
    messages(s"$prefix.$emptyOrSingularOrPlural.heading", count, houseConsignmentIndex.display, itemIndex.display)

  override def legend(implicit messages: Messages): String = if (count > 0) {
    messages(s"$prefix.label", count, houseConsignmentIndex.display, itemIndex.display)
  } else { messages(s"$prefix.empty.label", count, houseConsignmentIndex.display, itemIndex.display) }

  override def maxLimitLabel(implicit messages: Messages): String =
    messages(s"$prefix.maxLimit.label", count, houseConsignmentIndex.display, itemIndex.display)

}

object AddAnotherAdditionalReferenceViewModel {

  class AddAnotherAdditionalReferenceViewModelProvider {

    def apply(userAnswers: UserAnswers,
              arrivalId: ArrivalId,
              mode: Mode,
              houseConsignmentIndex: Index,
              itemIndex: Index
    ): AddAnotherAdditionalReferenceViewModel = {

      val array = userAnswers.get(AdditionalReferencesSection(houseConsignmentIndex, itemIndex))

      val listItems = array.flatMapWithIndex {
        case (_, additionalReferenceIndex) =>
          lazy val numberString = userAnswers.get(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)) match {
            case None        => ""
            case Some(value) => s"- $value"
          }
          userAnswers.get(AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)).map {
            `type` =>
              ListItem(
                name = s"${`type`.value} $numberString",
                changeUrl = None,
                removeUrl = Some(
                  routes.RemoveAdditionalReferenceYesNoController.onPageLoad(arrivalId, mode, houseConsignmentIndex, itemIndex, additionalReferenceIndex).url
                )
              )
          }
      }.toSeq

      new AddAnotherAdditionalReferenceViewModel(
        listItems,
        onSubmitCall = controllers.houseConsignment.index.items.additionalReference.routes.AddAnotherAdditionalReferenceController
          .onSubmit(arrivalId, mode, houseConsignmentIndex, itemIndex),
        nextIndex = array.nextIndex,
        houseConsignmentIndex,
        itemIndex
      )
    }

  }
}
