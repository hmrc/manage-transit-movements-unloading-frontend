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

package services

import connectors.ReferenceDataConnector
import models.reference.*
import models.reference.TransportMode.InlandMode
import models.{SecurityType, SelectableList}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataService @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) {

  def getAdditionalInformationCode(code: String)(implicit hc: HeaderCarrier): Future[AdditionalInformationCode] =
    referenceDataConnector
      .getAdditionalInformationCode(code)
      .map(_.resolve())

  def getAdditionalReferences()(implicit hc: HeaderCarrier): Future[SelectableList[AdditionalReferenceType]] =
    referenceDataConnector
      .getAdditionalReferences()
      .map(_.resolve())
      .map(_.toSelectableList)

  def getAdditionalReference(code: String)(implicit hc: HeaderCarrier): Future[AdditionalReferenceType] =
    referenceDataConnector
      .getAdditionalReference(code)
      .map(_.resolve())

  def isDocumentTypeExcise(docType: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    referenceDataConnector
      .getDocumentTypeExcise(docType)
      .map(_.isDefined)

  def getCountries()(implicit hc: HeaderCarrier): Future[Seq[Country]] =
    referenceDataConnector
      .getCountries()
      .map(_.resolve())
      .map(_.toSeq)

  def getCountry(code: String)(implicit hc: HeaderCarrier): Future[Country] =
    referenceDataConnector
      .getCountry(code)
      .map(_.resolve())

  def doesCUSCodeExist(cusCode: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    referenceDataConnector
      .getCUSCode(cusCode)
      .map(_.isDefined)

  def getCustomsOffice(code: String)(implicit hc: HeaderCarrier): Future[CustomsOffice] =
    referenceDataConnector
      .getCustomsOffice(code)
      .map(_.resolve())

  def getPreviousDocument(code: String)(implicit hc: HeaderCarrier): Future[DocumentType] =
    referenceDataConnector
      .getPreviousDocument(code)
      .map(_.resolve())

  def getSupportingDocument(code: String)(implicit hc: HeaderCarrier): Future[DocumentType] =
    referenceDataConnector
      .getSupportingDocument(code)
      .map(_.resolve())

  def getTransportDocument(code: String)(implicit hc: HeaderCarrier): Future[DocumentType] =
    referenceDataConnector
      .getTransportDocument(code)
      .map(_.resolve())

  def getPreviousDocumentExport(code: String)(implicit hc: HeaderCarrier): Future[DocumentType] =
    referenceDataConnector
      .getPreviousDocumentExport(code)
      .map(_.resolve())

  def getDocuments()(implicit hc: HeaderCarrier): Future[Seq[DocumentType]] =
    for {
      transportDocuments  <- referenceDataConnector.getTransportDocuments()
      supportingDocuments <- referenceDataConnector.getSupportingDocuments()
      documents = transportDocuments.resolve() ++ supportingDocuments.resolve()
    } yield documents.toSeq

  def getTransportDocuments()(implicit hc: HeaderCarrier): Future[Seq[DocumentType]] =
    referenceDataConnector
      .getTransportDocuments()
      .map(_.resolve())
      .map(_.toSeq)

  def getSupportingDocuments()(implicit hc: HeaderCarrier): Future[Seq[DocumentType]] =
    referenceDataConnector
      .getSupportingDocuments()
      .map(_.resolve())
      .map(_.toSeq)

  def getMeansOfTransportIdentificationTypes()(implicit hc: HeaderCarrier): Future[Seq[TransportMeansIdentification]] =
    referenceDataConnector
      .getMeansOfTransportIdentificationTypes()
      .map(_.resolve())
      .map(_.toSeq)

  def getMeansOfTransportIdentificationType(code: String)(implicit hc: HeaderCarrier): Future[TransportMeansIdentification] =
    referenceDataConnector
      .getMeansOfTransportIdentificationType(code)
      .map(_.resolve())

  def getPackageTypes()(implicit hc: HeaderCarrier): Future[SelectableList[PackageType]] =
    referenceDataConnector.getPackageTypes
      .map(_.resolve())
      .map(_.toSelectableList)

  def getPackageType(code: String)(implicit hc: HeaderCarrier): Future[PackageType] =
    referenceDataConnector
      .getPackageType(code)
      .map(_.resolve())

  def getSecurityType(code: String)(implicit hc: HeaderCarrier): Future[SecurityType] =
    referenceDataConnector
      .getSecurityType(code)
      .map(_.resolve())

  def getTransportModeCode(code: String)(implicit hc: HeaderCarrier): Future[InlandMode] =
    referenceDataConnector
      .getTransportModeCode(code)
      .map(_.resolve())

  def getQualifierOfIdentificationIncident(code: String)(implicit hc: HeaderCarrier): Future[QualifierOfIdentification] =
    referenceDataConnector
      .getQualifierOfIdentificationIncident(code)
      .map(_.resolve())

  def getIncidentType(code: String)(implicit hc: HeaderCarrier): Future[Incident] =
    referenceDataConnector
      .getIncidentType(code)
      .map(_.resolve())
}
