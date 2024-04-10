/*
 * Copyright 2024 HM Revenue & Customs
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

package utils.transformers

import connectors.ReferenceDataConnector
import generated.HouseConsignmentType04
import models.{Index, RichPreviousDocuments07, UserAnswers}
import pages.houseConsignment.index.SecurityIndicatorFromExportDeclarationPage
import pages.sections.HouseConsignmentSection
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HouseConsignmentsTransformer @Inject() (
  consigneeTransformer: ConsigneeTransformer,
  consignorTransformer: ConsignorTransformer,
  departureTransportMeansTransformer: DepartureTransportMeansTransformer,
  documentsTransformer: DocumentsTransformer,
  additionalReferencesTransformer: AdditionalReferencesTransformer,
  additionalInformationTransformer: AdditionalInformationTransformer,
  consignmentItemTransformer: ConsignmentItemTransformer,
  referenceDataConnector: ReferenceDataConnector
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(houseConsignments: Seq[HouseConsignmentType04])(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    houseConsignments.zipWithIndex
      .foldLeft(Future.successful(userAnswers))({
        case (acc, (houseConsignment, i)) =>
          acc.flatMap {
            userAnswers =>
              val hcIndex = Index(i)
              val pipeline =
                setSequenceNumber(HouseConsignmentSection(hcIndex), houseConsignment.sequenceNumber) andThen
                  consigneeTransformer.transform(houseConsignment.Consignee, hcIndex) andThen
                  consignorTransformer.transform(houseConsignment.Consignor, hcIndex) andThen
                  departureTransportMeansTransformer.transform(houseConsignment.DepartureTransportMeans, hcIndex) andThen
                  documentsTransformer.transform(
                    houseConsignment.SupportingDocument,
                    houseConsignment.TransportDocument,
                    houseConsignment.PreviousDocument.toPreviousDocumentType06,
                    hcIndex
                  ) andThen
                  additionalReferencesTransformer.transform(houseConsignment.AdditionalReference, hcIndex) andThen
                  additionalInformationTransformer.transform(houseConsignment.AdditionalInformation, hcIndex) andThen
                  consignmentItemTransformer.transform(houseConsignment.ConsignmentItem, hcIndex) andThen
                  transformSecurityIndicatorFromExportDeclaration(houseConsignment.securityIndicatorFromExportDeclaration, hcIndex)
              pipeline(userAnswers)
          }
      })

  private def transformSecurityIndicatorFromExportDeclaration(securityIndicatorFromExportDeclaration: Option[String], hcIndex: Index)(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] = userAnswers =>
    securityIndicatorFromExportDeclaration match {
      case Some(securityIndicator) =>
        referenceDataConnector.getSecurityType(securityIndicator).flatMap {
          indicator =>
            val pipeline: UserAnswers => Future[UserAnswers] =
              set(SecurityIndicatorFromExportDeclarationPage(hcIndex), indicator)
            pipeline(userAnswers)
        }

      case None => Future.successful(userAnswers)
    }
}
