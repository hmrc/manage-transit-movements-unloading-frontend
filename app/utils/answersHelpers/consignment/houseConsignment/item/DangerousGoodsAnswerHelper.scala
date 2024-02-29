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

package utils.answersHelpers.consignment.houseConsignment.item

import models.{Index, UserAnswers}
import pages.houseConsignment.index.items.DangerousGoodsPage
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.answersHelpers.AnswersHelper

class DangerousGoodsAnswerHelper(
  userAnswers: UserAnswers,
  houseConsignmentIndex: Index,
  itemIndex: Index,
  dangerousGoodsIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def dangerousGoodsRow: Option[SummaryListRow] =
    getAnswerAndBuildRow[String](
      page = DangerousGoodsPage(houseConsignmentIndex, itemIndex, dangerousGoodsIndex),
      formatAnswer = formatAsText,
      prefix = "unloadingFindings.dangerousGoods.unNumber",
      args = dangerousGoodsIndex.display,
      id = Some(s"change-unNumber-${dangerousGoodsIndex.display}"),
      call = Some(Call(GET, "#")) //TODO change me please
    )
}
