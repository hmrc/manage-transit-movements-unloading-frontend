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

package navigation

import controllers.routes
import models.{ArrivalId, NormalMode}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

case object UnloadingGuidanceNavigator {

  def unloadingGuidanceNavigate(arrivalId: ArrivalId, newAuth: Boolean, goodsTooLarge: Option[Boolean]): Result =
    if (newAuth) {
      goodsTooLarge match {
        case Some(goodsTooLarge) =>
          if (goodsTooLarge) {
            Redirect(routes.LargeUnsealedGoodsRecordDiscrepanciesYesNoController.onPageLoad(arrivalId, NormalMode))
          } else {
            Redirect(routes.SealsReplacedByCustomsAuthorityYesNoController.onPageLoad(arrivalId, NormalMode))
          }
        case _ =>
          Redirect(routes.GoodsTooLargeForContainerYesNoController.onPageLoad(arrivalId, NormalMode))
      }
    } else {
      Redirect(routes.UnloadingTypeController.onPageLoad(arrivalId, NormalMode))
    }

}
