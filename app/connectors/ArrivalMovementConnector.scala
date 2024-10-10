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
import models.{ArrivalId, Bytes}
import models.P5.Messages
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import play.api.Logging
import play.api.http.HeaderNames.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Node, XML}

class ArrivalMovementConnector @Inject() (
  config: FrontendAppConfig,
  http: HttpClientV2,
  phaseConfig: PhaseConfig
)(implicit mat: Materializer)
    extends Logging {

  private val version = phaseConfig.values.apiVersion

  def getMessageMetaData(arrivalId: ArrivalId)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Messages] = {
    val url = url"${config.commonTransitConventionTradersUrl}movements/arrivals/${arrivalId.value}/messages"
    http
      .get(url)
      .setHeader(ACCEPT -> s"application/vnd.hmrc.$version+json")
      .execute[Messages]
  }

  def getMessage(arrivalId: ArrivalId, messageId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Node] = {

    def getBody(source: Source[ByteString, ?]): Future[String] =
      source
        .runReduce(_ ++ _)
        .map {
          byteString =>
            logger.info(s"[$arrivalId][$messageId] Size: ${Bytes(byteString.length)}")
            byteString.utf8String
        }

    val url = url"${config.commonTransitConventionTradersUrl}movements/arrivals/${arrivalId.value}/messages/$messageId/body"
    http
      .get(url)
      .setHeader(ACCEPT -> s"application/vnd.hmrc.$version+xml")
      .execute[HttpResponse]
      .map(_.bodyAsSource)
      .flatMap(getBody)
      .map(XML.loadString)
  }
}
