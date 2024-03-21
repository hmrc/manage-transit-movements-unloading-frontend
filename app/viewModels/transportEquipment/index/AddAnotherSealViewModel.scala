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

package viewModels.transportEquipment.index

import config.FrontendAppConfig
import controllers.transportEquipment.index.seals.routes
import models.{ArrivalId, Index, Mode, RichOptionalJsArray, UserAnswers}
import pages.sections.SealsSection
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import play.api.i18n.Messages
import play.api.libs.json.JsArray
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherSealViewModel(listItems: Seq[ListItem], onSubmitCall: Call, equipmentIndex: Index, nextIndex: Index) extends AddAnotherViewModel {
  override val prefix: String = "transportEquipment.index.addAnotherSeal"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxSeals

  override def title(implicit messages: Messages): String = messages(s"$prefix.$emptyOrSingularOrPlural.title", count, equipmentIndex.display)

  override def heading(implicit messages: Messages): String = messages(s"$prefix.$emptyOrSingularOrPlural.heading", count, equipmentIndex.display)

  override def legend(implicit messages: Messages): String =
    if (count > 0) messages(s"$prefix.label", count, equipmentIndex.display) else messages(s"$prefix.empty.label", count, equipmentIndex.display)

  override def maxLimitLabel(implicit messages: Messages): String = messages(s"$prefix.maxLimit.label", equipmentIndex.display)

}

object AddAnotherSealViewModel {

  class AddAnotherSealViewModelProvider() {

    def apply(userAnswers: UserAnswers, arrivalId: ArrivalId, equipmentMode: Mode, sealMode: Mode, equipmentIndex: Index): AddAnotherSealViewModel = {

      val array = userAnswers.get(SealsSection(equipmentIndex))

      val listItems = array
        .getOrElse(JsArray())
        .value
        .zipWithIndex
        .flatMap {
          case (_, i) =>
            val sealIndex = Index(i)
            userAnswers.get(SealIdentificationNumberPage(equipmentIndex, sealIndex)).map {
              number =>
                ListItem(
                  name = number,
                  changeUrl = None,
                  removeUrl = Some(routes.RemoveSealYesNoController.onPageLoad(arrivalId, equipmentMode, sealMode, equipmentIndex, sealIndex).url)
                )
            }
        }
        .toSeq

      new AddAnotherSealViewModel(
        listItems,
        onSubmitCall = controllers.transportEquipment.index.routes.AddAnotherSealController.onSubmit(arrivalId, equipmentMode, sealMode, equipmentIndex),
        equipmentIndex = equipmentIndex,
        nextIndex = array.nextIndex
      )
    }
  }
}
