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

import generated.IncidentType03
import models.UserAnswers
import pages.incident.{IncidentCodePage, IncidentTextPage}
import pages.sections.incidents.IncidentSection
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IncidentsTransformer @Inject() (
  referenceDataService: ReferenceDataService,
  incidentEndorsementTransformer: IncidentEndorsementTransformer,
  incidentLocationTransformer: IncidentLocationTransformer,
  transhipmentTransformer: TranshipmentTransformer
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(
    incidents: Seq[IncidentType03]
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    incidents.mapWithSets {
      (value, incidentIndex) =>
        setSequenceNumber(IncidentSection(incidentIndex), value.sequenceNumber) andThen
          set(IncidentCodePage(incidentIndex), value.code, referenceDataService.getIncidentType) andThen
          set(IncidentTextPage(incidentIndex), value.text) andThen
          incidentEndorsementTransformer.transform(value.Endorsement, incidentIndex) andThen
          incidentLocationTransformer.transform(value.Location, incidentIndex) andThen
          transhipmentTransformer.transform(value.Transhipment, incidentIndex)
    }
}
