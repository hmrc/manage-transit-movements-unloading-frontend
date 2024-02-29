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
import models.{ArrivalId, Index, Mode, UserAnswers}
import pages.sections.SealsSection
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import play.api.libs.json.JsArray
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherSealViewModel(listItems: Seq[ListItem], onSubmitCall: Call) extends AddAnotherViewModel {
  override val prefix: String = "transportEquipment.index.addAnotherSeal"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxSeals
}

object AddAnotherSealViewModel {

  class AddAnotherSealViewModelProvider() {

    def apply(userAnswers: UserAnswers, arrivalId: ArrivalId, mode: Mode, equipmentIndex: Index): AddAnotherSealViewModel = {

      val listItems = userAnswers
        .get(SealsSection(equipmentIndex))
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
                  changeUrl = Some(routes.SealIdentificationNumberController.onPageLoad(arrivalId, mode, equipmentIndex, sealIndex).url),
                  removeUrl = Some(routes.RemoveSealYesNoController.onPageLoad(arrivalId, mode, equipmentIndex, sealIndex).url)
                )
            }
        }
        .toSeq

      new AddAnotherSealViewModel(
        listItems,
        onSubmitCall = controllers.transportEquipment.index.routes.AddAnotherSealController.onSubmit(arrivalId, mode, equipmentIndex)
      )
    }
  }
}
