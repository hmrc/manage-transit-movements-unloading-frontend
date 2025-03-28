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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.{AdditionalReferenceType02, AdditionalReferenceType03}
import generators.Generators
import models.Index
import models.reference.AdditionalReferenceType
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalReference.AdditionalReferenceTypePage
import pages.houseConsignment.index.items.additionalReference.{AdditionalReferenceInCL234Page, AdditionalReferenceTypePage as AdditionalReferenceTypeItemPage}
import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferenceSection
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService

import scala.concurrent.Future

class AdditionalReferencesTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer: AdditionalReferencesTransformer = app.injector.instanceOf[AdditionalReferencesTransformer]

  private lazy val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ReferenceDataService].toInstance(mockReferenceDataService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
  }

  "must transform data" in {

    val additionalReferenceType03: Seq[AdditionalReferenceType03] = arbitrary[Seq[AdditionalReferenceType03]].sample.value

    additionalReferenceType03.map {
      type0 =>
        when(mockReferenceDataService.getAdditionalReference(eqTo(type0.typeValue))(any()))
          .thenReturn(
            Future.successful(AdditionalReferenceType(documentType = type0.typeValue, description = "describe me"))
          )
    }

    val result = transformer.transform(additionalReferenceType03).apply(emptyUserAnswers).futureValue

    additionalReferenceType03.zipWithIndex.map {
      case (refType, i) =>
        result.getValue(AdditionalReferenceTypePage(Index(i))).documentType mustBe refType.typeValue
        result.getValue(AdditionalReferenceTypePage(Index(i))).description mustBe "describe me"

    }
  }

  "must transform data at HC level" in {
    import pages.houseConsignment.index.additionalReference.*
    import pages.sections.houseConsignment.index.additionalReference.AdditionalReferenceSection

    val additionalReferenceType03: Seq[AdditionalReferenceType03] = arbitrary[Seq[AdditionalReferenceType03]].sample.value

    additionalReferenceType03.map {
      type0 =>
        when(mockReferenceDataService.getAdditionalReference(eqTo(type0.typeValue))(any()))
          .thenReturn(
            Future.successful(AdditionalReferenceType(documentType = type0.typeValue, description = "describe me"))
          )
    }
    val result = transformer.transform(additionalReferenceType03, hcIndex).apply(emptyUserAnswers).futureValue

    additionalReferenceType03.zipWithIndex.map {
      case (refType, i) =>
        result.getSequenceNumber(AdditionalReferenceSection(hcIndex, Index(i))) mustBe refType.sequenceNumber
        result.getValue(HouseConsignmentAdditionalReferenceTypePage(hcIndex, Index(i))).documentType mustBe refType.typeValue
    }
  }

  "must transform data at Item level" - {

    "when is docTypeExcise" in {
      val additionalReferenceType02: Seq[AdditionalReferenceType02] = arbitrary[Seq[AdditionalReferenceType02]].sample.value

      additionalReferenceType02.map {
        type0 =>
          when(mockReferenceDataService.getAdditionalReference(eqTo(type0.typeValue))(any()))
            .thenReturn(
              Future.successful(AdditionalReferenceType(documentType = type0.typeValue, description = "describe me"))
            )
      }

      when(mockReferenceDataService.isDocumentTypeExcise(any())(any())).thenReturn(Future.successful(true))

      val result = transformer.transform(additionalReferenceType02, hcIndex, itemIndex).apply(emptyUserAnswers).futureValue

      additionalReferenceType02.zipWithIndex.map {
        case (refType, i) =>
          result.getSequenceNumber(AdditionalReferenceSection(hcIndex, itemIndex, Index(i))) mustBe refType.sequenceNumber
          result.getValue(AdditionalReferenceTypeItemPage(hcIndex, itemIndex, Index(i))).documentType mustBe refType.typeValue
          result.getValue(AdditionalReferenceInCL234Page(hcIndex, itemIndex, Index(i))) mustBe true
      }
    }

    "when is not docTypeExcise" in {
      val additionalReferenceType02: Seq[AdditionalReferenceType02] = arbitrary[Seq[AdditionalReferenceType02]].sample.value

      additionalReferenceType02.map {
        type0 =>
          when(mockReferenceDataService.getAdditionalReference(eqTo(type0.typeValue))(any()))
            .thenReturn(
              Future.successful(AdditionalReferenceType(documentType = type0.typeValue, description = "describe me"))
            )
      }

      when(mockReferenceDataService.isDocumentTypeExcise(any())(any())).thenReturn(Future.successful(false))

      val result = transformer.transform(additionalReferenceType02, hcIndex, itemIndex).apply(emptyUserAnswers).futureValue

      additionalReferenceType02.zipWithIndex.map {
        case (refType, i) =>
          result.getSequenceNumber(AdditionalReferenceSection(hcIndex, itemIndex, Index(i))) mustBe refType.sequenceNumber
          result.getValue(AdditionalReferenceTypeItemPage(hcIndex, itemIndex, Index(i))).documentType mustBe refType.typeValue
          result.getValue(AdditionalReferenceInCL234Page(hcIndex, itemIndex, Index(i))) mustBe false
      }
    }

  }
}
