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

import cats.Order
import cats.data.NonEmptySet
import config.FrontendAppConfig
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import logging.Logging
import metrics.MetricsService
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.transport.TransportMode
import models.reference.{Country, CustomsOffice}
import play.api.http.Status._
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Reads}
import sttp.model.HeaderNames
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClient, metricsService: MetricsService) extends Logging {

  def getCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[Country]] = {
    val serviceUrl = s"${config.referenceDataUrl}/lists/CountryCodesFullList"
    http.GET[Seq[Country]](serviceUrl, headers = version2Header)
  }

  def getTransportModeCodes[T <: TransportMode[T]]()(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier,
    rds: Reads[T],
    order: Order[T]
  ): Future[Seq[T]] = {
    val url = s"${config.referenceDataUrl}/lists/TransportModeCode"
    http.GET[Seq[T]](url, headers = version2Header)
  }

  def getMeansOfTransportIdentificationTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[TransportMeansIdentification]] = {
    val url = s"${config.referenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport"
    http.GET[Seq[TransportMeansIdentification]](url, headers = version2Header)
  }

  def getCountryNameByCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[Country]] = {

    val queryParams: Seq[(String, String)] = Seq(
      "data.code" -> code
    )

    val serviceUrl = s"${config.referenceDataUrl}/filtered-lists/CountryCodesFullList"

    http.GET[Seq[Country]](serviceUrl, headers = version2Header, queryParams = queryParams)
  }

  def getCustomsOffice(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[CustomsOffice]] = {

    val queryParams: Seq[(String, String)] = Seq(
      "data.id" -> code
    )

    val serviceUrl = s"${config.referenceDataUrl}/filtered-lists/CustomsOffices"

    http.GET[Seq[CustomsOffice]](serviceUrl, headers = version2Header, queryParams = queryParams)
  }

  private def version2Header: Seq[(String, String)] = Seq(
    HeaderNames.Accept -> "application/vnd.hmrc.2.0+json"
  )

  implicit def responseHandlerGeneric[A](implicit reads: Reads[A]): HttpReads[Seq[A]] =
    (_: String, _: String, response: HttpResponse) => {
      response.status match {
        case OK =>
          (response.json \ "data").validate[Seq[A]] match {
            case JsSuccess(Nil, _) =>
              throw new NoReferenceDataFoundException
            case JsSuccess(value, _) =>
              value
            case JsError(errors) =>
              throw JsResultException(errors)
          }
        case e =>
          logger.warn(s"[ReferenceDataConnector][responseHandlerGeneric] Reference data call returned $e")
          throw new Exception(s"[ReferenceDataConnector][responseHandlerGeneric] $e - ${response.body}")
      }
    }
}

object ReferenceDataConnector {

  class NoReferenceDataFoundException extends Exception("The reference data call was successful but the response body is empty.")
}
