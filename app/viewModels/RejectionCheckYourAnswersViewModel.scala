/*
 * Copyright 2022 HM Revenue & Customs
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
import utils.RejectionCheckYourAnswersHelper
import viewModels.sections.Section

case class RejectionCheckYourAnswersViewModel(sections: Seq[Section])

object RejectionCheckYourAnswersViewModel {

  def apply(userAnswers: UserAnswers): RejectionCheckYourAnswersViewModel = {
    val cyaHelper = new RejectionCheckYourAnswersHelper(userAnswers)
    RejectionCheckYourAnswersViewModel(
      Seq(
        Section(
          Seq(
            cyaHelper.vehicleNameRegistrationRejection,
            cyaHelper.dateGoodsUnloaded,
            cyaHelper.totalNumberOfItems,
            cyaHelper.totalNumberOfPackages,
            cyaHelper.grossMassAmount
          ).flatten
        )
      )
    )
  }

}
