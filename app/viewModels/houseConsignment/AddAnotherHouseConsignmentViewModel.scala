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

package viewModels.houseConsignment

import config.FrontendAppConfig
import controllers.houseConsignment.routes
import models.{ArrivalId, Index, Mode, RichOptionalJsArray, UserAnswers}
import pages.sections.HouseConsignmentsSection
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherHouseConsignmentViewModel(
  listItems: Seq[ListItem],
  onSubmitCall: Call,
  nextIndex: Index
) extends AddAnotherViewModel {
  override val prefix: String = "houseConsignment.addAnotherHouseConsignment"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxHouseConsignments

  override def maxLimitLabel(implicit messages: Messages): String = messages(s"$prefix.maxLimit.label")
}

object AddAnotherHouseConsignmentViewModel {

  class AddAnotherHouseConsignmentViewModelProvider() {

    def apply(userAnswers: UserAnswers, arrivalId: ArrivalId, mode: Mode)(implicit messages: Messages): AddAnotherHouseConsignmentViewModel = {

      val array = userAnswers.get(HouseConsignmentsSection)

      val listItems = array.mapWithIndex {
        case (_, hcIndex) =>
          def hcNumber(increment: Int): String = messages("houseConsignment.prefix", increment)

          val name = hcNumber(hcIndex.display)

          ListItem(
            name = name,
            changeUrl = None,
            removeUrl = Some(controllers.houseConsignment.index.routes.RemoveHouseConsignmentYesNoController.onPageLoad(arrivalId, hcIndex, mode).url)
          )
      }

      new AddAnotherHouseConsignmentViewModel(
        listItems,
        onSubmitCall = routes.AddAnotherHouseConsignmentController.onSubmit(arrivalId, mode),
        nextIndex = array.nextIndex
      )
    }
  }
}
