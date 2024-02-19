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

import models.UserAnswers
import play.api.i18n.Messages
import utils.UnloadingFindingsAnswersHelper
import viewModels.sections.Section

import javax.inject.Inject

case class UnloadingFindingsViewModel(section: Seq[Section])

object UnloadingFindingsViewModel {

  def apply(userAnswers: UserAnswers)(implicit messages: Messages): UnloadingFindingsViewModel = new UnloadingFindingsViewModelProvider()(userAnswers)

  class UnloadingFindingsViewModelProvider @Inject() () {

    def apply(userAnswers: UserAnswers)(implicit messages: Messages): UnloadingFindingsViewModel = {
      val helper = new UnloadingFindingsAnswersHelper(userAnswers)

      val sections = Seq(
        Seq(helper.preSection),
        helper.buildTransportSections,
        helper.transportEquipmentSections,
        helper.houseConsignmentSections,
        Seq(Section(sectionTitle = messages("unloadingFindings.additional.reference.heading"), rows = helper.additionalReferences))
      ).flatten

      new UnloadingFindingsViewModel(sections)
    }
  }
}
