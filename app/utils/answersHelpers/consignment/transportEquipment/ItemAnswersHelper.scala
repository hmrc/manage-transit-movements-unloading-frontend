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

package utils.answersHelpers.consignment.transportEquipment

import models.reference.Item
import models.{CheckMode, Index, UserAnswers}
import pages.transportEquipment.index.ItemPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper

class ItemAnswersHelper(
  userAnswers: UserAnswers,
  equipmentIndex: Index,
  itemIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def transportEquipmentItem: Option[SummaryListRow] = getAnswerAndBuildRow[Item](
    page = ItemPage(equipmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.consignment.item",
    args = Seq(itemIndex.display, equipmentIndex.display): _*,
    id = Some(s"change-consignment-item-details-${equipmentIndex.display}-${itemIndex.display}"),
    call = Some(controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(arrivalId, equipmentIndex, itemIndex, CheckMode))
  )
}
