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
import models.DocType.{Previous, Support, Transport}
import models.reference.TransportMode.InlandMode
import models.reference._
import models.{DocType, SecurityType}
import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Reads}
import sttp.model.HeaderNames
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClientV2) extends Logging {

  def getCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Country]] = {
    val url = url"${config.referenceDataUrl}/lists/CountryCodesFullList"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[Country]]
  }

  def getCountry(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Country] = {
    val url = url"${config.referenceDataUrl}/lists/CountryCodesFullList"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.code" -> code))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[Country]]
      .map(_.head)
  }

  def getTransportModeCodes[T <: TransportMode[T]](implicit
    ec: ExecutionContext,
    hc: HeaderCarrier,
    rds: Reads[T],
    order: Order[T]
  ): Future[NonEmptySet[T]] = {
    val url = url"${config.referenceDataUrl}/lists/TransportModeCode"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[T]]
  }

  def getTransportModeCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[InlandMode] = {
    val url = url"${config.referenceDataUrl}/lists/TransportModeCode"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.code" -> code))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[InlandMode]]
      .map(_.head)
  }

  def getMeansOfTransportIdentificationTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[TransportMeansIdentification]] = {
    val url = url"${config.referenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[TransportMeansIdentification]]
  }

  def getSecurityType(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[SecurityType] = {
    val url = url"${config.referenceDataUrl}/lists/DeclarationTypeSecurity"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.code" -> typeValue))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[SecurityType]]
      .map(_.head)
  }

  def getMeansOfTransportIdentificationType(
    code: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[TransportMeansIdentification] = {
    val url = url"${config.referenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.code" -> code))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[TransportMeansIdentification]]
      .map(_.head)
  }

  def getCountryNameByCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Country] = {
    val url = url"${config.referenceDataUrl}/lists/CountryCodesFullList"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.code" -> code))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[Country]]
      .map(_.head)
  }

  def getCUSCode(cusCode: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[CUSCode]] = {

    val url = url"${config.referenceDataUrl}/lists/CUSCode"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.code" -> cusCode))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[CUSCode]]
  }

  def getCustomsOffice(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[CustomsOffice] = {
    val url = url"${config.referenceDataUrl}/lists/CustomsOffices"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.code" -> code))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[CustomsOffice]]
      .map(_.head)
  }

  def getPackageTypes(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[PackageType]] = {
    val url = url"${config.referenceDataUrl}/lists/KindOfPackages"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[PackageType]]
  }

  def getPackageType(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[PackageType] = {
    val url = url"${config.referenceDataUrl}/lists/KindOfPackages"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.code" -> typeValue))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[PackageType]]
      .map(_.head)
  }

  def getIncidentType(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Incident] = {
    val url = url"${config.referenceDataUrl}/lists/IncidentCode"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.code" -> typeValue))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[Incident]]
      .map(_.head)
  }

  def getSupportingDocument(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DocumentType] =
    getDocument(Support, "SupportingDocumentType", typeValue)

  def getAdditionalReferences()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[AdditionalReferenceType]] = {
    val url = url"${config.referenceDataUrl}/lists/AdditionalReference"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[AdditionalReferenceType]]
  }

  def getAdditionalReferenceType(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[AdditionalReferenceType] = {
    val url = url"${config.referenceDataUrl}/lists/AdditionalReference"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.code" -> typeValue))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[AdditionalReferenceType]]
      .map(_.head)
  }

  def getAdditionalInformationCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[AdditionalInformationCode] = {
    val url = url"${config.referenceDataUrl}/lists/AdditionalInformation"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.code" -> code))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[AdditionalInformationCode]]
      .map(_.head)
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
    val url                                 = url"${config.referenceDataUrl}/lists/$path"
    implicit val reads: Reads[DocumentType] = DocumentType.reads(dt)

    http
      .get(url)
      .transform(_.addQueryStringParameters(queryParams.head))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[DocumentType]]
  }

  def getQualifierOfIdentificationIncident(qualifier: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[QualifierOfIdentification] = {
    val url = url"${config.referenceDataUrl}/lists/QualifierOfIdentificationIncident"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.code" -> qualifier))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[QualifierOfIdentification]]
      .map(_.head)
  }

  private def getDocument(dt: DocType, path: String, typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DocumentType] =
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
