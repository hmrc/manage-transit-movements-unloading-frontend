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

import models.{Index, UserAnswers}
import pages.ContainerIdentificationNumberPage
import pages.sections.{SealsSection, TransportEquipmentListSection}
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import viewModels.sections.Section
import viewModels.sections.Section.AccordionSection

class TransportEquipmentAnswersHelper(
  userAnswers: UserAnswers,
  equipmentIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def transportEquipmentSections: Seq[Section] =
    userAnswers
      .get(TransportEquipmentListSection)
      .mapWithIndex {
        (_, equipmentIndex) =>
          val rows = Seq(
            Seq(containerIdentificationNumber).flatten,
            transportEquipmentSeals
          ).flatten

          Some(
            AccordionSection(
              sectionTitle = messages("unloadingFindings.subsections.transportEquipment", equipmentIndex.display),
              rows = rows
            )
          )
      }

  def containerIdentificationNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ContainerIdentificationNumberPage(equipmentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.containerIdentificationNumber",
    id = Some(s"change-container-identification-number-${equipmentIndex.display}"),
    args = None,
    call = Some(Call(GET, "#"))
  )

  def transportEquipmentSeals: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(SealsSection(equipmentIndex)) {
      sealIndex =>
        val helper = new SealAnswersHelper(userAnswers, equipmentIndex, sealIndex)
        helper.transportEquipmentSeal
    }
}
