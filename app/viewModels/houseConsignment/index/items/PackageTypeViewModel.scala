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

import models.{Index, Mode}
import play.api.i18n.Messages
import viewModels.houseConsignment.index.items.ModeViewModel.ModeViewModelProvider

import javax.inject.Inject

case class PackageTypeViewModel(heading: String, title: String, requiredError: String)

object PackageTypeViewModel {

  class PackageTypeViewModelProvider @Inject() () {

    private def prefix = "houseConsignment.index.item.packageType"

    private def title(modeViewModelProvider: ModeViewModelProvider, houseConsignmentIndex: Index, itemIndex: Index, mode: Mode)(implicit
      messages: Messages
    ): String =
      modeViewModelProvider.apply(prefix, houseConsignmentIndex, itemIndex, mode).title

    private def heading(modeViewModelProvider: ModeViewModelProvider, houseConsignmentIndex: Index, itemIndex: Index, mode: Mode)(implicit
      messages: Messages
    ): String =
      modeViewModelProvider.apply(prefix, houseConsignmentIndex, itemIndex, mode).heading

    private def requiredError(modeViewModelProvider: ModeViewModelProvider, houseConsignmentIndex: Index, itemIndex: Index, mode: Mode)(implicit
      messages: Messages
    ): String =
      modeViewModelProvider.apply(prefix, houseConsignmentIndex, itemIndex, mode).requiredError

    def apply(houseConsignmentIndex: Index, itemIndex: Index, mode: Mode, modeViewModelProvider: ModeViewModelProvider)(implicit
      message: Messages
    ): PackageTypeViewModel =
      new PackageTypeViewModel(
        heading(modeViewModelProvider, houseConsignmentIndex, itemIndex, mode),
        title(modeViewModelProvider, houseConsignmentIndex, itemIndex, mode),
        requiredError(modeViewModelProvider, houseConsignmentIndex, itemIndex, mode)
      )
  }
}
