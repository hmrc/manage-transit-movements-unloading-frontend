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

import generated.{CUSTOM_DepartureTransportMeansType02, DepartureTransportMeansType02}
import models.reference.{Country, TransportMeansIdentification}
import models.{Index, UserAnswers}
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureTransportMeansTransformer @Inject() (
  referenceDataService: ReferenceDataService
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  private case class GenericDepartureTransportMeans(
    sequenceNumber: BigInt,
    typeOfIdentification: Option[String],
    identificationNumber: Option[String],
    nationality: Option[String]
  )

  private object GenericDepartureTransportMeans {

    def apply(dtm: DepartureTransportMeansType02): GenericDepartureTransportMeans =
      new GenericDepartureTransportMeans(
        sequenceNumber = dtm.sequenceNumber,
        typeOfIdentification = Some(dtm.typeOfIdentification),
        identificationNumber = Some(dtm.identificationNumber),
        nationality = Some(dtm.nationality)
      )

    def apply(dtm: CUSTOM_DepartureTransportMeansType02): GenericDepartureTransportMeans =
      new GenericDepartureTransportMeans(
        sequenceNumber = dtm.sequenceNumber,
        typeOfIdentification = dtm.typeOfIdentification,
        identificationNumber = dtm.identificationNumber,
        nationality = dtm.nationality
      )
  }

  private case class TempDepartureTransportMeans[T](
    underlying: T,
    typeOfIdentification: Option[TransportMeansIdentification],
    nationality: Option[Country]
  )

  def transform(
    departureTransportMeans: Seq[CUSTOM_DepartureTransportMeansType02]
  )(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.departureMeansOfTransport.*
    import pages.sections.TransportMeansSection

    genericTransform(departureTransportMeans.map(GenericDepartureTransportMeans(_))) {
      case (TempDepartureTransportMeans(underlying, typeOfIdentification, nationality), index) =>
        setSequenceNumber(TransportMeansSection(index), underlying.sequenceNumber) andThen
          set(TransportMeansIdentificationPage(index), typeOfIdentification) andThen
          set(VehicleIdentificationNumberPage(index), underlying.identificationNumber) andThen
          set(CountryPage(index), nationality)
    }
  }

  def transform(
    departureTransportMeans: Seq[DepartureTransportMeansType02],
    hcIndex: Index
  )(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.houseConsignment.index.departureMeansOfTransport.*
    import pages.sections.houseConsignment.index.departureTransportMeans.TransportMeansSection

    genericTransform(departureTransportMeans.map(GenericDepartureTransportMeans(_))) {
      case (TempDepartureTransportMeans(underlying, typeOfIdentification, nationality), index) =>
        setSequenceNumber(TransportMeansSection(hcIndex, index), underlying.sequenceNumber) andThen
          set(TransportMeansIdentificationPage(hcIndex, index), typeOfIdentification) andThen
          set(VehicleIdentificationNumberPage(hcIndex, index), underlying.identificationNumber) andThen
          set(CountryPage(hcIndex, index), nationality)
    }
  }

  private def genericTransform(
    departureTransportMeans: Seq[GenericDepartureTransportMeans]
  )(
    pipeline: (TempDepartureTransportMeans[GenericDepartureTransportMeans], Index) => UserAnswers => Future[UserAnswers]
  )(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers => {
    lazy val referenceDataLookups = departureTransportMeans.map {
      dtm =>
        // Defining futures here as for-comprehension creates a dependency between Futures, making the code synchronous
        val typeOfIdentificationF = dtm.typeOfIdentification.lookup(referenceDataService.getMeansOfTransportIdentificationType)
        val nationalityF          = dtm.nationality.lookup(referenceDataService.getCountry)

        for {
          typeOfIdentification <- typeOfIdentificationF
          nationality          <- nationalityF
        } yield TempDepartureTransportMeans(
          underlying = dtm,
          typeOfIdentification = typeOfIdentification,
          nationality = nationality
        )
    }

    Future.sequence(referenceDataLookups).flatMap {
      _.zipWithIndex.foldLeft(Future.successful(userAnswers)) {
        case (acc, (dtm, i)) =>
          acc.flatMap(pipeline(dtm, Index(i)))
      }
    }
  }
}
