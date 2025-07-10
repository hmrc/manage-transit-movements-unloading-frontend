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

import generated.ConsignmentType05
import models.UserAnswers
import pages.countryOfDestination.CountryOfDestinationPage
import pages.inlandModeOfTransport.InlandModeOfTransportPage
import pages.{GrossWeightPage, UniqueConsignmentReferencePage}
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConsignmentTransformer @Inject() (
  consignorTransformer: ConsignorTransformer,
  consigneeTransformer: ConsigneeTransformer,
  transportEquipmentTransformer: TransportEquipmentTransformer,
  departureTransportMeansTransformer: DepartureTransportMeansTransformer,
  countriesOfRoutingTransformer: CountriesOfRoutingTransformer,
  documentsTransformer: DocumentsTransformer,
  houseConsignmentsTransformer: HouseConsignmentsTransformer,
  additionalReferencesTransformer: AdditionalReferencesTransformer,
  additionalInformationTransformer: AdditionalInformationTransformer,
  incidentsTransformer: IncidentsTransformer,
  referenceDataService: ReferenceDataService
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(consignment: Option[ConsignmentType05])(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    consignment match {
      case Some(value) =>
        consignorTransformer.transform(value.Consignor) andThen
          consigneeTransformer.transform(value.Consignee) andThen
          transportEquipmentTransformer.transform(value.TransportEquipment) andThen
          departureTransportMeansTransformer.transform(value.DepartureTransportMeans) andThen
          countriesOfRoutingTransformer.transform(value.CountryOfRoutingOfConsignment) andThen
          documentsTransformer.transform(value.SupportingDocument, value.TransportDocument, value.PreviousDocument) andThen
          houseConsignmentsTransformer.transform(value.HouseConsignment) andThen
          additionalInformationTransformer.transform(value.AdditionalInformation) andThen
          set(GrossWeightPage, value.grossMass) andThen
          set(UniqueConsignmentReferencePage, value.referenceNumberUCR) andThen
          additionalReferencesTransformer.transform(value.AdditionalReference) andThen
          incidentsTransformer.transform(value.Incident) andThen
          set(CountryOfDestinationPage, value.countryOfDestination, referenceDataService.getCountry) andThen
          set(InlandModeOfTransportPage, value.inlandModeOfTransport, referenceDataService.getTransportModeCode)
      case None =>
        Future.successful
    }
}
