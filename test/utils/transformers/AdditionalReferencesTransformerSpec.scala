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
import generated.{AdditionalReferenceType01, AdditionalReferenceType02}
import generators.Generators
import models.Index
import models.reference.AdditionalReferenceType
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalReference.AdditionalReferenceTypePage
import pages.houseConsignment.index.items.additionalReference.{AdditionalReferenceInCL234Page, AdditionalReferenceTypePage as AdditionalReferenceTypeItemPage}
import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferenceSection
import services.ReferenceDataService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AdditionalReferencesTransformerSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private lazy val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  private val transformer: AdditionalReferencesTransformer = new AdditionalReferencesTransformer(mockReferenceDataService)

  "must transform data" in {

    val additionalReferences: Seq[AdditionalReferenceType02] = arbitrary[Seq[AdditionalReferenceType02]].sample.value

    additionalReferences.map {
      additionalReference =>
        when(mockReferenceDataService.getAdditionalReference(eqTo(additionalReference.typeValue))(any()))
          .thenReturn(
            Future.successful(AdditionalReferenceType(documentType = additionalReference.typeValue, description = "describe me"))
          )
    }

    val result = transformer.transform(additionalReferences).apply(emptyUserAnswers).futureValue

    additionalReferences.zipWithIndex.map {
      case (refType, i) =>
        result.getValue(AdditionalReferenceTypePage(Index(i))).documentType mustEqual refType.typeValue
        result.getValue(AdditionalReferenceTypePage(Index(i))).description mustEqual "describe me"

    }
  }

  "must transform data at HC level" in {
    import pages.houseConsignment.index.additionalReference.*
    import pages.sections.houseConsignment.index.additionalReference.AdditionalReferenceSection

    val additionalReferences: Seq[AdditionalReferenceType02] = arbitrary[Seq[AdditionalReferenceType02]].sample.value

    additionalReferences.map {
      additionalReference =>
        when(mockReferenceDataService.getAdditionalReference(eqTo(additionalReference.typeValue))(any()))
          .thenReturn(
            Future.successful(AdditionalReferenceType(documentType = additionalReference.typeValue, description = "describe me"))
          )
    }
    val result = transformer.transform(additionalReferences, hcIndex).apply(emptyUserAnswers).futureValue

    additionalReferences.zipWithIndex.map {
      case (refType, i) =>
        result.getSequenceNumber(AdditionalReferenceSection(hcIndex, Index(i))) mustEqual refType.sequenceNumber
        result.getValue(HouseConsignmentAdditionalReferenceTypePage(hcIndex, Index(i))).documentType mustEqual refType.typeValue
    }
  }

  "must transform data at Item level" - {

    "when is docTypeExcise" in {
      val additionalReferences: Seq[AdditionalReferenceType01] = arbitrary[Seq[AdditionalReferenceType01]].sample.value

      additionalReferences.map {
        additionalReference =>
          when(mockReferenceDataService.getAdditionalReference(eqTo(additionalReference.typeValue))(any()))
            .thenReturn(
              Future.successful(AdditionalReferenceType(documentType = additionalReference.typeValue, description = "describe me"))
            )
      }

      when(mockReferenceDataService.isDocumentTypeExcise(any())(any())).thenReturn(Future.successful(true))

      val result = transformer.transform(additionalReferences, hcIndex, itemIndex).apply(emptyUserAnswers).futureValue

      additionalReferences.zipWithIndex.map {
        case (refType, i) =>
          result.getSequenceNumber(AdditionalReferenceSection(hcIndex, itemIndex, Index(i))) mustEqual refType.sequenceNumber
          result.getValue(AdditionalReferenceTypeItemPage(hcIndex, itemIndex, Index(i))).documentType mustEqual refType.typeValue
          result.getValue(AdditionalReferenceInCL234Page(hcIndex, itemIndex, Index(i))) mustEqual true
      }
    }

    "when is not docTypeExcise" in {
      val additionalReferences: Seq[AdditionalReferenceType01] = arbitrary[Seq[AdditionalReferenceType01]].sample.value

      additionalReferences.map {
        additionalReference =>
          when(mockReferenceDataService.getAdditionalReference(eqTo(additionalReference.typeValue))(any()))
            .thenReturn(
              Future.successful(AdditionalReferenceType(documentType = additionalReference.typeValue, description = "describe me"))
            )
      }

      when(mockReferenceDataService.isDocumentTypeExcise(any())(any())).thenReturn(Future.successful(false))

      val result = transformer.transform(additionalReferences, hcIndex, itemIndex).apply(emptyUserAnswers).futureValue

      additionalReferences.zipWithIndex.map {
        case (refType, i) =>
          result.getSequenceNumber(AdditionalReferenceSection(hcIndex, itemIndex, Index(i))) mustEqual refType.sequenceNumber
          result.getValue(AdditionalReferenceTypeItemPage(hcIndex, itemIndex, Index(i))).documentType mustEqual refType.typeValue
          result.getValue(AdditionalReferenceInCL234Page(hcIndex, itemIndex, Index(i))) mustEqual false
      }
    }

  }
}
