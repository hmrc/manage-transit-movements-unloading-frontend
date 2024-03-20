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

package viewModels.transportEquipment

import config.FrontendAppConfig
import models.{ArrivalId, Index, Mode, RichOptionalJsArray, UserAnswers}
import pages.ContainerIdentificationNumberPage
import pages.sections.TransportEquipmentListSection
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherEquipmentViewModel(
  listItems: Seq[ListItem],
  onSubmitCall: Call,
  nextIndex: Index
) extends AddAnotherViewModel {
  override val prefix: String = "transportEquipment.addAnotherEquipment"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxTransportEquipment

  override def maxLimitLabel(implicit messages: Messages): String = messages(s"$prefix.maxLimit.label")

  override def allowMore(implicit config: FrontendAppConfig): Boolean = count < maxCount

  def noMoreItemsLabel(implicit messages: Messages): String = messages(s"$prefix.noMoreItems.label")
}

object AddAnotherEquipmentViewModel {

  class AddAnotherEquipmentViewModelProvider() {

    def apply(userAnswers: UserAnswers, arrivalId: ArrivalId, mode: Mode)(implicit
      messages: Messages
    ): AddAnotherEquipmentViewModel = {

      val equipments = userAnswers.get(TransportEquipmentListSection)

      val listItems = equipments.mapWithIndex {
        case (_, index) =>
          def equipmentPrefix(increment: Int) = messages("transportEquipment.prefix", increment)
          def container(id: String)           = messages("transportEquipment.containerId", id)

          val name = userAnswers.get(ContainerIdentificationNumberPage(index)) flatMap {
            identificationNumber =>
              Some(s"${equipmentPrefix(index.display)} - ${container(identificationNumber)}")
          } getOrElse {
            s"${equipmentPrefix(index.display)}"
          }

          ListItem(
            name = name,
            changeUrl = None,
            removeUrl = Some(controllers.transportEquipment.index.routes.RemoveTransportEquipmentYesNoController.onPageLoad(arrivalId, mode, index).url)
          )
      }

      new AddAnotherEquipmentViewModel(
        listItems,
        onSubmitCall = controllers.transportEquipment.routes.AddAnotherEquipmentController.onSubmit(arrivalId, mode),
        nextIndex = equipments.nextIndex
      )
    }
  }
}
