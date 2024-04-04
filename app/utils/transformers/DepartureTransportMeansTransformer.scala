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
import generated.DepartureTransportMeansType02
import models.reference.{Country, TransportMeansIdentification}
import models.{Index, UserAnswers}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureTransportMeansTransformer @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit
  ec: ExecutionContext
) extends PageTransformer {

  private case class TempDepartureTransportMeans(
    underlying: DepartureTransportMeansType02,
    typeOfIdentification: TransportMeansIdentification,
    nationality: Country
  )

  def transform(
    departureTransportMeans: Seq[DepartureTransportMeansType02]
  )(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.departureMeansOfTransport._
    import pages.sections.TransportMeansSection

    genericTransform(departureTransportMeans) {
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
    import pages._
    import pages.sections.houseConsignment.index.departureTransportMeans.TransportMeansSection

    genericTransform(departureTransportMeans) {
      case (TempDepartureTransportMeans(underlying, typeOfIdentification, nationality), index) =>
        setSequenceNumber(TransportMeansSection(hcIndex, index), underlying.sequenceNumber) andThen
          set(DepartureTransportMeansIdentificationTypePage(hcIndex, index), typeOfIdentification) andThen
          set(DepartureTransportMeansIdentificationNumberPage(hcIndex, index), underlying.identificationNumber) andThen
          set(DepartureTransportMeansCountryPage(hcIndex, index), nationality)
    }
  }

  private def genericTransform(
    departureTransportMeans: Seq[DepartureTransportMeansType02]
  )(
    pipeline: (TempDepartureTransportMeans, Index) => UserAnswers => Future[UserAnswers]
  )(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers => {
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
          typeOfIdentification = typeOfIdentification,
          nationality = nationality
        )
    }

    Future.sequence(referenceDataLookups).flatMap {
      _.zipWithIndex.foldLeft(Future.successful(userAnswers))({
        case (acc, (dtm, i)) =>
          acc.flatMap(pipeline(dtm, Index(i)))
      })
    }
  }
}
