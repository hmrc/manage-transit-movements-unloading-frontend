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

  val nextIndex: Index = Index(count)

  val emptyOrSingularOrPlural: String = count match {
    case 0 => "empty"
    case 1 => "singular"
    case _ => "plural"
  }

  val prefix: String

  def title(implicit messages: Messages): String           = messages(s"$prefix.$emptyOrSingularOrPlural.title", count)
  def heading(implicit messages: Messages): String         = messages(s"$prefix.$emptyOrSingularOrPlural.heading", count)
  def legend(implicit messages: Messages): String          = messages(s"$prefix.label")
  def emptyListLegend(implicit messages: Messages): String = messages(s"$prefix.empty.label")
  def maxLimitLabel(implicit messages: Messages): String   = messages(s"$prefix.maxLimit.label")

  def maxCount(implicit config: FrontendAppConfig): Int
  def allowMore(implicit config: FrontendAppConfig): Boolean = count < maxCount
}
