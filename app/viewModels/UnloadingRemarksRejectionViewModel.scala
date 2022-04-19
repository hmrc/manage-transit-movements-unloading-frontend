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

import models._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.Date.getDate
import utils.UnloadingRemarksRejectionHelper

import javax.inject.Inject

class UnloadingRemarksRejectionViewModel @Inject() (
  helper: UnloadingRemarksRejectionHelper
) {

  def apply(
    error: FunctionalError,
    arrivalId: ArrivalId
  )(implicit messages: Messages): Option[SummaryListRow] =
    error.originalAttributeValue.flatMap {
      originalValue =>
        error.pointer match {
          case NumberOfPackagesPointer    => Some(helper.totalNumberOfPackages(arrivalId, originalValue))
          case VehicleRegistrationPointer => Some(helper.vehicleNameRegistrationReference(arrivalId, originalValue))
          case NumberOfItemsPointer       => Some(helper.totalNumberOfItems(arrivalId, originalValue))
          case GrossMassPointer           => Some(helper.grossMassAmount(arrivalId, originalValue))
          case UnloadingDatePointer =>
            getDate(originalValue).map(
              date => helper.unloadingDate(arrivalId, date)
            )
          case _: DefaultPointer => None
        }
    }
}
