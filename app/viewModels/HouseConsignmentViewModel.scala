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

import models.{Index, UserAnswers}
import play.api.i18n.Messages
import utils.answersHelpers.consignment.HouseConsignmentAnswersHelper
import viewModels.sections.Section

import javax.inject.Inject

case class HouseConsignmentViewModel(sections: Seq[Section])

object HouseConsignmentViewModel {

  def apply(
    userAnswers: UserAnswers,
    houseConsignmentIndex: Index
  )(implicit messages: Messages): HouseConsignmentViewModel = new HouseConsignmentViewModelProvider()(userAnswers, houseConsignmentIndex)

  class HouseConsignmentViewModelProvider @Inject() () {

    def apply(userAnswers: UserAnswers, houseConsignmentIndex: Index)(implicit messages: Messages): HouseConsignmentViewModel = {
      val helper = new HouseConsignmentAnswersHelper(userAnswers, houseConsignmentIndex)

      val sections: Seq[Section] = Seq(
        helper.departureTransportMeansSection,
        helper.documentSection,
        helper.additionalReferencesSection,
        helper.additionalInformationSection,
        helper.itemSection,
        helper.houseConsignmentConsignorSection,
        helper.houseConsignmentConsigneeSection
      )

      HouseConsignmentViewModel(sections)
    }
  }
}
