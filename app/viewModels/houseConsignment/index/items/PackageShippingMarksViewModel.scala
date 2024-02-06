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

package viewModels.houseConsignment.index.items

import models.{CheckMode, Index, Mode, NormalMode}
import play.api.i18n.Messages
import viewModels.houseConsignment.index.items.ModeViewModel.ModeViewModelProvider

import javax.inject.Inject

case class PackageShippingMarksViewModel(heading: String, title: String, requiredError: String)

object PackageShippingMarksViewModel {

  class PackageShippingMarksViewModelProvider @Inject() () {

    private val prefix = "houseConsignment.item.packageShippingMark"

    private def heading(prefix: String, modeViewModelProvider: ModeViewModelProvider, houseConsignmentIndex: Index, itemIndex: Index, mode: Mode)(implicit
      messages: Messages
    ): String =
      modeViewModelProvider.apply(prefix, houseConsignmentIndex, itemIndex, mode).heading

    private def title(prefix: String, modeViewModelProvider: ModeViewModelProvider, houseConsignmentIndex: Index, itemIndex: Index, mode: Mode)(implicit
      messages: Messages
    ): String =
      modeViewModelProvider.apply(prefix, houseConsignmentIndex, itemIndex, mode).title

    private def requiredError(prefix: String, modeViewModelProvider: ModeViewModelProvider, houseConsignmentIndex: Index, itemIndex: Index, mode: Mode)(implicit
      messages: Messages
    ): String =
      modeViewModelProvider.apply(prefix, houseConsignmentIndex, itemIndex, mode).requiredError

    def apply(houseConsignmentIndex: Index, itemIndex: Index, mode: Mode, modeViewModelProvider: ModeViewModelProvider)(implicit
      messages: Messages
    ): PackageShippingMarksViewModel =
      new PackageShippingMarksViewModel(
        heading(prefix, modeViewModelProvider, houseConsignmentIndex, itemIndex, mode),
        title(prefix, modeViewModelProvider, houseConsignmentIndex, itemIndex, mode),
        requiredError(prefix, modeViewModelProvider, houseConsignmentIndex, itemIndex, mode)
      )
  }
}
