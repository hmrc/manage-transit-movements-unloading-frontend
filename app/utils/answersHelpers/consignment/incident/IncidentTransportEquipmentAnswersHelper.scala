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

package utils.answersHelpers.consignment.incident

import generated.TransportEquipmentType07
import models.{Index, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper

class IncidentTransportEquipmentAnswersHelper(
  userAnswers: UserAnswers,
  transportEquipmentType0: TransportEquipmentType07
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def containerIdentificationNumber: Option[SummaryListRow] = buildRowWithNoChangeLink[String](
    data = transportEquipmentType0.containerIdentificationNumber,
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.containerIdentificationNumber"
  )

  def transportEquipmentSeals: Seq[SummaryListRow] =
    transportEquipmentType0.Seal.zipWithIndex.flatMap {
      case (sealType0, i) =>
        val sealIndex = Index(i)
        buildRowWithNoChangeLink[String](
          data = Option(sealType0.identifier),
          formatAnswer = formatAsText,
          prefix = "unloadingFindings.rowHeadings.sealIdentifier",
          args = sealIndex.display
        )

    }

  def itemNumber: Seq[SummaryListRow] =
    transportEquipmentType0.GoodsReference.zipWithIndex.flatMap {
      case (type0, i) =>
        val itemIndex = Index(i)
        buildRowWithNoChangeLink[String](
          data = Option(type0.declarationGoodsItemNumber.toString()),
          formatAnswer = formatAsText,
          prefix = "unloadingFindings.rowHeadings.item",
          args = itemIndex.display
        )

    }

}
