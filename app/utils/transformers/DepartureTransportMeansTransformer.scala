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

import generated.DepartureTransportMeansType02
import models.{Index, UserAnswers}
import pages._
import pages.departureMeansOfTransport.{CountryPage, TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
import services.MeansOfTransportIdentificationTypesService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureTransportMeansTransformer @Inject() (meansOfTransportIdentificationTypesService: MeansOfTransportIdentificationTypesService)(implicit
  ec: ExecutionContext
) extends PageTransformer {

  def transform(departureTransportMeans: Seq[DepartureTransportMeansType02])(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    userAnswers =>
      departureTransportMeans.zipWithIndex.foldLeft(Future.successful(userAnswers))({
        case (acc, (DepartureTransportMeansType02(_, typeOfIdentification, identificationNumber, nationality), i)) =>
          acc.flatMap {
            userAnswers =>
              val dtmIndex: Index    = Index(i)
              val fetchReferenceData = meansOfTransportIdentificationTypesService.getMeansOfTransportIdentificationType(Some(typeOfIdentification))
              val pipeline: Future[UserAnswers => Future[UserAnswers]] = fetchReferenceData.map(
                identificationType =>
                  set(TransportMeansIdentificationPage(dtmIndex), identificationType) andThen
                    set(VehicleIdentificationNumberPage(dtmIndex), identificationNumber) andThen
                    set(CountryPage(dtmIndex), nationality)
              )
              pipeline.flatMap(_(userAnswers))
          }
      })

  def transform(departureTransportMeans: Seq[DepartureTransportMeansType02], hcIndex: Index): UserAnswers => Future[UserAnswers] =
    userAnswers =>
      departureTransportMeans.zipWithIndex.foldLeft(Future.successful(userAnswers))({
        case (acc, (DepartureTransportMeansType02(_, typeOfIdentification, identificationNumber, nationality), i)) =>
          acc.flatMap {
            userAnswers =>
              val dtmIndex: Index = Index(i)
              val pipeline: UserAnswers => Future[UserAnswers] =
                set(DepartureTransportMeansIdentificationTypePage(hcIndex, dtmIndex), typeOfIdentification) andThen
                  set(DepartureTransportMeansIdentificationNumberPage(hcIndex, dtmIndex), identificationNumber) andThen
                  set(DepartureTransportMeansCountryPage(hcIndex, dtmIndex), nationality)

              pipeline(userAnswers)
          }
      })
}
