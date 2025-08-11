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
import generated.HouseConsignmentType04
import generators.Generators
import models.Index
import models.reference.{Country, SecurityType}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.{CountryOfDestinationPage, GrossWeightPage, SecurityIndicatorFromExportDeclarationPage, UniqueConsignmentReferencePage}
import pages.sections.houseConsignment.index.*
import pages.sections.houseConsignment.index.additionalInformation.AdditionalInformationListSection
import pages.sections.houseConsignment.index.additionalReference.AdditionalReferenceListSection
import pages.sections.houseConsignment.index.departureTransportMeans.TransportMeansListSection
import pages.sections.houseConsignment.index.documents.DocumentsSection
import pages.sections.{HouseConsignmentSection, ItemsSection}
import play.api.libs.json.{JsArray, Json}
import services.ReferenceDataService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HouseConsignmentsTransformerSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private lazy val mockConsigneeTransformer               = mock[ConsigneeTransformer]
  private lazy val mockConsignorTransformer               = mock[ConsignorTransformer]
  private lazy val mockDepartureTransportMeansTransformer = mock[DepartureTransportMeansTransformer]
  private lazy val mockDocumentsTransformer               = mock[DocumentsTransformer]
  private lazy val mockAdditionalReferenceTransformer     = mock[AdditionalReferencesTransformer]
  private lazy val mockAdditionalInformationTransformer   = mock[AdditionalInformationTransformer]
  private lazy val mockConsignmentItemTransformer         = mock[ConsignmentItemTransformer]

  private lazy val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  private val transformer = new HouseConsignmentsTransformer(
    mockConsigneeTransformer,
    mockConsignorTransformer,
    mockDepartureTransportMeansTransformer,
    mockDocumentsTransformer,
    mockAdditionalReferenceTransformer,
    mockAdditionalInformationTransformer,
    mockConsignmentItemTransformer,
    mockReferenceDataService
  )

  "must transform data" in {
    val country = Country("GB", "country")
    forAll(arbitrary[Seq[HouseConsignmentType04]]) {
      houseConsignments =>
        houseConsignments.zipWithIndex.map {
          case (_, i) =>
            val hcIndex = Index(i)

            when(mockConsigneeTransformer.transform(any(), eqTo(hcIndex))(any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(ConsigneeSection(hcIndex), Json.obj("foo" -> i.toString)))
              }

            when(mockConsignorTransformer.transform(any(), eqTo(hcIndex))(any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(ConsignorSection(hcIndex), Json.obj("foo" -> i.toString)))
              }

            when(mockDepartureTransportMeansTransformer.transform(any(), eqTo(hcIndex))(any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(TransportMeansListSection(hcIndex), JsArray(Seq(Json.obj("foo" -> i.toString)))))
              }

            when(mockDocumentsTransformer.transform(any(), any(), any(), eqTo(hcIndex))(any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(DocumentsSection(hcIndex), JsArray(Seq(Json.obj("foo" -> i.toString)))))
              }

            when(mockAdditionalReferenceTransformer.transform(any(), eqTo(hcIndex))(any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(AdditionalReferenceListSection(hcIndex), JsArray(Seq(Json.obj("foo" -> i.toString)))))
              }

            when(mockAdditionalInformationTransformer.transform(any(), eqTo(hcIndex))(any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(AdditionalInformationListSection(hcIndex), JsArray(Seq(Json.obj("foo" -> i.toString)))))
              }

            when(mockConsignmentItemTransformer.transform(any(), eqTo(hcIndex))(any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(ItemsSection(hcIndex), JsArray(Seq(Json.obj("foo" -> i.toString)))))
              }

            when(mockReferenceDataService.getSecurityType(any())(any()))
              .thenReturn {
                Future.successful(SecurityType("code", "description"))
              }

            when(mockReferenceDataService.getCountry(any())(any()))
              .thenReturn(Future.successful(country))
        }

        val result = transformer.transform(houseConsignments).apply(emptyUserAnswers).futureValue

        houseConsignments.zipWithIndex.map {
          case (hc, i) =>
            val hcIndex = Index(i)

            result.getSequenceNumber(HouseConsignmentSection(hcIndex)) mustEqual hc.sequenceNumber
            result.getValue(GrossWeightPage(hcIndex)) mustEqual hc.grossMass
            result.get(UniqueConsignmentReferencePage(hcIndex)) mustEqual hc.referenceNumberUCR
            result.getValue(SecurityIndicatorFromExportDeclarationPage(hcIndex)) mustEqual SecurityType("code", "description")
            result.getValue(ConsigneeSection(hcIndex)) mustEqual Json.obj("foo" -> i.toString)
            result.getValue(ConsignorSection(hcIndex)) mustEqual Json.obj("foo" -> i.toString)
            result.getValue(TransportMeansListSection(hcIndex)) mustEqual JsArray(Seq(Json.obj("foo" -> i.toString)))
            result.getValue(DocumentsSection(hcIndex)) mustEqual JsArray(Seq(Json.obj("foo" -> i.toString)))
            result.getValue(AdditionalReferenceListSection(hcIndex)) mustEqual JsArray(Seq(Json.obj("foo" -> i.toString)))
            result.getValue(AdditionalInformationListSection(hcIndex)) mustEqual JsArray(Seq(Json.obj("foo" -> i.toString)))
            result.getValue(ItemsSection(hcIndex)) mustEqual JsArray(Seq(Json.obj("foo" -> i.toString)))
            result.getValue(CountryOfDestinationPage(hcIndex)) mustEqual country
        }
    }
  }
}
