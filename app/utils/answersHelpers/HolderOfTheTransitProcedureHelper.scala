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

package utils.answersHelpers

import generated.HolderOfTheTransitProcedureType06
import models._
import models.reference.Country
import pages.holderOfTheTransitProcedure.CountryPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.sections.Section
import viewModels.sections.Section.AccordionSection

class HolderOfTheTransitProcedureHelper(
  userAnswers: UserAnswers
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def identificationNumber(holderOfTheTransitProcedure: HolderOfTheTransitProcedureType06): Option[SummaryListRow] = buildRowWithNoChangeLink[String](
    data = holderOfTheTransitProcedure.identificationNumber,
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.holderOfTheTransitProcedure.identificationNumber"
  )

  def tirHolderIdentificationNumber(holderOfTheTransitProcedure: HolderOfTheTransitProcedureType06): Option[SummaryListRow] = buildRowWithNoChangeLink[String](
    data = holderOfTheTransitProcedure.TIRHolderIdentificationNumber,
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.holderOfTheTransitProcedure.tirHolderIdentificationNumber"
  )

  def name(holderOfTheTransitProcedure: HolderOfTheTransitProcedureType06): Option[SummaryListRow] = buildRowWithNoChangeLink[String](
    data = Option(holderOfTheTransitProcedure.name),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.holderOfTheTransitProcedure.name"
  )

  def address(holderOfTheTransitProcedure: HolderOfTheTransitProcedureType06): Option[SummaryListRow] = buildRowWithNoChangeLink[DynamicAddress](
    data = Option(holderOfTheTransitProcedure.Address.toDynamicAddress),
    formatAnswer = formatAsDynamicAddress,
    prefix = "unloadingFindings.rowHeadings.holderOfTheTransitProcedure.address"
  )

  def country: Option[SummaryListRow] = buildRowWithNoChangeLink[Country](
    data = userAnswers.get(CountryPage),
    formatAnswer = formatAsCountry,
    prefix = "unloadingFindings.rowHeadings.holderOfTheTransitProcedure.country"
  )

  def holderOfTheTransitProcedureSection: Seq[Section] = userAnswers.ie043Data.HolderOfTheTransitProcedure
    .map(
      hotP =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.rowHeadings.holderOfTheTransitProcedure.heading")),
          rows = Seq(
            identificationNumber(hotP),
            name(hotP),
            country,
            address(hotP),
            tirHolderIdentificationNumber(hotP)
          ).flatten
        )
    )
    .toList

}
