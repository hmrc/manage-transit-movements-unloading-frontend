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

package utils

import controllers.routes
import models.{CheckMode, Seal, UserAnswers}
import pages._
import play.api.i18n.Messages
import queries.SealsQuery
import uk.gov.hmrc.govukfrontend.views.viewmodels.content._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import java.time.LocalDate

class CheckYourAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) extends AnswersHelper(userAnswers) {

  def areAnySealsBroken: Option[SummaryListRow] =
    getAnswerAndBuildRow[Boolean](
      page = AreAnySealsBrokenPage,
      formatAnswer = formatAsYesOrNo,
      prefix = "areAnySealsBroken",
      id = Some("change-are-any-seals-broken"),
      call = Some(routes.AreAnySealsBrokenController.onPageLoad(userAnswers.id, CheckMode))
    )

  def canSealsBeRead: Option[SummaryListRow] =
    getAnswerAndBuildRow[Boolean](
      page = CanSealsBeReadPage,
      formatAnswer = formatAsYesOrNo,
      prefix = "canSealsBeRead",
      id = Some("change-can-seals-be-read"),
      call = Some(controllers.p5.routes.CanSealsBeReadController.onPageLoad(userAnswers.id, CheckMode))
    )

  def seals: Option[SummaryListRow] =
    getAnswerAndBuildRow[Seq[Seal]](
      page = SealsQuery,
      formatAnswer = x => HtmlContent(x.map(_.sealId).mkString("<br>")),
      prefix = "checkYourAnswers.seals",
      id = None,
      call = None
    )

  def dateGoodsUnloaded: Option[SummaryListRow] =
    getAnswerAndBuildRow[LocalDate](
      page = DateGoodsUnloadedPage,
      formatAnswer = formatAsDate,
      prefix = "dateGoodsUnloaded",
      id = Some("change-date-goods-unloaded"),
      call = Some(routes.DateGoodsUnloadedController.onPageLoad(userAnswers.id, CheckMode))
    )
}
