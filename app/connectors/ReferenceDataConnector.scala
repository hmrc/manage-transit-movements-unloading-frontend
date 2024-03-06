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
import models.DocType.{Previous, Support, Transport}
import models.departureTransportMeans.TransportMeansIdentification
import models.reference._
import models.reference.transport.TransportMode
import models.{DocType, SecurityType}
import play.api.http.Status._
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Reads}
import sttp.model.HeaderNames
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClient) extends Logging {

  def getCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Country]] = {
    val url = s"${config.referenceDataUrl}/lists/CountryCodesFullList"
    http.GET[NonEmptySet[Country]](url, headers = version2Header)
  }

  def getCountry(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Country] = {
    val queryParams: Seq[(String, String)] = Seq("data.code" -> code)
    val url                                = s"${config.referenceDataUrl}/lists/CountryCodesFullList"
    http.GET[NonEmptySet[Country]](url, headers = version2Header, queryParams = queryParams).map(_.head)
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

  def getSecurityType(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[SecurityType] = {
    val queryParams: Seq[(String, String)] = Seq("data.code" -> typeValue)
    val url                                = s"${config.referenceDataUrl}/lists/DeclarationTypeSecurity"
    http
      .GET[NonEmptySet[SecurityType]](url, headers = version2Header, queryParams = queryParams)(
        responseHandlerGeneric(SecurityType.format, SecurityType.order),
        hc,
        ec
      )
      .map(_.head)
  }

  def getMeansOfTransportIdentificationType(
    code: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[TransportMeansIdentification] = {
    val queryParams: Seq[(String, String)] = Seq("data.type" -> code)
    val url                                = s"${config.referenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport"
    http.GET[NonEmptySet[TransportMeansIdentification]](url, headers = version2Header, queryParams = queryParams).map(_.head)
  }

  def getCountryNameByCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Country]] = {

    val queryParams: Seq[(String, String)] = Seq(
      "data.code" -> code
    )

    val url = s"${config.referenceDataUrl}/lists/CountryCodesFullList"

    http.GET[NonEmptySet[Country]](url, headers = version2Header, queryParams = queryParams)
  }

  def getCUSCode(cusCode: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[CUSCode]] = {
    val queryParams: Seq[(String, String)] = Seq("data.code" -> cusCode)

    val url = s"${config.referenceDataUrl}/lists/CUSCode"
    http.GET[NonEmptySet[CUSCode]](url, headers = version2Header, queryParams = queryParams)
  }

  def getCustomsOffice(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[CustomsOffice]] = {

    val queryParams: Seq[(String, String)] = Seq(
      "data.id" -> code
    )

    val url = s"${config.referenceDataUrl}/lists/CustomsOffices"

    http.GET[NonEmptySet[CustomsOffice]](url, headers = version2Header, queryParams = queryParams)
  }

  def getPackageTypes(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[PackageType]] = {
    val url = s"${config.referenceDataUrl}/lists/KindOfPackages"
    http.GET[NonEmptySet[PackageType]](url, headers = version2Header)(responseHandlerGeneric(PackageType.format, PackageType.order), hc, ec)
  }

  def getPackageType(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[PackageType] = {
    val queryParams: Seq[(String, String)] = Seq("data.code" -> typeValue)
    val url                                = s"${config.referenceDataUrl}/lists/KindOfPackages"
    http
      .GET[NonEmptySet[PackageType]](url, headers = version2Header, queryParams = queryParams)(
        responseHandlerGeneric(PackageType.format, PackageType.order),
        hc,
        ec
      )
      .map(_.head)
  }

  def getIncidentType(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Incident] = {
    val queryParams: Seq[(String, String)] = Seq("data.code" -> typeValue)
    val serviceUrl                         = s"${config.referenceDataUrl}/lists/IncidentCode"
    http
      .GET[NonEmptySet[Incident]](serviceUrl, headers = version2Header, queryParams = queryParams)(
        responseHandlerGeneric(Incident.format, Incident.order),
        hc,
        ec
      )
      .map(_.head)
  }

  def getSupportingDocument(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DocumentType] =
    getDocument(Support, "SupportingDocumentType", typeValue)

  def getAdditionalReferences()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[AdditionalReferenceType]] = {
    val url = s"${config.referenceDataUrl}/lists/AdditionalReference"
    http.GET[NonEmptySet[AdditionalReferenceType]](url, headers = version2Header)
  }

  def getAdditionalReferenceType(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[AdditionalReferenceType] = {
    val queryParams: Seq[(String, String)] = Seq("data.documentType" -> typeValue)
    val url                                = s"${config.referenceDataUrl}/lists/AdditionalReference"
    http.GET[NonEmptySet[AdditionalReferenceType]](url, headers = version2Header, queryParams = queryParams).map(_.head)
  }

  def getAdditionalInformationCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[AdditionalInformationCode] = {
    val queryParams: Seq[(String, String)] = Seq("data.code" -> code)
    val url                                = s"${config.referenceDataUrl}/lists/AdditionalInformation"
    http.GET[NonEmptySet[AdditionalInformationCode]](url, headers = version2Header, queryParams = queryParams).map(_.head)
  }

  def getTransportDocument(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DocumentType] =
    getDocument(Transport, "TransportDocumentType", typeValue)

  def getSupportingDocuments()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[DocumentType]] =
    getDocuments(Support, "SupportingDocumentType")

  def getTransportDocuments()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[DocumentType]] =
    getDocuments(Transport, "TransportDocumentType")

  def getPreviousDocuments()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[DocumentType]] =
    getDocuments(Previous, "PreviousDocumentType")

  def getPreviousDocument(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DocumentType] =
    getDocument(Previous, "PreviousDocumentType", typeValue)

  def getDocuments(dt: DocType, path: String, queryParams: Seq[(String, String)] = Seq())(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[NonEmptySet[DocumentType]] = {
    val url                                 = s"${config.referenceDataUrl}/lists/$path"
    implicit val reads: Reads[DocumentType] = DocumentType.reads(dt)
    http.GET[NonEmptySet[DocumentType]](url, headers = version2Header, queryParams = queryParams)
  }

  def getQualifierOfIdentificationIncident(qualifier: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[QualifierOfIdentification] = {
    val queryParams: Seq[(String, String)] = Seq("data.qualifier" -> qualifier)
    val url                                = s"${config.referenceDataUrl}/lists/QualifierOfIdentificationIncident"
    http.GET[NonEmptySet[QualifierOfIdentification]](url, headers = version2Header, queryParams = queryParams).map(_.head)
  }

  def getDocument(dt: DocType, path: String, typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DocumentType] =
    getDocuments(dt, path, Seq("data.code" -> typeValue)).map(_.head)

  private def version2Header: Seq[(String, String)] = Seq(
    HeaderNames.Accept -> "application/vnd.hmrc.2.0+json"
  )

  implicit def responseHandlerGeneric[A](implicit reads: Reads[A], order: Order[A]): HttpReads[NonEmptySet[A]] =
    (_: String, url: String, response: HttpResponse) => {
      response.status match {
        case OK =>
          (response.json \ "data").validate[List[A]] match {
            case JsSuccess(Nil, _) =>
              throw new NoReferenceDataFoundException(url)
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

  class NoReferenceDataFoundException(url: String) extends Exception(s"The reference data call was successful but the response body is empty: $url")
}
