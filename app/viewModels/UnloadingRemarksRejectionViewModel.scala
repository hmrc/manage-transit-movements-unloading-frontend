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

package viewModels

import models._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.UnloadingRemarksRejectionHelper

class UnloadingRemarksRejectionViewModel {

  def apply(
    userAnswers: UserAnswers
  )(implicit messages: Messages): Option[SummaryListRow] = {
    lazy val helper = new UnloadingRemarksRejectionHelper(userAnswers)

    Seq(
      helper.totalNumberOfPackages,
      helper.vehicleNameRegistrationReference,
      helper.totalNumberOfItems,
      helper.grossMassAmount,
      helper.unloadingDate
    ).flatten.headOption
  }
}
