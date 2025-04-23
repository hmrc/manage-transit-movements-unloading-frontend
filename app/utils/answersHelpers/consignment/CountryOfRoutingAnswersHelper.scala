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

import controllers.countriesOfRouting.routes
import models.reference.Country
import models.{CheckMode, Index, UserAnswers}
import pages.countriesOfRouting.CountryOfRoutingPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper

class CountryOfRoutingAnswersHelper(
  userAnswers: UserAnswers,
  countryIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def country: Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = CountryOfRoutingPage(countryIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.countryOfRouting",
    id = Some(s"change-country-$countryIndex"),
    call = Some(routes.CountryController.onPageLoad(userAnswers.id, countryIndex, CheckMode)),
    args = countryIndex.display
  )
}
