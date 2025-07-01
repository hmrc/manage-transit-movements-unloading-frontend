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

import generated.EndorsementType02
import models.{Index, UserAnswers}
import pages.incident.endorsement.EndorsementCountryPage
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IncidentEndorsementTransformer @Inject() (
  referenceDataService: ReferenceDataService
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(endorsement: Option[EndorsementType02], incidentIndex: Index)(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    endorsement match {
      case Some(EndorsementType02(date, authority, place, countryCode)) =>
        val countryF = referenceDataService.getCountry(countryCode)

        for {
          country <- countryF
          userAnswers <- {
            val pipeline = set(EndorsementCountryPage(incidentIndex), country)

            pipeline(userAnswers)
          }
        } yield userAnswers
      case None => Future.successful(userAnswers)
    }
}
