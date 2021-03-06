/*
 * Copyright 2022 HM Revenue & Customs
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

package utils

import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.html.components._
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import utils.Format.cyaDateFormatter

import java.time.LocalDate

class SummaryListRowHelper(implicit messages: Messages) {

  def formatAsYesOrNo(answer: Boolean): Content =
    messages {
      if (answer) {
        "site.yes"
      } else {
        "site.no"
      }
    }.toText

  def formatAsDate(answer: LocalDate): Content =
    answer.format(cyaDateFormatter).toText

  def buildRow(
    prefix: String,
    answer: Content,
    id: Option[String],
    call: Option[Call],
    args: Any*
  ): SummaryListRow =
    SummaryListRow(
      key = messages(s"$prefix.checkYourAnswersLabel", args: _*).toKey,
      value = Value(answer),
      actions = call.map {
        x =>
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = x.url,
                visuallyHiddenText = Some(messages(s"$prefix.change.hidden", args: _*)),
                attributes = id.fold[Map[String, String]](Map.empty)(
                  id => Map("id" -> id)
                )
              )
            )
          )
      }
    )

  def buildRemovableRow(
    prefix: String,
    answer: Content,
    id: String,
    changeCall: Call,
    removeCall: Call,
    args: Any*
  ): SummaryListRow =
    SummaryListRow(
      key = messages(s"$prefix.checkYourAnswersLabel", args: _*).toKey,
      value = Value(answer),
      actions = Some(
        Actions(items =
          List(
            ActionItem(
              content = messages("site.edit").toText,
              href = changeCall.url,
              visuallyHiddenText = Some(messages(s"$prefix.change.hidden", args: _*)),
              attributes = Map("id" -> s"change-$id")
            ),
            ActionItem(
              content = messages("site.delete").toText,
              href = removeCall.url,
              visuallyHiddenText = Some(messages(s"$prefix.remove.hidden", args: _*)),
              attributes = Map("id" -> s"remove-$id")
            )
          )
        )
      )
    )
}
