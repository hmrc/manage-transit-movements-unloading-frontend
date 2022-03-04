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
import uk.gov.hmrc.viewmodels.SummaryList._
import uk.gov.hmrc.viewmodels.Text.Literal
import uk.gov.hmrc.viewmodels._
import utils.Format._

class CheckYourAnswersHelper(userAnswers: UserAnswers) {

  def areAnySealsBroken: Option[Row] = userAnswers.get(AreAnySealsBrokenPage) map {
    answer =>
      Row(
        key = Key(msg"areAnySealsBroken.checkYourAnswersLabel"),
        value = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.AreAnySealsBrokenController.onPageLoad(userAnswers.id, CheckMode).url,
            visuallyHiddenText = Some(msg"areAnySealsBroken.checkYourAnswersLabel"),
            attributes = Map("id" -> "change-are-any-seals-broken")
          )
        )
      )
  }

  def canSealsBeRead: Option[Row] = userAnswers.get(CanSealsBeReadPage) map {
    answer =>
      Row(
        key = Key(msg"canSealsBeRead.checkYourAnswersLabel"),
        value = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.CanSealsBeReadController.onPageLoad(userAnswers.id, CheckMode).url,
            visuallyHiddenText = Some(msg"canSealsBeRead.checkYourAnswersLabel"),
            attributes = Map("id" -> "change-can-seals-be-read")
          )
        )
      )
  }

  def seals(seals: Seq[String]): Option[Row] = seals match {
    case _ :: _ =>
      Some(
        Row(
          key = Key(msg"checkYourAnswers.seals.checkYourAnswersLabel"),
          value = Value(Html(seals.mkString("<br>"))),
          actions = Nil
        )
      )
    case _ => None
  }

  def dateGoodsUnloaded: Option[Row] = userAnswers.get(DateGoodsUnloadedPage) map {
    answer =>
      Row(
        key = Key(msg"dateGoodsUnloaded.checkYourAnswersLabel"),
        value = Value(Literal(answer.format(cyaDateFormatter))),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.DateGoodsUnloadedController.onPageLoad(userAnswers.id, CheckMode).url,
            visuallyHiddenText = Some(msg"dateGoodsUnloaded.visually.hidden"),
            attributes = Map("id" -> "change-date-goods-unloaded")
          )
        )
      )
  }

  private def yesOrNo(answer: Boolean): Content =
    if (answer) {
      msg"site.yes"
    } else {
      msg"site.no"
    }
}
