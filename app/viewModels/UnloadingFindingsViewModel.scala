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

import config.FrontendAppConfig
import models.UserAnswers
import play.api.i18n.Messages
import utils.answersHelpers.ConsignmentAnswersHelper
import viewModels.sections.Section

import javax.inject.Inject

case class UnloadingFindingsViewModel(sections: Seq[Section])

object UnloadingFindingsViewModel {

  class UnloadingFindingsViewModelProvider @Inject() (config: FrontendAppConfig) {

    def apply(userAnswers: UserAnswers)(implicit messages: Messages): UnloadingFindingsViewModel = {
      val helper = new ConsignmentAnswersHelper(userAnswers)
      val sections = Seq(
        Some(helper.headerSection),
        helper.consignorSection,
        helper.consigneeSection,
        helper.holderOfTheTransitProcedureSection,
        Some(helper.inlandModeOfTransportSection),
        Option.when(config.phase6Enabled)(helper.countriesOfRoutingSection),
        Some(helper.departureTransportMeansSection),
        Some(helper.transportEquipmentSection),
        Some(helper.documentSection),
        Some(helper.additionalReferencesSection),
        Some(helper.incidentSection),
        Some(helper.additionalInformationSection),
        Some(helper.houseConsignmentSection)
      ).flatten

      new UnloadingFindingsViewModel(sections)
    }
  }
}
