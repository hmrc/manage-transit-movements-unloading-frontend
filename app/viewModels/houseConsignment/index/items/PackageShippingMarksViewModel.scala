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

case class PackageShippingMarksViewModel(heading: String, title: String, requiredError: String)

object PackageShippingMarksViewModel {

  class PackageShippingMarksViewModelProvider @Inject() () extends ViewModelProvider {

    override def prefix(mode: Mode): String = mode match {
      case CheckMode  => "houseConsignment.item.packageShippingMark.checkMode"
      case NormalMode => "houseConsignment.item.packageShippingMark.normalMode"
    }

    def apply(houseConsignmentIndex: Index, itemIndex: Index, mode: Mode)(implicit messages: Messages): PackageShippingMarksViewModel =
      new PackageShippingMarksViewModel(
        heading(houseConsignmentIndex, itemIndex, mode),
        title(houseConsignmentIndex, itemIndex, mode),
        requiredError(houseConsignmentIndex, itemIndex, mode)
      )
  }
}
