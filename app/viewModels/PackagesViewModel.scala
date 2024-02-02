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

package viewModels

import models.{CheckMode, Index, Mode, NormalMode}
import play.api.i18n.Messages

case object PackagesViewModel {

  private val normalMode = "houseConsignment.item.packageShippingMark.normalMode"
  private val checkMode  = "houseConsignment.item.packageShippingMark.checkMode"

  def heading(mode: Mode, houseConsignmentIndex: Index, itemIndex: Index)(implicit messages: Messages): String =
    mode match {
      case NormalMode => Messages(s"$normalMode.heading")
      case CheckMode  => messages(s"$checkMode.heading", houseConsignmentIndex.display, itemIndex.display)
    }

  def title(mode: Mode, houseConsignmentIndex: Index, itemIndex: Index)(implicit messages: Messages): String =
    mode match {
      case NormalMode => messages(s"$normalMode.title")
      case CheckMode  => messages(s"$checkMode.title", houseConsignmentIndex.display, itemIndex.display)
    }

  def requiredError(mode: Mode, houseConsignmentIndex: Index, itemIndex: Index)(implicit messages: Messages): String =
    mode match {
      case NormalMode => messages(s"$normalMode.error.required")
      case CheckMode  => messages(s"$checkMode.error.required", houseConsignmentIndex.display, itemIndex.display)
    }

}