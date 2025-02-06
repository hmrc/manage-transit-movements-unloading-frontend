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
import models.reference.*
import models.reference.TransportMode.InlandMode
import models.{DocType, SecurityType}
import play.api.Logging
import play.api.cache.*
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Reads}
import sttp.model.HeaderNames
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

@Singleton
class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClientV2, cache: AsyncCacheApi) extends Logging {

  private def get[T](url: URL)(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[NonEmptySet[T]]): Future[NonEmptySet[T]] =
    http
      .get(url)
      .setHeader(HeaderNames.Accept -> "application/vnd.hmrc.2.0+json")
      .execute[NonEmptySet[T]]

  // https://www.playframework.com/documentation/2.6.x/ScalaCache#Accessing-the-Cache-API
  private def getOrElseUpdate[T: ClassTag](url: URL)(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[NonEmptySet[T]]): Future[T] =
    cache.getOrElseUpdate[T](url.toString, config.asyncCacheApiExpiration.seconds) {
      get[T](url).map(_.head)
    }

  def getCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Country]] = {
    val url = url"${config.referenceDataUrl}/lists/CountryCodesFullList"
    get[Country](url)
  }

  def getCountry(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Country] = {
    val queryParameters = Seq("data.code" -> code)
    val url             = url"${config.referenceDataUrl}/lists/CountryCodesFullList?$queryParameters"
    getOrElseUpdate[Country](url)
  }

  def getTransportModeCodes[T <: TransportMode[T]](implicit
    ec: ExecutionContext,
    hc: HeaderCarrier,
    rds: Reads[T],
    order: Order[T]
  ): Future[NonEmptySet[T]] = {
    val url = url"${config.referenceDataUrl}/lists/TransportModeCode"
    get[T](url)
  }

  def getTransportModeCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[InlandMode] = {
    val queryParameters = Seq("data.code" -> code)
    val url             = url"${config.referenceDataUrl}/lists/TransportModeCode?$queryParameters"
    getOrElseUpdate[InlandMode](url)
  }

  def getMeansOfTransportIdentificationTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[TransportMeansIdentification]] = {
    val url = url"${config.referenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport"
    get[TransportMeansIdentification](url)
  }

  def getSecurityType(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[SecurityType] = {
    val queryParameters = Seq("data.code" -> typeValue)
    val url             = url"${config.referenceDataUrl}/lists/DeclarationTypeSecurity?$queryParameters"
    getOrElseUpdate[SecurityType](url)
  }

  def getMeansOfTransportIdentificationType(
    code: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[TransportMeansIdentification] = {
    val queryParameters = Seq("data.type" -> code)
    val url             = url"${config.referenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport?$queryParameters"
    getOrElseUpdate[TransportMeansIdentification](url)
  }

  def getCountryNameByCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Country] = {
    val queryParameters = Seq("data.code" -> code)
    val url             = url"${config.referenceDataUrl}/lists/CountryCodesFullList?$queryParameters"
    getOrElseUpdate[Country](url)
  }

  def getCUSCode(cusCode: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[CUSCode] = {
    val queryParameters = Seq("data.code" -> cusCode)
    val url             = url"${config.referenceDataUrl}/lists/CUSCode?$queryParameters"
    getOrElseUpdate[CUSCode](url)
  }

  def getCustomsOffice(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[CustomsOffice] = {
    val queryParameters = Seq("data.id" -> code)
    val url             = url"${config.referenceDataUrl}/lists/CustomsOffices?$queryParameters"
    getOrElseUpdate[CustomsOffice](url)
  }

  def getPackageTypes(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[PackageType]] = {
    val url = url"${config.referenceDataUrl}/lists/KindOfPackages"
    get[PackageType](url)
  }

  def getPackageType(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[PackageType] = {
    val queryParameters = Seq("data.code" -> typeValue)
    val url             = url"${config.referenceDataUrl}/lists/KindOfPackages?$queryParameters"
    getOrElseUpdate[PackageType](url)
  }

  def getIncidentType(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Incident] = {
    val queryParameters = Seq("data.code" -> typeValue)
    val url             = url"${config.referenceDataUrl}/lists/IncidentCode?$queryParameters"
    getOrElseUpdate[Incident](url)
  }

  def getSupportingDocument(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DocumentType] =
    getDocument(Support, "SupportingDocumentType", typeValue)

  def getAdditionalReferences()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[AdditionalReferenceType]] = {
    val url = url"${config.referenceDataUrl}/lists/AdditionalReference"
    get[AdditionalReferenceType](url)
  }

  def getAdditionalReferenceType(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[AdditionalReferenceType] = {
    val queryParameters = Seq("data.documentType" -> typeValue)
    val url             = url"${config.referenceDataUrl}/lists/AdditionalReference?$queryParameters"
    getOrElseUpdate[AdditionalReferenceType](url)
  }

  def getAdditionalInformationCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[AdditionalInformationCode] = {
    val queryParameters = Seq("data.code" -> code)
    val url             = url"${config.referenceDataUrl}/lists/AdditionalInformation?$queryParameters"
    getOrElseUpdate[AdditionalInformationCode](url)
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

  def getPreviousDocumentExport(typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DocumentType] =
    getDocument(Previous, "PreviousDocumentExportType", typeValue)

  def getDocuments(dt: DocType, path: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[DocumentType]] = {
    implicit val reads: Reads[DocumentType] = DocumentType.reads(dt)

    val url = url"${config.referenceDataUrl}/lists/$path"
    get[DocumentType](url)
  }

  private def getDocument(dt: DocType, path: String, typeValue: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DocumentType] = {
    implicit val reads: Reads[DocumentType] = DocumentType.reads(dt)

    val queryParameters = Seq("data.code" -> typeValue)
    val url             = url"${config.referenceDataUrl}/lists/$path?$queryParameters"
    getOrElseUpdate[DocumentType](url)
  }

  def getQualifierOfIdentificationIncident(qualifier: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[QualifierOfIdentification] = {
    val queryParameters = Seq("data.qualifier" -> qualifier)
    val url             = url"${config.referenceDataUrl}/lists/QualifierOfIdentificationIncident?$queryParameters"
    getOrElseUpdate[QualifierOfIdentification](url)
  }

  def getDocumentTypeExcise(docType: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DocTypeExcise] = {
    val queryParameters = Seq("data.code" -> docType)
    val url             = url"${config.referenceDataUrl}/lists/DocumentTypeExcise?$queryParameters"
    getOrElseUpdate[DocTypeExcise](url)
  }

  implicit def responseHandlerGeneric[A](implicit reads: Reads[List[A]], order: Order[A]): HttpReads[NonEmptySet[A]] =
    (_: String, url: String, response: HttpResponse) =>
      response.status match {
        case OK =>
          (response.json \ "data").validate[List[A]] match {
            case JsSuccess(Nil, _) =>
              throw new NoReferenceDataFoundException(url)
            case JsSuccess(head :: tail, _) =>
              NonEmptySet.of(head, tail*)
            case JsError(errors) =>
              throw JsResultException(errors)
          }
        case e =>
          logger.warn(s"[ReferenceDataConnector][responseHandlerGeneric] Reference data call returned $e")
          throw new Exception(s"[ReferenceDataConnector][responseHandlerGeneric] $e - ${response.body}")
      }
}

object ReferenceDataConnector {

  class NoReferenceDataFoundException(url: String) extends Exception(s"The reference data call was successful but the response body is empty: $url")
}
