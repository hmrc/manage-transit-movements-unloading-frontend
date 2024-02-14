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

package utils

import models.UserAnswers
import pages.grossMass.GrossMassPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class ConsignmentAnswersHelper(userAnswers: UserAnswers)(implicit
  messages: Messages
) extends UnloadingAnswersHelper(userAnswers) {

  def grossMass: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = GrossMassPage,
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.gross.mass.heading",
    id = Some(s"change-gross-mass"),
    call = None // TODO change this when implementing change link
  )
}
