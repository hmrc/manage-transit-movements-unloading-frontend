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

import generated.ConsignmentItemType04
import models.*
import pages.houseConsignment.index.items.*
import pages.sections.ItemSection
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConsignmentItemTransformer @Inject() (
  commodityTransformer: CommodityTransformer,
  packagingTransformer: PackagingTransformer,
  documentsTransformer: DocumentsTransformer,
  additionalReferencesTransformer: AdditionalReferencesTransformer,
  consigneeTransformer: ConsigneeTransformer,
  additionalInformationTransformer: AdditionalInformationTransformer,
  referenceDataService: ReferenceDataService
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(
    consignmentItems: Seq[ConsignmentItemType04],
    hcIndex: Index
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    consignmentItems.mapWithSets {
      (consignmentItem, itemIndex) =>
        setSequenceNumber(ItemSection(hcIndex, itemIndex), consignmentItem.goodsItemNumber) andThen
          set(DeclarationGoodsItemNumberPage(hcIndex, itemIndex), consignmentItem.declarationGoodsItemNumber) andThen
          set(DeclarationTypePage(hcIndex, itemIndex), consignmentItem.declarationType) andThen
          set(UniqueConsignmentReferencePage(hcIndex, itemIndex), consignmentItem.referenceNumberUCR) andThen
          set(CountryOfDestinationPage(hcIndex, itemIndex), consignmentItem.countryOfDestination, referenceDataService.getCountry) andThen
          commodityTransformer.transform(consignmentItem.Commodity, hcIndex, itemIndex) andThen
          packagingTransformer.transform(consignmentItem.Packaging, hcIndex, itemIndex) andThen
          documentsTransformer.transform(
            consignmentItem.SupportingDocument,
            consignmentItem.TransportDocument,
            consignmentItem.PreviousDocument,
            hcIndex,
            itemIndex
          ) andThen
          additionalReferencesTransformer.transform(consignmentItem.AdditionalReference, hcIndex, itemIndex) andThen
          consigneeTransformer.transform(consignmentItem.Consignee, hcIndex, itemIndex) andThen
          additionalInformationTransformer.transform(consignmentItem.AdditionalInformation, hcIndex, itemIndex)
    }
}
