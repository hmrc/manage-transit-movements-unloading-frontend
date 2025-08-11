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

import base.SpecBase
import generated.ConsignmentItemType04
import generators.Generators
import models.Index
import models.reference.Country
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.{CountryOfDestinationPage, DeclarationGoodsItemNumberPage, DeclarationTypePage, UniqueConsignmentReferencePage}
import pages.sections.houseConsignment.index.items.CommoditySection
import pages.sections.houseConsignment.index.items.additionalInformation.AdditionalInformationsSection
import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferencesSection
import pages.sections.houseConsignment.index.items.consignee.ConsigneeSection
import pages.sections.houseConsignment.index.items.documents.DocumentsSection
import pages.sections.{ItemSection, PackagingListSection}
import play.api.libs.json.{JsArray, Json}
import services.ReferenceDataService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConsignmentItemTransformerSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private lazy val mockCommodityTransformer             = mock[CommodityTransformer]
  private lazy val mockPackagingTransformer             = mock[PackagingTransformer]
  private lazy val mockDocumentsTransformer             = mock[DocumentsTransformer]
  private lazy val mockAdditionalReferencesTransformer  = mock[AdditionalReferencesTransformer]
  private lazy val mockConsigneeTransformer             = mock[ConsigneeTransformer]
  private lazy val mockAdditionalInformationTransformer = mock[AdditionalInformationTransformer]

  private lazy val mockReferenceDataService = mock[ReferenceDataService]

  private val transformer = new ConsignmentItemTransformer(
    mockCommodityTransformer,
    mockPackagingTransformer,
    mockDocumentsTransformer,
    mockAdditionalReferencesTransformer,
    mockConsigneeTransformer,
    mockAdditionalInformationTransformer,
    mockReferenceDataService
  )

  "must transform data" in {
    forAll(arbitrary[Seq[ConsignmentItemType04]]) {
      consignmentItems =>
        consignmentItems.zipWithIndex.map {
          case (consignmentItem, i) =>
            val itemIndex = Index(i)

            when(mockCommodityTransformer.transform(any(), any(), eqTo(itemIndex)))
              .thenReturn {
                ua => Future.successful(ua.setValue(CommoditySection(hcIndex, itemIndex), Json.obj("foo" -> i.toString)))
              }

            when(mockPackagingTransformer.transform(any(), any(), eqTo(itemIndex))(any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(PackagingListSection(hcIndex, itemIndex), JsArray(Seq(Json.obj("foo" -> i.toString)))))
              }

            when(mockDocumentsTransformer.transform(any(), any(), any(), any(), eqTo(itemIndex))(any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(DocumentsSection(hcIndex, itemIndex), JsArray(Seq(Json.obj("foo" -> i.toString)))))
              }

            when(mockAdditionalReferencesTransformer.transform(any(), any(), eqTo(itemIndex))(any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(AdditionalReferencesSection(hcIndex, itemIndex), JsArray(Seq(Json.obj("foo" -> i.toString)))))
              }

            when(mockConsigneeTransformer.transform(any(), any(), eqTo(itemIndex))(any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(ConsigneeSection(hcIndex, itemIndex), Json.obj("foo" -> i.toString)))
              }

            when(mockAdditionalInformationTransformer.transform(any(), any(), eqTo(itemIndex))(any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(AdditionalInformationsSection(hcIndex, itemIndex), JsArray(Seq(Json.obj("foo" -> i.toString)))))
              }

            when(mockReferenceDataService.getCountry(eqTo(consignmentItem.countryOfDestination.value))(any()))
              .thenReturn(Future.successful(Country("foo", i.toString)))
        }

        val result = transformer.transform(consignmentItems, hcIndex).apply(emptyUserAnswers).futureValue

        consignmentItems.zipWithIndex.map {
          case (consignmentItem, i) =>
            val itemIndex = Index(i)

            result.getSequenceNumber(ItemSection(hcIndex, itemIndex)) mustEqual consignmentItem.goodsItemNumber
            result.getValue(DeclarationGoodsItemNumberPage(hcIndex, itemIndex)) mustEqual consignmentItem.declarationGoodsItemNumber
            result.get(DeclarationTypePage(hcIndex, itemIndex)) mustEqual consignmentItem.declarationType
            result.getValue(CountryOfDestinationPage(hcIndex, itemIndex)) mustEqual Country("foo", i.toString)
            result.get(UniqueConsignmentReferencePage(hcIndex, itemIndex)) mustEqual consignmentItem.referenceNumberUCR
            result.getValue(CommoditySection(hcIndex, itemIndex)) mustEqual Json.obj("foo" -> i.toString)
            result.getValue(ConsigneeSection(hcIndex, itemIndex)) mustEqual Json.obj("foo" -> i.toString)
            result.getValue(PackagingListSection(hcIndex, itemIndex)) mustEqual JsArray(Seq(Json.obj("foo" -> i.toString)))
            result.getValue(DocumentsSection(hcIndex, itemIndex)) mustEqual JsArray(Seq(Json.obj("foo" -> i.toString)))
            result.getValue(AdditionalReferencesSection(hcIndex, itemIndex)) mustEqual JsArray(Seq(Json.obj("foo" -> i.toString)))
            result.getValue(AdditionalInformationsSection(hcIndex, itemIndex)) mustEqual JsArray(Seq(Json.obj("foo" -> i.toString)))
        }
    }
  }
}
