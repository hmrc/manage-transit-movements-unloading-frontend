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

import connectors.ReferenceDataConnector
import models.UserAnswers
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier
import utils.UnloadingFindingsAnswersHelper
import viewModels.sections.Section

import javax.inject.Inject
import scala.concurrent.ExecutionContext

case class UnloadingFindingsViewModel(section: Seq[Section])

object UnloadingFindingsViewModel {

  def apply(userAnswers: UserAnswers, referenceDataService: ReferenceDataService)(implicit
    messages: Messages,
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): UnloadingFindingsViewModel =
    new UnloadingFindingsViewModelProvider(referenceDataService)(userAnswers)

  class UnloadingFindingsViewModelProvider @Inject() (referenceDataService: ReferenceDataService) {

    def apply(userAnswers: UserAnswers)(implicit messages: Messages): UnloadingFindingsViewModel = {
      val helper = new UnloadingFindingsAnswersHelper(userAnswers)

      val transportMeansSections = helper.transportMeansSections(referenceDataService)

      val transportEquipmentSections = helper.transportEquipmentSections

      val houseConsignmentSections = helper.houseConsignmentSections

      val sections: Seq[Section] = transportMeansSections ++ transportEquipmentSections ++ houseConsignmentSections

      new UnloadingFindingsViewModel(sections)
    }
  }
}
