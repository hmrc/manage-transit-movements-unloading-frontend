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

import models.{CheckMode, UserAnswers}
import pages.{AreAnySealsBrokenPage, CanSealsBeReadPage, DateGoodsUnloadedPage, UnloadingCommentsPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import java.time.LocalDate

class CheckYourAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) extends AnswersHelper(userAnswers) {

  def goodsUnloadedDate: Option[SummaryListRow] = getAnswerAndBuildRow[LocalDate](
    page = DateGoodsUnloadedPage,
    formatAnswer = formatAsDate,
    prefix = "checkYourAnswers.rowHeadings.goodsUnloadedDate",
    id = Some("change-goods-unloaded-date"),
    call = Some(controllers.routes.DateGoodsUnloadedController.onPageLoad(arrivalId, CheckMode))
  )

  def anySealsBroken: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AreAnySealsBrokenPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "checkYourAnswers.rowHeadings.anySealsBroken",
    id = Some("change-any-seals-broken"),
    call = Some(controllers.routes.AreAnySealsBrokenController.onPageLoad(arrivalId, CheckMode))
  )

  def canSealsBeRead: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = CanSealsBeReadPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "checkYourAnswers.rowHeadings.canSealsBeRead",
    id = Some("change-can-seals-be-read"),
    call = Some(controllers.routes.CanSealsBeReadController.onPageLoad(arrivalId, CheckMode))
  )

  def additionalComment: Option[SummaryListRow] = getAnswerAndBuildRow[String]( //TODO: Does this need to be removable?
    page = UnloadingCommentsPage,
    formatAnswer = formatAsText,
    prefix = "checkYourAnswers.rowHeadings.additionalComments",
    id = Some("change-comment"),
    call = Some(controllers.routes.UnloadingCommentsController.onPageLoad(arrivalId, CheckMode))
  )

}
