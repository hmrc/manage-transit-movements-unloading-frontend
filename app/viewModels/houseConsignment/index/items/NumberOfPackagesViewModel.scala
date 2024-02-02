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

package viewModels.houseConsignment.index.items

import models.{CheckMode, Index, Mode, NormalMode}
import play.api.i18n.Messages

import javax.inject.Inject

case class NumberOfPackagesViewModel(houseConsignmentIndex: Index, itemIndex: Index, mode: Mode) {

  def heading(implicit messages: Messages): String =
    mode match {
      case NormalMode => Messages("numberOfPackages.normalMode.heading")
      case CheckMode  => messages("numberOfPackages.checkMode.heading", houseConsignmentIndex.display, itemIndex.display)
    }

  def title(implicit messages: Messages): String =
    mode match {
      case NormalMode => messages("numberOfPackages.normalMode.title")
      case CheckMode  => messages("numberOfPackages.checkMode.title", houseConsignmentIndex.display, itemIndex.display)
    }

  def requiredError(implicit messages: Messages): String =
    mode match {
      case NormalMode => messages("numberOfPackages.normalMode.error.required")
      case CheckMode  => messages("numberOfPackages.checkMode.error.required", houseConsignmentIndex.display, itemIndex.display)
    }

}

object NumberOfPackagesViewModel {

  class NumberOfPackagesViewModelProvider @Inject() () {

    def apply(houseConsignmentIndex: Index, itemIndex: Index, mode: Mode): NumberOfPackagesViewModel =
      new NumberOfPackagesViewModel(houseConsignmentIndex, itemIndex, mode)
  }
}
