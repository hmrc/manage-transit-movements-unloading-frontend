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
import generated.{DepartureTransportMeansType02, DepartureTransportMeansType07}
import models.reference.{Country, TransportMeansIdentification}
import models.{Index, UserAnswers}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureTransportMeansTransformer @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit
  ec: ExecutionContext
) extends PageTransformer {

  private case class TempDepartureTransportMeans[T](
    underlying: T,
    typeOfIdentification: Option[TransportMeansIdentification],
    nationality: Option[Country]
  )

  def transform(
    departureTransportMeans: Seq[DepartureTransportMeansType07]
  )(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers => {
    import pages.departureMeansOfTransport._
    import pages.sections.TransportMeansSection

    lazy val referenceDataLookups = departureTransportMeans.map {
      dtm =>
        // Defining futures here as for-comprehension creates a dependency between Futures, making the code synchronous
        val typeOfIdentificationF = dtm.typeOfIdentification.lookup(referenceDataConnector.getMeansOfTransportIdentificationType)
        val nationalityF          = dtm.nationality.lookup(referenceDataConnector.getCountry)

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
      _.zipWithIndex.foldLeft(Future.successful(userAnswers))({
        case (acc, (dtm, i)) =>
          acc.flatMap {
            userAnswers =>
              val index = Index(i)
              val pipeline: UserAnswers => Future[UserAnswers] =
                setSequenceNumber(TransportMeansSection(index), dtm.underlying.sequenceNumber) andThen
                  set(TransportMeansIdentificationPage(index), dtm.typeOfIdentification) andThen
                  set(VehicleIdentificationNumberPage(index), dtm.underlying.identificationNumber) andThen
                  set(CountryPage(index), dtm.nationality)

              pipeline(userAnswers)
          }
      })
    }
  }

  def transform(
    departureTransportMeans: Seq[DepartureTransportMeansType02],
    hcIndex: Index
  )(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers => {
    import pages.houseConsignment.index.departureMeansOfTransport._
    import pages.sections.houseConsignment.index.departureTransportMeans.TransportMeansSection

    lazy val referenceDataLookups = departureTransportMeans.map {
      dtm =>
        // Defining futures here as for-comprehension creates a dependency between Futures, making the code synchronous
        val typeOfIdentificationF = referenceDataConnector.getMeansOfTransportIdentificationType(dtm.typeOfIdentification)
        val nationalityF          = referenceDataConnector.getCountry(dtm.nationality)

        for {
          typeOfIdentification <- typeOfIdentificationF
          nationality          <- nationalityF
        } yield TempDepartureTransportMeans(
          underlying = dtm,
          typeOfIdentification = Some(typeOfIdentification),
          nationality = Some(nationality)
        )
    }

    Future.sequence(referenceDataLookups).flatMap {
      _.zipWithIndex.foldLeft(Future.successful(userAnswers))({
        case (acc, (dtm, i)) =>
          acc.flatMap {
            userAnswers =>
              val index = Index(i)
              val pipeline: UserAnswers => Future[UserAnswers] =
                setSequenceNumber(TransportMeansSection(hcIndex, index), dtm.underlying.sequenceNumber) andThen
                  set(TransportMeansIdentificationPage(hcIndex, index), dtm.typeOfIdentification) andThen
                  set(VehicleIdentificationNumberPage(hcIndex, index), dtm.underlying.identificationNumber) andThen
                  set(CountryPage(hcIndex, index), dtm.nationality)

              pipeline(userAnswers)
          }
      })
    }
  }
}
