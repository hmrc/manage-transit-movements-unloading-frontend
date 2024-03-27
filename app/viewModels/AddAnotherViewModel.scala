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

import config.FrontendAppConfig
import models.Index
import play.api.i18n.Messages

trait AddAnotherViewModel {
  val listItems: Seq[ListItem]
  val count: Int = listItems.length

  val nextIndex: Index

  val emptyOrSingularOrPlural: String = count match {
    case 0 => "empty"
    case 1 => "singular"
    case _ => "plural"
  }

  val prefix: String

  def title(implicit messages: Messages): String         = messages(s"$prefix.$emptyOrSingularOrPlural.title", count)
  def heading(implicit messages: Messages): String       = messages(s"$prefix.$emptyOrSingularOrPlural.heading", count)
  def legend(implicit messages: Messages): String        = if (count > 0) messages(s"$prefix.label") else messages(s"$prefix.empty.label")
  def maxLimitLabel(implicit messages: Messages): String = messages(s"$prefix.maxLimit.label")

  def title(houseConsignmentIndex: Index, itemIndex: Index)(implicit messages: Messages): String =
    messages(s"$prefix.$emptyOrSingularOrPlural.title", count, itemIndex.display, houseConsignmentIndex.display)

  def heading(houseConsignmentIndex: Index, itemIndex: Index)(implicit messages: Messages): String =
    messages(s"$prefix.$emptyOrSingularOrPlural.heading", count, itemIndex.display, houseConsignmentIndex.display)

  def legend(houseConsignmentIndex: Index, itemIndex: Index)(implicit messages: Messages): String =
    if (count <= 0) {
      messages(s"$prefix.empty.label", count, itemIndex.display, houseConsignmentIndex.display)
    } else {
      messages(s"$prefix.label", count, itemIndex.display, houseConsignmentIndex.display)
    }

  def maxLimitLabel(houseConsignment: Index, itemIndex: Index)(implicit messages: Messages): String =
    messages(s"$prefix.maxLimit.label", count, itemIndex.display, houseConsignment.display)

  def maxCount(implicit config: FrontendAppConfig): Int
  def allowMore(implicit config: FrontendAppConfig): Boolean = count < maxCount
}
