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

package utils.answersHelpers.consignment

import generated.AddressType14
import models.reference.Country
import models.{DynamicAddress, UserAnswers}
import pages.consignee.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper

class ConsigneeAnswersHelper(
  userAnswers: UserAnswers
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def identificationNumber(answer: Option[String]): Option[SummaryListRow] =
    buildRowWithNoChangeLink[String](
      data = answer,
      formatAnswer = formatAsText,
      prefix = "unloadingFindings.consignee.identificationNumber"
    )

  def name(answer: Option[String]): Option[SummaryListRow] =
    buildRowWithNoChangeLink[String](
      data = answer,
      formatAnswer = formatAsText,
      prefix = "unloadingFindings.consignee.name"
    )

  def country: Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = CountryPage,
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.consignee.country",
    id = None,
    call = None
  )

  def address(answer: Option[AddressType14]): Option[SummaryListRow] =
    buildRowWithNoChangeLink[DynamicAddress](
      data = answer.map(DynamicAddress(_)),
      formatAnswer = formatAsHtmlContent,
      prefix = "unloadingFindings.consignee.address"
    )
}
