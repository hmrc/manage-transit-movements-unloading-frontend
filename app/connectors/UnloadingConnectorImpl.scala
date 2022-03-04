/*
 * Copyright 2022 HM Revenue & Customs
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
import javax.inject.Inject
import logging.Logging
import models.XMLWrites._
import models.messages.UnloadingRemarksRequest
import models.response.ResponseArrival
import models.{XMLReads, _}
import play.api.http.HeaderNames
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class UnloadingConnectorImpl @Inject() (
  val config: FrontendAppConfig,
  val http: HttpClient,
  val ws: WSClient
)(implicit ec: ExecutionContext)
    extends UnloadingConnector
    with HttpErrorFunctions
    with Logging {

  private val channel: String = "web"

  def post(arrivalId: ArrivalId, unloadingRemarksRequest: UnloadingRemarksRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val url = s"${config.arrivalsBackend}/movements/arrivals/${arrivalId.value}/messages/"

    val headers = Seq(ContentTypeHeader("application/xml"), ChannelHeader(channel))

    http.POSTString[HttpResponse](url, unloadingRemarksRequest.toXml.toString, headers)
  }

  def getUnloadingPermission(unloadingPermission: String)(implicit hc: HeaderCarrier): Future[Option[UnloadingPermission]] = {

    val serviceUrl = s"${config.arrivalsBackendBaseUrl}$unloadingPermission"
    val header     = hc.withExtraHeaders(ChannelHeader(channel))

    http.GET[HttpResponse](serviceUrl)(httpReads, header, ec) map {
      case responseMessage if is2xx(responseMessage.status) =>
        val message: NodeSeq = responseMessage.json.as[ResponseMovementMessage].message
        XMLReads.readAs[UnloadingPermission](message)
      case _ =>
        logger.error(s"Get UnloadingPermission failed to return data")
        None
    }
  }

  def getSummary(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Option[MessagesSummary]] = {

    val serviceUrl: String = s"${config.arrivalsBackend}/movements/arrivals/${arrivalId.value}/messages/summary"
    val header             = hc.withExtraHeaders(ChannelHeader(channel))

    http.GET[HttpResponse](serviceUrl)(httpReads, header, ec) map {
      case responseMessage if is2xx(responseMessage.status) =>
        Some(responseMessage.json.as[MessagesSummary])
      case _ =>
        logger.error(s"Get Summary failed to return data")
        None
    }
  }

  def getRejectionMessage(rejectionLocation: String)(implicit hc: HeaderCarrier): Future[Option[UnloadingRemarksRejectionMessage]] = {
    val serviceUrl = s"${config.arrivalsBackendBaseUrl}$rejectionLocation"
    val header     = hc.withExtraHeaders(ChannelHeader(channel))

    http.GET[HttpResponse](serviceUrl)(httpReads, header, ec) map {
      case responseMessage if is2xx(responseMessage.status) =>
        val message: NodeSeq = responseMessage.json.as[ResponseMovementMessage].message
        XMLReads.readAs[UnloadingRemarksRejectionMessage](message)
      case _ =>
        logger.error(s"Get Rejection Message failed to return data")
        None
    }
  }

  def getUnloadingRemarksMessage(unloadingRemarksLocation: String)(implicit hc: HeaderCarrier): Future[Option[UnloadingRemarksRequest]] = {
    val serviceUrl = s"${config.arrivalsBackendBaseUrl}$unloadingRemarksLocation"
    val header     = hc.withExtraHeaders(ChannelHeader(channel))

    http.GET[HttpResponse](serviceUrl)(httpReads, header, ec) map {
      case responseMessage if is2xx(responseMessage.status) =>
        val message: NodeSeq = responseMessage.json.as[ResponseMovementMessage].message
        XMLReads.readAs[UnloadingRemarksRequest](message)
      case _ =>
        logger.error(s"getUnloadingRemarksMessage failed to return data")
        None
    }
  }

  def getPDF(arrivalId: ArrivalId, bearerToken: String)(implicit hc: HeaderCarrier): Future[WSResponse] = {
    val serviceUrl: String = s"${config.arrivalsBackend}/movements/arrivals/${arrivalId.value}/unloading-permission"
    ws.url(serviceUrl)
      .withHttpHeaders(ChannelHeader(channel), AuthorizationHeader(bearerToken))
      .get
  }

  override def getArrival(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Option[ResponseArrival]] = {

    val serviceUrl: String = s"${config.arrivalsBackend}/movements/arrivals/${arrivalId.value}"
    val header             = hc.withExtraHeaders(ChannelHeader(channel))

    http.GET[HttpResponse](serviceUrl)(httpReads, header, ec) map {
      case responseMessage if is2xx(responseMessage.status) =>
        Some(responseMessage.json.as[ResponseArrival])
      case _ =>
        logger.error(s"Get Arrival failed to return data")
        None
    }
  }

  object ChannelHeader {
    def apply(value: String): (String, String) = ("Channel", value)
  }

  object ContentTypeHeader {
    def apply(value: String): (String, String) = (HeaderNames.CONTENT_TYPE, value)
  }

  object AuthorizationHeader {
    def apply(value: String): (String, String) = (HeaderNames.AUTHORIZATION, value)
  }

}

trait UnloadingConnector {
  def getUnloadingPermission(unloadingPermission: String)(implicit headerCarrier: HeaderCarrier): Future[Option[UnloadingPermission]]
  def post(arrivalId: ArrivalId, unloadingRemarksRequest: UnloadingRemarksRequest)(implicit hc: HeaderCarrier): Future[HttpResponse]
  def getSummary(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Option[MessagesSummary]]
  def getRejectionMessage(rejectionLocation: String)(implicit hc: HeaderCarrier): Future[Option[UnloadingRemarksRejectionMessage]]
  def getUnloadingRemarksMessage(unloadinRemarksLocation: String)(implicit hc: HeaderCarrier): Future[Option[UnloadingRemarksRequest]]
  def getPDF(arrivalId: ArrivalId, bearerToken: String)(implicit hc: HeaderCarrier): Future[WSResponse]
  def getArrival(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Option[ResponseArrival]]
}
