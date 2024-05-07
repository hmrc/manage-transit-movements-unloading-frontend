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

import config.PhaseConfig
import models.{Index, Phase, UserAnswers}
import play.api.i18n.Messages
import utils.answersHelpers.consignment.HouseConsignmentAnswersHelper
import viewModels.sections.Section
import viewModels.sections.Section.StaticSection

import javax.inject.Inject

case class HouseConsignmentViewModel(section: Section)

object HouseConsignmentViewModel {

  class HouseConsignmentViewModelProvider @Inject() (implicit phaseConfig: PhaseConfig) {

    def apply(userAnswers: UserAnswers, houseConsignmentIndex: Index)(implicit messages: Messages): HouseConsignmentViewModel = {
      val helper = new HouseConsignmentAnswersHelper(userAnswers, houseConsignmentIndex)

      val rows = Seq(
        helper.grossMassRow,
        helper.countryOfDestination,
        helper.safetyAndSecurityDetails
      ).flatten

      val children: Seq[Section] = phaseConfig.phase match {
        case Phase.Transition =>
          Seq(
            helper.itemSection
          )
        case Phase.PostTransition =>
          Seq(
            helper.houseConsignmentConsignorSection,
            helper.houseConsignmentConsigneeSection,
            helper.departureTransportMeansSection,
            helper.documentSection,
            helper.additionalReferencesSection,
            helper.additionalInformationSection,
            helper.itemSection
          )
      }

      val houseConsignmentSection: Section =
        StaticSection(rows = rows, children = children)

      HouseConsignmentViewModel(houseConsignmentSection)
    }
  }
}
