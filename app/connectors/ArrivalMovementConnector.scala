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
import play.api.http.HeaderNames._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Node, XML}

class ArrivalMovementConnector @Inject() (config: FrontendAppConfig, http: HttpClientV2) {

  def getMessageMetaData(arrivalId: ArrivalId)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Messages] = {
    val url = url"${config.commonTransitConventionTradersUrl}movements/arrivals/${arrivalId.value}/messages"
    http
      .get(url)
      .setHeader(ACCEPT -> "application/vnd.hmrc.2.0+json")
      .execute[Messages]
  }

  def getMessage(arrivalId: ArrivalId, messageId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Node] = {
    val url = url"${config.commonTransitConventionTradersUrl}movements/arrivals/${arrivalId.value}/messages/$messageId/body"
    http
      .get(url)
      .setHeader(ACCEPT -> "application/vnd.hmrc.2.0+xml")
      .execute[HttpResponse]
      .map(_.body)
      .map(XML.loadString)
  }
}
