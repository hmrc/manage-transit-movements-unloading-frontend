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
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier
import utils.{HouseConsignmentAnswersHelper, UnloadingFindingsAnswersHelper}
import viewModels.sections.Section

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class HouseConsignmentViewModel(section: Seq[Section], houseConsignment: Seq[Section])

object HouseConsignmentViewModel {

  def apply(userAnswers: UserAnswers, houseConsignmentIndex: Index, referenceDataService: ReferenceDataService)(implicit
    messages: Messages,
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HouseConsignmentViewModel] = new HouseConsignmentViewModelProvider(referenceDataService)(userAnswers, houseConsignmentIndex)

  class HouseConsignmentViewModelProvider @Inject() (referenceDataService: ReferenceDataService) {

    def apply(userAnswers: UserAnswers,
              houseConsignmentIndex: Index
    )(implicit messages: Messages, hc: HeaderCarrier, ex: ExecutionContext): Future[HouseConsignmentViewModel] = {
      val helper = new HouseConsignmentAnswersHelper(userAnswers, houseConsignmentIndex: Index, referenceDataService)

      helper.buildTransportSections.map {
        meansOfTransportSections =>
          val houseConsignmentSection: Seq[Section] = Seq(helper.houseConsignmentSection)
          val itemSections: Seq[Section]            = helper.itemSections

          val sections: Seq[Section] = meansOfTransportSections ++ itemSections

          HouseConsignmentViewModel(sections, houseConsignmentSection)

      }
    }
  }
}
