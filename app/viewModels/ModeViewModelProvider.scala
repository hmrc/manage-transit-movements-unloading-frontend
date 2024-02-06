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

package viewModels

import models.{Index, Mode}
import play.api.i18n.Messages

trait ModeViewModelProvider {

  def prefix: String

  def heading(mode: Mode, houseConsignmentIndex: Index, itemIndex: Index)(implicit messages: Messages): String =
    messages(s"$prefix.${mode.toString}.heading", houseConsignmentIndex.display, itemIndex.display)

  def title(mode: Mode, houseConsignmentIndex: Index, itemIndex: Index)(implicit messages: Messages): String =
    messages(s"$prefix.${mode.toString}.title", houseConsignmentIndex.display, itemIndex.display)

  def requiredError(mode: Mode, houseConsignmentIndex: Index, itemIndex: Index)(implicit messages: Messages): String =
    messages(s"$prefix.${mode.toString}.errorRequired", houseConsignmentIndex.display, itemIndex.display)

}
