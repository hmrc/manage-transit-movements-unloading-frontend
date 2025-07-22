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

import generated.{CUSTOM_DepartureTransportMeansType01, DepartureTransportMeansType01}
import models.{Index, UserAnswers}
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureTransportMeansTransformer @Inject() (
  referenceDataService: ReferenceDataService
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(
    departureTransportMeans: Seq[CUSTOM_DepartureTransportMeansType01]
  )(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.departureMeansOfTransport.*
    import pages.sections.TransportMeansSection

    departureTransportMeans.mapWithSets {
      (value, index) =>
        setSequenceNumber(TransportMeansSection(index), value.sequenceNumber) andThen
          set(TransportMeansIdentificationPage(index), value.typeOfIdentification, referenceDataService.getMeansOfTransportIdentificationType) andThen
          set(VehicleIdentificationNumberPage(index), value.identificationNumber) andThen
          set(CountryPage(index), value.nationality, referenceDataService.getCountry)
    }
  }

  def transform(
    departureTransportMeans: Seq[DepartureTransportMeansType01],
    hcIndex: Index
  )(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.houseConsignment.index.departureMeansOfTransport.*
    import pages.sections.houseConsignment.index.departureTransportMeans.TransportMeansSection

    departureTransportMeans.mapWithSets {
      (value, index) =>
        setSequenceNumber(TransportMeansSection(hcIndex, index), value.sequenceNumber) andThen
          set(
            TransportMeansIdentificationPage(hcIndex, index),
            value.typeOfIdentification,
            referenceDataService.getMeansOfTransportIdentificationType
          ) andThen
          set(VehicleIdentificationNumberPage(hcIndex, index), value.identificationNumber) andThen
          set(CountryPage(hcIndex, index), value.nationality, referenceDataService.getCountry)
    }
  }
}
