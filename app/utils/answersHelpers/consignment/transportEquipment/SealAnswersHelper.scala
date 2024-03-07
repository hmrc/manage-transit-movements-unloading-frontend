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

import models.{CheckMode, Index, UserAnswers}
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.answersHelpers.AnswersHelper

class SealAnswersHelper(
  userAnswers: UserAnswers,
  equipmentIndex: Index,
  sealIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def transportEquipmentSeal: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = SealIdentificationNumberPage(equipmentIndex, sealIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.sealIdentifier",
    args = Seq(sealIndex.display, equipmentIndex.display): _*,
    id = Some(s"change-seal-details-${equipmentIndex.display}-${sealIndex.display}"),
    call =
      Some(controllers.transportEquipment.index.seals.routes.SealIdentificationNumberController.onPageLoad(arrivalId, CheckMode, equipmentIndex, sealIndex))
  )
}
