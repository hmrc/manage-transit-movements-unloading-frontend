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

import generated.HolderOfTheTransitProcedureType06
import models.UserAnswers
import pages.holderOfTheTransitProcedure.CountryPage
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

// HolderOfTheTransitProcedure is read only data. Fetch country only to be able to display with description on cross-check page
class HolderOfTheTransitProcedureTransformer @Inject() (referenceDataConnector: ReferenceDataService)(implicit ec: ExecutionContext) extends PageTransformer {

  def transform(holderOfTheTransitProcedureOption: Option[HolderOfTheTransitProcedureType06])(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] = userAnswers =>
    holderOfTheTransitProcedureOption match {
      case Some(holderOfTheTransitProcedure) =>
        referenceDataConnector
          .getCountryByCode(holderOfTheTransitProcedure.Address.country)
          .flatMap(
            country => set(CountryPage, country).apply(userAnswers)
          )
      case None =>
        Future.successful(userAnswers)
    }
}
