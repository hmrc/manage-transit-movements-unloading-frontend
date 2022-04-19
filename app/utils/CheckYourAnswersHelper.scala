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

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.Format._

class CheckYourAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) {

  def areAnySealsBroken: Option[SummaryListRow] = userAnswers.get(AreAnySealsBrokenPage) map {
    answer =>
      SummaryListRow(
        key = messages("areAnySealsBroken.checkYourAnswersLabel").toKey,
        value = Value(yesOrNo(answer)),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.AreAnySealsBrokenController.onPageLoad(userAnswers.id, CheckMode).url,
                visuallyHiddenText = Some(messages("areAnySealsBroken.change.hidden")),
                attributes = Map("id" -> "change-are-any-seals-broken")
              )
            )
          )
        )
      )
  }

  def canSealsBeRead: Option[SummaryListRow] = userAnswers.get(CanSealsBeReadPage) map {
    answer =>
      SummaryListRow(
        key = messages("canSealsBeRead.checkYourAnswersLabel").toKey,
        value = Value(yesOrNo(answer)),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.CanSealsBeReadController.onPageLoad(userAnswers.id, CheckMode).url,
                visuallyHiddenText = Some(messages("canSealsBeRead.change.hidden")),
                attributes = Map("id" -> "change-can-seals-be-read")
              )
            )
          )
        )
      )
  }

  def seals(seals: Seq[String]): Option[SummaryListRow] = seals match {
    case _ :: _ =>
      Some(
        SummaryListRow(
          key = messages("checkYourAnswers.seals.checkYourAnswersLabel").toKey,
          value = Value(HtmlContent(seals.mkString("<br>"))),
          actions = None
        )
      )
    case _ => None
  }

  def dateGoodsUnloaded: Option[SummaryListRow] = userAnswers.get(DateGoodsUnloadedPage) map {
    answer =>
      SummaryListRow(
        key = messages("dateGoodsUnloaded.checkYourAnswersLabel").toKey,
        value = Value(answer.format(cyaDateFormatter).toText),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.DateGoodsUnloadedController.onPageLoad(userAnswers.id, CheckMode).url,
                visuallyHiddenText = Some(messages("dateGoodsUnloaded.change.hidden")),
                attributes = Map("id" -> "change-date-goods-unloaded")
              )
            )
          )
        )
      )
  }

  private def yesOrNo(answer: Boolean)(implicit messages: Messages): Content =
    messages {
      if (answer) {
        "site.yes"
      } else {
        "site.no"
      }
    }.toText
}
