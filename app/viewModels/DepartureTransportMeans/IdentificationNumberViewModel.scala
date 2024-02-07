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

package viewModels.DepartureTransportMeans

import models.{Index, Mode}
import play.api.i18n.Messages
import viewModels.ModeViewModelProvider

import javax.inject.Inject

case class IdentificationNumberViewModel(heading: String, title: String, requiredError: String)

object IdentificationNumberViewModel {

  class IdentificationNumberViewModelProvider @Inject() extends ModeViewModelProvider {

    override def prefix = "departureMeansOfTransport.vehicleIdentificationNumber"

    def apply(mode: Mode, transportMeansIndex: Index)(implicit message: Messages): IdentificationNumberViewModel =
      new IdentificationNumberViewModel(
        heading(mode, transportMeansIndex),
        title(mode, transportMeansIndex),
        requiredError(mode, transportMeansIndex)
      )
  }
}
