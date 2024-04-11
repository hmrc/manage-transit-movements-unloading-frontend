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

package utils.transformers

import connectors.ReferenceDataConnector
import generated.ConsignmentType05
import models.UserAnswers
import pages.countryOfDestination.CountryOfDestinationPage
import pages.grossMass.GrossMassPage
import pages.inlandModeOfTransport.InlandModeOfTransportPage
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConsignmentTransformer @Inject() (
  consignorTransformer: ConsignorTransformer,
  consigneeTransformer: ConsigneeTransformer,
  transportEquipmentTransformer: TransportEquipmentTransformer,
  departureTransportMeansTransformer: DepartureTransportMeansTransformer,
  documentsTransformer: DocumentsTransformer,
  houseConsignmentsTransformer: HouseConsignmentsTransformer,
  additionalReferencesTransformer: AdditionalReferencesTransformer,
  additionalInformationTransformer: AdditionalInformationTransformer,
  incidentsTransformer: IncidentsTransformer,
  referenceDataConnector: ReferenceDataConnector
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(consignment: Option[ConsignmentType05])(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    consignment match {
      case Some(consignment05) =>
        lazy val pipeline: UserAnswers => Future[UserAnswers] =
          consignorTransformer.transform(consignment05.Consignor) andThen
            consigneeTransformer.transform(consignment05.Consignee) andThen
            transportEquipmentTransformer.transform(consignment05.TransportEquipment) andThen
            departureTransportMeansTransformer.transform(consignment05.DepartureTransportMeans) andThen
            documentsTransformer.transform(consignment05.SupportingDocument, consignment05.TransportDocument, consignment05.PreviousDocument) andThen
            houseConsignmentsTransformer.transform(consignment05.HouseConsignment) andThen
            additionalReferencesTransformer.transform(consignment05.AdditionalReference) andThen
            additionalInformationTransformer.transform(consignment05.AdditionalInformation) andThen
            set(GrossMassPage, consignment05.grossMass) andThen
            additionalReferencesTransformer.transform(consignment05.AdditionalReference) andThen
            incidentsTransformer.transform(consignment05.Incident) andThen
            transformCountryOfDestination(consignment05.countryOfDestination) andThen
            transformInlandModeOfTransport(consignment05.inlandModeOfTransport)
        pipeline(userAnswers)
      case None =>
        Future.successful(userAnswers)
    }

  private def transformCountryOfDestination(countryOfDestination: Option[String])(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] = userAnswers =>
    countryOfDestination match {

      case Some(country) =>
        referenceDataConnector.getCountry(country).flatMap {
          countryVal =>
            val pipeline: UserAnswers => Future[UserAnswers] =
              set(CountryOfDestinationPage, countryVal)
            pipeline(userAnswers)
        }

      case None => Future.successful(userAnswers)
    }

  private def transformInlandModeOfTransport(inlandModeOfTransport: Option[String])(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] = userAnswers =>
    inlandModeOfTransport match {
      case Some(inlandMode) =>
        referenceDataConnector.getTransportModeCode(inlandMode).flatMap {
          inlandModeVal =>
            val pipeline: UserAnswers => Future[UserAnswers] =
              set(InlandModeOfTransportPage, inlandModeVal)
            pipeline(userAnswers)
        }

      case None => Future.successful(userAnswers)
    }
}
