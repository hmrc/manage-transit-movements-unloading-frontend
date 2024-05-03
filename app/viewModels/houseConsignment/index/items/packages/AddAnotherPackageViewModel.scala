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

package viewModels.houseConsignment.index.items.packages

import config.FrontendAppConfig
import controllers.houseConsignment.index.items.packages.routes
import models.removable.Packaging
import models.{ArrivalId, Index, Mode, RichOptionalJsArray, UserAnswers}
import pages.sections.PackagingListSection
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherPackageViewModel(
  listItems: Seq[ListItem],
  onSubmitCall: Call,
  nextIndex: Index
) extends AddAnotherViewModel {
  override val prefix: String = "houseConsignment.index.items.packages.addAnotherPackage"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxPackages

  def title(houseConsignmentIndex: Index, itemIndex: Index)(implicit messages: Messages): String =
    messages(s"$prefix.$emptyOrSingularOrPlural.title", count, itemIndex.display, houseConsignmentIndex.display)

  def heading(houseConsignmentIndex: Index, itemIndex: Index)(implicit messages: Messages): String =
    messages(s"$prefix.$emptyOrSingularOrPlural.heading", count, itemIndex.display, houseConsignmentIndex.display)

  def legend(houseConsignmentIndex: Index, itemIndex: Index)(implicit messages: Messages): String =
    if (count <= 0) {
      messages(s"$prefix.empty.label", count, itemIndex.display, houseConsignmentIndex.display)
    } else {
      messages(s"$prefix.label", count, itemIndex.display, houseConsignmentIndex.display)
    }

  def maxLimitLabel(houseConsignment: Index, itemIndex: Index)(implicit messages: Messages): String =
    messages(s"$prefix.maxLimit.label", count, itemIndex.display, houseConsignment.display)

}

object AddAnotherPackageViewModel {

  class AddAnotherPackageViewModelProvider {

    def apply(
      userAnswers: UserAnswers,
      arrivalId: ArrivalId,
      houseConsignmentIndex: Index,
      itemIndex: Index,
      houseConsignmentMode: Mode,
      itemMode: Mode
    ): AddAnotherPackageViewModel = {

      val array = userAnswers.get(PackagingListSection(houseConsignmentIndex, itemIndex))

      val listItems = array
        .flatMapWithIndex {
          case (_, packageIndex) =>
            Packaging(userAnswers, houseConsignmentIndex, itemIndex, packageIndex).map {
              packaging =>
                ListItem(
                  name = packaging.forAddAnotherDisplay,
                  changeUrl = None,
                  removeUrl = Some(
                    routes.RemovePackageTypeYesNoController
                      .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode)
                      .url
                  )
                )
            }
        }

      new AddAnotherPackageViewModel(
        listItems,
        onSubmitCall = routes.AddAnotherPackageController.onSubmit(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode),
        nextIndex = array.nextIndex
      )
    }
  }
}
