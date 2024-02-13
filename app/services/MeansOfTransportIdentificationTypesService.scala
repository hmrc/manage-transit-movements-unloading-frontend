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
import config.Constants.{Fixed, Unknown}
import connectors.ReferenceDataConnector
import models.Index
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.transport.TransportMode.InlandMode
import models.requests.DataRequest
import pages.departureMeansOfTransport.TransportMeansIdentificationPage
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MeansOfTransportIdentificationTypesService @Inject() (
  referenceDataConnector: ReferenceDataConnector,
  transportModeCodesService: TransportModeCodesService
)(implicit ec: ExecutionContext) {

  def getMeansOfTransportIdentificationTypes(inlandMode: Option[InlandMode], transportMeansIndex: Index)(implicit
    hc: HeaderCarrier,
    request: DataRequest[AnyContent]
  ): Future[Seq[TransportMeansIdentification]] =
    inlandMode match {
      case Some(InlandMode(_, _)) =>
        referenceDataConnector
          .getMeansOfTransportIdentificationTypes()
          .map(_.toSeq)
          .flatMap(filter(_, Future.successful(inlandMode)))
      case None =>
        referenceDataConnector
          .getMeansOfTransportIdentificationTypes()
          .map(_.toSeq)
          .flatMap(
            filter(
              _,
              transportModeCodesService.getInlandModes().map {
                inlandModes =>
                  request.userAnswers
                    .get(TransportMeansIdentificationPage(transportMeansIndex))
                    .flatMap {
                      inlandMode =>
                        inlandModes.find(_.code == inlandMode.code)
                    }
              }
            )
          )
    }

  def getMeansOfTransportIdentificationType(userAnswersCode: Option[String])(implicit hc: HeaderCarrier): Future[Option[TransportMeansIdentification]] =
    userAnswersCode match {
      case Some(value) =>
        referenceDataConnector
          .getMeansOfTransportIdentificationType(value)
          .map(
            item => Some(item.head)
          )
      case _ => Future.successful(None)
    }

  private def filter(
    identificationTypes: Seq[TransportMeansIdentification],
    inlandMode: Future[Option[InlandMode]]
  ): Future[Seq[TransportMeansIdentification]] =
    inlandMode.map {
      case Some(InlandMode(code, _)) if code != Fixed && code != Unknown => identificationTypes.filter(_.code.startsWith(code))
      case _                                                             => identificationTypes.filterNot(_.code == UnknownIdentification)
    }
}
