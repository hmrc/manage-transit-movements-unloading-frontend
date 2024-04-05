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

package viewModels.houseConsignment.index.items

import config.FrontendAppConfig
import models.{ArrivalId, Index, Mode, RichOptionalJsArray, UserAnswers}
import pages.houseConsignment.index.items.ItemDescriptionPage
import pages.sections.ItemsSection
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherItemViewModel(
  listItems: Seq[ListItem],
  onSubmitCall: Call,
  houseConsignmentIndex: Index,
  nextIndex: Index
) extends AddAnotherViewModel {
  override val prefix: String = "houseConsignment.index.items.addAnotherItem"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxHouseConsignmentItem

  override def maxLimitLabel(implicit messages: Messages): String = messages(s"$prefix.maxLimit.label", houseConsignmentIndex.display)

  override def allowMore(implicit config: FrontendAppConfig): Boolean = count < maxCount

  override def title(implicit messages: Messages): String = messages(s"$prefix.$emptyOrSingularOrPlural.title", count, houseConsignmentIndex.display)

  override def heading(implicit messages: Messages): String = messages(s"$prefix.$emptyOrSingularOrPlural.heading", count, houseConsignmentIndex.display)

  override def legend(implicit messages: Messages): String =
    if (count > 0) messages(s"$prefix.label", count, houseConsignmentIndex.display) else messages(s"$prefix.empty.label", count, houseConsignmentIndex.display)

  def noMoreItemsLabel(implicit messages: Messages): String = messages(s"$prefix.noMoreItems.label", houseConsignmentIndex.display)
}

object AddAnotherItemViewModel {

  class AddAnotherItemViewModelProvider() {

    def apply(userAnswers: UserAnswers, arrivalId: ArrivalId, houseConsignmentIndex: Index, mode: Mode)(implicit
      messages: Messages
    ): AddAnotherItemViewModel = {

      val items = userAnswers.get(ItemsSection(houseConsignmentIndex))

      val listItems = items.mapWithIndex {
        case (_, itemIndex) =>
          def itemNumber(increment: Int) = messages("houseConsignment.index.itemPrefix", increment)

          val name = userAnswers.get(ItemDescriptionPage(houseConsignmentIndex, itemIndex)) flatMap {
            description =>
              Some(s"${itemNumber(itemIndex.display)} - $description")
          } getOrElse {
            itemNumber(itemIndex.display)
          }

          ListItem(
            name = name,
            changeUrl = None,
            removeUrl = Some(
              controllers.houseConsignment.index.items.routes.RemoveConsignmentItemYesNoController
                .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode)
                .url
            )
          )
      }

      new AddAnotherItemViewModel(
        listItems,
        onSubmitCall = controllers.houseConsignment.index.items.routes.AddAnotherItemController.onSubmit(arrivalId, houseConsignmentIndex, mode),
        houseConsignmentIndex,
        nextIndex = items.nextIndex
      )
    }
  }
}
