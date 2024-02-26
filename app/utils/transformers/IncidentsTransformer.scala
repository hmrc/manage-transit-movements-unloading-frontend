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
import generated._
import models.reference.{Country, Incident}
import models.{Index, UserAnswers}
import pages.incident.endorsement.EndorsementCountryPage
import pages.incident.{IncidentCodePage, IncidentTextPage}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IncidentsTransformer @Inject() (
  referenceDataConnector: ReferenceDataConnector,
  incidentLocationTransformer: IncidentLocationTransformer
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  private case class TempIncident(
    typeValue: Incident,
    text: String,
    endorsement: Option[EndorsementType03],
    endorsementCountry: Option[Country],
    location: LocationType02
  )

  def transform(incidents: Seq[generated.IncidentType04])(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers => {

    lazy val incidentRefLookups = incidents.map {
      incidentType0 =>
        val incidentF = referenceDataConnector.getIncidentType(incidentType0.code)
        val countryF  = incidentType0.Endorsement.map(_.country).lookup(referenceDataConnector.getCountry)
        for {
          incident <- incidentF
          country  <- countryF
        } yield TempIncident(incident, incidentType0.text, incidentType0.Endorsement, country, incidentType0.Location)
    }

    Future.sequence(incidentRefLookups).flatMap {
      _.zipWithIndex.foldLeft(Future.successful(userAnswers))({
        case (acc, (tempIncident, i)) =>
          val incidentIndex: Index = Index(i)
          acc.flatMap {
            userAnswers =>
              val pipeline: UserAnswers => Future[UserAnswers] =
                set(IncidentCodePage(incidentIndex), tempIncident.typeValue) andThen
                  set(IncidentTextPage(incidentIndex), tempIncident.text) andThen
                  set(EndorsementCountryPage(incidentIndex), tempIncident.endorsementCountry) andThen
                  incidentLocationTransformer.transform(tempIncident.location, incidentIndex)

              pipeline(userAnswers)
          }
      })
    }
  }
}
