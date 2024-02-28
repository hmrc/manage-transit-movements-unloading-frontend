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
import models.reference.Incident
import models.{Index, UserAnswers}
import pages.incident.{IncidentCodePage, IncidentTextPage}
import pages.sections.incidents.IncidentSection
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IncidentsTransformer @Inject() (
  referenceDataConnector: ReferenceDataConnector,
  incidentEndorsementTransformer: IncidentEndorsementTransformer,
  incidentLocationTransformer: IncidentLocationTransformer,
  transhipmentIdentificationTransformer: TranshipmentTransformer
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  private case class TempIncident[T](
    underlying: T,
    typeValue: Incident
  )

  def transform(incidents: Seq[generated.IncidentType04])(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers => {

    lazy val incidentRefLookups = incidents.map {
      incidentType0 =>
        val incidentF = referenceDataConnector.getIncidentType(incidentType0.code)
        for {
          incident <- incidentF
        } yield TempIncident(incidentType0, incident)
    }

    Future.sequence(incidentRefLookups).flatMap {
      _.zipWithIndex.foldLeft(Future.successful(userAnswers))({
        case (acc, (TempIncident(underlying, typeValue), i)) =>
          val incidentIndex: Index = Index(i)
          acc.flatMap {
            userAnswers =>
              val pipeline: UserAnswers => Future[UserAnswers] =
                setSequenceNumber(IncidentSection(incidentIndex), underlying.sequenceNumber) andThen
                  set(IncidentCodePage(incidentIndex), typeValue) andThen
                  set(IncidentTextPage(incidentIndex), underlying.text) andThen
                  incidentEndorsementTransformer.transform(underlying.Endorsement, incidentIndex) andThen
                  incidentLocationTransformer.transform(underlying.Location, incidentIndex) andThen
                  transhipmentIdentificationTransformer.transform(underlying.Transhipment, incidentIndex)

              pipeline(userAnswers)
          }
      })
    }
  }
}
