/*
 * Copyright 2023 HM Revenue & Customs
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

package connectors

import config.{FrontendAppConfig, PhaseConfig}
import models.ArrivalId
import play.api.Logging
import play.api.http.HeaderNames.{ACCEPT, CONTENT_TYPE}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class ApiConnector @Inject() (
  http: HttpClientV2,
  appConfig: FrontendAppConfig,
  phaseConfig: PhaseConfig
)(implicit ec: ExecutionContext)
    extends HttpErrorFunctions
    with Logging {

  private val version = phaseConfig.values.apiVersion

  def submit(xml: NodeSeq, arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val url = url"${appConfig.commonTransitConventionTradersUrl}movements/arrivals/${arrivalId.value}/messages"
    http
      .post(url)
      .setHeader(
        ACCEPT       -> s"application/vnd.hmrc.$version+json",
        CONTENT_TYPE -> "application/xml"
      )
      .withBody(xml)
      .execute[HttpResponse]
  }
}
