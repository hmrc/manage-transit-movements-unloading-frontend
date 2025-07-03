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

import generated.HouseConsignmentType04
import models.{Index, UserAnswers}
import pages.houseConsignment.index.{CountryOfDestinationPage, GrossWeightPage, SecurityIndicatorFromExportDeclarationPage, UniqueConsignmentReferencePage}
import pages.sections.HouseConsignmentSection
import services.ReferenceDataService
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
  referenceDataService: ReferenceDataService
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(houseConsignments: Seq[HouseConsignmentType04])(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    userAnswers =>
      houseConsignments.zipWithIndex
        .foldLeft(Future.successful(userAnswers)) {
          case (acc, (houseConsignment, i)) =>
            acc.flatMap {
              userAnswers =>
                val hcIndex = Index(i)
                val pipeline =
                  setSequenceNumber(HouseConsignmentSection(hcIndex), houseConsignment.sequenceNumber) andThen
                    set(GrossWeightPage(hcIndex), houseConsignment.grossMass) andThen
                    set(UniqueConsignmentReferencePage(hcIndex), houseConsignment.referenceNumberUCR) andThen
                    consigneeTransformer.transform(houseConsignment.Consignee, hcIndex) andThen
                    consignorTransformer.transform(houseConsignment.Consignor, hcIndex) andThen
                    departureTransportMeansTransformer.transform(houseConsignment.DepartureTransportMeans, hcIndex) andThen
                    documentsTransformer.transform(
                      houseConsignment.SupportingDocument,
                      houseConsignment.TransportDocument,
                      houseConsignment.PreviousDocument,
                      hcIndex
                    ) andThen
                    additionalReferencesTransformer.transform(houseConsignment.AdditionalReference, hcIndex) andThen
                    additionalInformationTransformer.transform(houseConsignment.AdditionalInformation, hcIndex) andThen
                    consignmentItemTransformer.transform(houseConsignment.ConsignmentItem, hcIndex) andThen
                    transformSecurityIndicatorFromExportDeclaration(houseConsignment.securityIndicatorFromExportDeclaration, hcIndex) andThen
                    transformCountryOfDestination(houseConsignment.countryOfDestination, hcIndex)
                pipeline(userAnswers)
            }
        }

  private def transformSecurityIndicatorFromExportDeclaration(securityIndicatorFromExportDeclaration: Option[String], hcIndex: Index)(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] = userAnswers =>
    securityIndicatorFromExportDeclaration match {
      case Some(securityIndicator) =>
        referenceDataService.getSecurityType(securityIndicator).flatMap {
          indicator =>
            val pipeline: UserAnswers => Future[UserAnswers] =
              set(SecurityIndicatorFromExportDeclarationPage(hcIndex), indicator)
            pipeline(userAnswers)
        }

      case None => Future.successful(userAnswers)
    }

  private def transformCountryOfDestination(countryOfDestination: Option[String], hcIndex: Index)(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] = userAnswers =>
    countryOfDestination match {

      case Some(country) =>
        referenceDataService.getCountry(country).flatMap {
          countryVal =>
            val pipeline: UserAnswers => Future[UserAnswers] =
              set(CountryOfDestinationPage(hcIndex), countryVal)
            pipeline(userAnswers)
        }

      case None =>
        Future.successful(userAnswers)
    }
}
