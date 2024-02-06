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

package viewModels.transportEquipment.index.seals

import models.{CheckMode, Mode, NormalMode}
import play.api.i18n.Messages

import javax.inject.Inject

case class SealIdentificationNumberViewModel(heading: String, title: String, requiredError: String)

object SealIdentificationNumberViewModel {

  class SealIdentificationNumberViewModelProvider @Inject() () {

    private def prefix(mode: Mode): String = mode match {
      case NormalMode => "transportEquipment.index.seal.identificationNumber"
      case CheckMode  => "transportEquipment.index.seal.identificationNumber.check"
    }

    private def title(mode: Mode)(implicit messages: Messages): String =
      messages(s"${prefix(mode)}.title")

    private def heading(mode: Mode)(implicit messages: Messages): String =
      messages(s"${prefix(mode)}.heading")

    private def requiredError(mode: Mode)(implicit messages: Messages): String =
      messages(s"${prefix(mode)}.error.required")

    def apply(mode: Mode)(implicit messages: Messages): SealIdentificationNumberViewModel =
      new SealIdentificationNumberViewModel(heading(mode), title(mode), requiredError(mode))
  }
}
