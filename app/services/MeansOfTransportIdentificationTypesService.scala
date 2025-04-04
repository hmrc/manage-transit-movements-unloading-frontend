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

package services

import config.Constants.MeansOfTransportIdentification.UnknownIdentification
import config.Constants.TransportModeCode.*
import models.UserAnswers
import models.reference.TransportMeansIdentification
import models.reference.TransportMode.InlandMode
import pages.inlandModeOfTransport.InlandModeOfTransportPage
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MeansOfTransportIdentificationTypesService @Inject() (
  referenceDataService: ReferenceDataService
)(implicit ec: ExecutionContext) {

  def getMeansOfTransportIdentificationTypes(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier): Future[Seq[TransportMeansIdentification]] =
    referenceDataService
      .getMeansOfTransportIdentificationTypes()
      .map(filter(_, userAnswers.get(InlandModeOfTransportPage)))

  private def filter(
    identificationTypes: Seq[TransportMeansIdentification],
    inlandMode: Option[InlandMode]
  ): Seq[TransportMeansIdentification] =
    inlandMode.map(_.code) match {
      case Some(code) if code != Fixed && code != Unknown => identificationTypes.filter(_.code.startsWith(code))
      case _                                              => identificationTypes.filterNot(_.code == UnknownIdentification)
    }
}
