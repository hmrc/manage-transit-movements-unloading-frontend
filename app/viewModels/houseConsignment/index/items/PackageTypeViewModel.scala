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

import javax.inject.Inject

case class PackageTypeViewModel(heading: String, title: String)

object PackageTypeViewModel {

  class PackageTypeViewModelProvider @Inject() () {

    private def prefix(mode: Mode): String = mode match {
      case NormalMode => "houseConsignment.index.item.packageType"
      case CheckMode  => "houseConsignment.index.item.packageType.check"
    }

    private def title(houseConsignmentIndex: Index, itemIndex: Index, mode: Mode)(implicit messages: Messages): String =
      messages(s"${prefix(mode)}.title", itemIndex.display, houseConsignmentIndex.display)

    private def heading(houseConsignmentIndex: Index, itemIndex: Index, mode: Mode)(implicit messages: Messages): String =
      messages(s"${prefix(mode)}.heading", itemIndex.display, houseConsignmentIndex.display)

    def apply(mode: Mode, itemIndex: Index, houseConsignmentIndex: Index)(implicit messages: Messages): PackageTypeViewModel =
      new PackageTypeViewModel(heading(houseConsignmentIndex, itemIndex, mode), title(houseConsignmentIndex, itemIndex, mode))
  }
}
