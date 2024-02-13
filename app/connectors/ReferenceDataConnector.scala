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
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.transport.TransportMode
import models.reference._
import play.api.http.Status._
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Reads}
import sttp.model.HeaderNames
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClient) extends Logging {

  def getCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Country]] = {
    val serviceUrl = s"${config.referenceDataUrl}/lists/CountryCodesFullList"
    http.GET[NonEmptySet[Country]](serviceUrl, headers = version2Header)
  }

  def getTransportModeCodes[T <: TransportMode[T]](implicit
    ec: ExecutionContext,
    hc: HeaderCarrier,
    rds: Reads[T],
    order: Order[T]
  ): Future[NonEmptySet[T]] = {
    val url = s"${config.referenceDataUrl}/lists/TransportModeCode"
    http.GET[NonEmptySet[T]](url, headers = version2Header)
  }

  def getMeansOfTransportIdentificationTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[TransportMeansIdentification]] = {
    val url = s"${config.referenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport"
    http.GET[NonEmptySet[TransportMeansIdentification]](url, headers = version2Header)
  }

  def getMeansOfTransportIdentificationType(
    code: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[TransportMeansIdentification]] = {
    val queryParams: Seq[(String, String)] = Seq("data.type" -> code)
    val url                                = s"${config.referenceDataUrl}/filtered-lists/TypeOfIdentificationOfMeansOfTransport"
    http.GET[NonEmptySet[TransportMeansIdentification]](url, headers = version2Header, queryParams = queryParams)
  }

  def getCountryNameByCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Country]] = {

    val queryParams: Seq[(String, String)] = Seq(
      "data.code" -> code
    )

    val serviceUrl = s"${config.referenceDataUrl}/filtered-lists/CountryCodesFullList"

    http.GET[NonEmptySet[Country]](serviceUrl, headers = version2Header, queryParams = queryParams)
  }

  def getCUSCode(cusCode: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[CUSCode]] = {
    val queryParams: Seq[(String, String)] = Seq("data.code" -> cusCode)

    val serviceUrl = s"${config.referenceDataUrl}/filtered-lists/CUSCode"
    http.GET[NonEmptySet[CUSCode]](serviceUrl, headers = version2Header, queryParams = queryParams)
  }

  def getCustomsOffice(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[CustomsOffice]] = {

    val queryParams: Seq[(String, String)] = Seq(
      "data.id" -> code
    )

    val serviceUrl = s"${config.referenceDataUrl}/filtered-lists/CustomsOffices"

    http.GET[NonEmptySet[CustomsOffice]](serviceUrl, headers = version2Header, queryParams = queryParams)
  }

  def getPackageTypes(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[PackageType]] = {
    val serviceUrl = s"${config.referenceDataUrl}/lists/KindOfPackages"
    http.GET[NonEmptySet[PackageType]](serviceUrl, headers = version2Header)(responseHandlerGeneric(PackageType.format, PackageType.order), hc, ec)
  }

  def getSupportingDocument(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DocumentType] = {
    val queryParams: Seq[(String, String)] = Seq("data.code" -> typeValue)
    val url                                = s"${config.referenceDataUrl}/lists/SupportingDocumentType"
    http.GET[NonEmptySet[DocumentType]](url, headers = version2Header, queryParams = queryParams).map(_.head)
  }

  def getTransportDocument(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DocumentType] = {
    val queryParams: Seq[(String, String)] = Seq("data.code" -> typeValue)
    val url                                = s"${config.referenceDataUrl}/lists/TransportDocumentType"
    http.GET[NonEmptySet[DocumentType]](url, headers = version2Header, queryParams = queryParams).map(_.head)
  }

  private def version2Header: Seq[(String, String)] = Seq(
    HeaderNames.Accept -> "application/vnd.hmrc.2.0+json"
  )

  implicit def responseHandlerGeneric[A](implicit reads: Reads[A], order: Order[A]): HttpReads[NonEmptySet[A]] =
    (_: String, _: String, response: HttpResponse) => {
      response.status match {
        case OK =>
          (response.json \ "data").validate[List[A]] match {
            case JsSuccess(Nil, _) =>
              throw new NoReferenceDataFoundException
            case JsSuccess(head :: tail, _) =>
              NonEmptySet.of(head, tail: _*)
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
