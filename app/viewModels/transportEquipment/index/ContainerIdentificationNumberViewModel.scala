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

package viewModels.transportEquipment.index

import models.Mode
import play.api.i18n.Messages
import viewModels.ModeViewModelProvider

import javax.inject.Inject

case class ContainerIdentificationNumberViewModel(heading: String, title: String, requiredError: String, paragraph: String)

object ContainerIdentificationNumberViewModel {

  class ContainerIdentificationNumberViewModelProvider @Inject() extends ModeViewModelProvider {

    override val prefix = "containerIdentificationNumber"

    def apply(mode: Mode)(implicit messages: Messages): ContainerIdentificationNumberViewModel =
      new ContainerIdentificationNumberViewModel(heading(mode), title(mode), requiredError(mode), paragraph(mode))

  }
}
