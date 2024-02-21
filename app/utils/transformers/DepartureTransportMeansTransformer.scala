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
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.Country
import models.{Index, UserAnswers}
import pages._
import pages.departureMeansOfTransport.{CountryPage, TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureTransportMeansTransformer @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit
  ec: ExecutionContext
) extends PageTransformer {

  private case class TempDepartureTransportMeans(
    typeOfIdentification: TransportMeansIdentification,
    identificationNumber: String,
    nationality: Country
  )

  def transform(departureTransportMeans: Seq[DepartureTransportMeansType02])(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    userAnswers => {
      lazy val referenceDataLookups = departureTransportMeans.map {
        dtm =>
          // Defining futures here as for-comprehension creates a dependency between Futures, making the code synchronous
          val typeOfIdentificationF = referenceDataConnector.getMeansOfTransportIdentificationType(dtm.typeOfIdentification)
          val nationalityF          = referenceDataConnector.getCountry(dtm.nationality)

          for {
            typeOfIdentification <- typeOfIdentificationF
            nationality          <- nationalityF
          } yield TempDepartureTransportMeans(
            typeOfIdentification = typeOfIdentification,
            identificationNumber = dtm.identificationNumber,
            nationality = nationality
          )
      }

      Future.sequence(referenceDataLookups).flatMap {
        _.zipWithIndex.foldLeft(Future.successful(userAnswers))({
          case (acc, (TempDepartureTransportMeans(typeOfIdentification, identificationNumber, nationality), i)) =>
            acc.flatMap {
              userAnswers =>
                val dtmIndex = Index(i)
                val pipeline = set(TransportMeansIdentificationPage(dtmIndex), typeOfIdentification) andThen
                  set(VehicleIdentificationNumberPage(dtmIndex), identificationNumber) andThen
                  set(CountryPage(dtmIndex), nationality)

                pipeline(userAnswers)
            }
        })
      }
    }

  def transform(departureTransportMeans: Seq[DepartureTransportMeansType02], hcIndex: Index)(implicit
    headerCarrier: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] =
    userAnswers => {
      lazy val referenceDataLookups = departureTransportMeans.map {
        dtm =>
          // Defining futures here as for-comprehension creates a dependency between Futures, making the code synchronous
          val typeOfIdentificationF = referenceDataConnector.getMeansOfTransportIdentificationType(dtm.typeOfIdentification)
          val nationalityF          = referenceDataConnector.getCountry(dtm.nationality)

          for {
            typeOfIdentification <- typeOfIdentificationF
            nationality          <- nationalityF
          } yield TempDepartureTransportMeans(
            typeOfIdentification = typeOfIdentification,
            identificationNumber = dtm.identificationNumber,
            nationality = nationality
          )
      }

      Future.sequence(referenceDataLookups).flatMap {
        _.zipWithIndex.foldLeft(Future.successful(userAnswers))({
          case (acc, (TempDepartureTransportMeans(typeOfIdentification, identificationNumber, nationality), i)) =>
            acc.flatMap {
              userAnswers =>
                val dtmIndex = Index(i)
                val pipeline = set(DepartureTransportMeansIdentificationTypePage(hcIndex, dtmIndex), typeOfIdentification) andThen
                  set(DepartureTransportMeansIdentificationNumberPage(hcIndex, dtmIndex), identificationNumber) andThen
                  set(DepartureTransportMeansCountryPage(hcIndex, dtmIndex), nationality)

                pipeline(userAnswers)
            }
        })
      }
    }
}
