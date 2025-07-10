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

import config.FrontendAppConfig
import models.ArrivalId
import models.P5.Messages
import play.api.http.HeaderNames.{ACCEPT, CONTENT_TYPE}
import play.api.libs.ws.XMLBodyWritables.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Node, NodeSeq, XML}

class ArrivalMovementConnector @Inject() (
  config: FrontendAppConfig,
  http: HttpClientV2
)(implicit ec: ExecutionContext) {

  private val version = config.phase6Enabled match {
    case _ => 2.1
  }

  def getMessageMetaData(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Messages] = {
    val url = url"${config.commonTransitConventionTradersUrl}movements/arrivals/${arrivalId.value}/messages"
    http
      .get(url)
      .setHeader(ACCEPT -> s"application/vnd.hmrc.$version+json")
      .execute[Messages]
  }

  def getMessage(arrivalId: ArrivalId, messageId: String)(implicit hc: HeaderCarrier): Future[Node] = {
    val url = url"${config.commonTransitConventionTradersUrl}movements/arrivals/${arrivalId.value}/messages/$messageId/body"
    http
      .get(url)
      .setHeader(ACCEPT -> s"application/vnd.hmrc.$version+xml")
      .execute[HttpResponse]
      .map(_.body)
      .map(XML.loadString)
  }

  def submit(xml: NodeSeq, arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val url = url"${config.commonTransitConventionTradersUrl}movements/arrivals/${arrivalId.value}/messages"
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
