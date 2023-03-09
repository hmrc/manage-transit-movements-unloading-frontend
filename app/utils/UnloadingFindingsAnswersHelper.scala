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

package utils

import models.{NormalMode, UserAnswers}
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow

class UnloadingFindingsAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) extends AnswersHelper(userAnswers) {

  def departureMeansID: Option[SummaryListRow] = getAnswerAndBuildRowFromPath[String](
    path = JsPath \ "n1:CC043C" \ "Consignment" \ "DepartureTransportMeans" \ "identificationNumber",
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.identificationType",
    id = Some("change-departure-means-id"),
    call = Some(controllers.routes.VehicleIdentificationNumberController.onPageLoad(arrivalId, NormalMode))
  )

  def departureRegisteredCountry: Option[SummaryListRow] = getAnswerAndBuildRowFromPath[String](
    path = JsPath \ "n1:CC043C" \ "Consignment" \ "DepartureTransportMeans" \ "nationality",
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.vehicleNationality",
    id = Some("change-departure-means-country"),
    call = Some(controllers.routes.VehicleRegistrationCountryController.onPageLoad(arrivalId, NormalMode))
  )
}
