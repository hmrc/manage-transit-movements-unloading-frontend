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
import generated.AdditionalInformationType02
import generators.Generators
import models.Index
import models.reference.AdditionalInformationCode
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService

import scala.concurrent.Future

class AdditionalInformationTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer: AdditionalInformationTransformer = app.injector.instanceOf[AdditionalInformationTransformer]

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
    import pages.additionalInformation.*
    import pages.sections.additionalInformation.AdditionalInformationSection

    val additionalInformationType02: Seq[AdditionalInformationType02] = arbitrary[Seq[AdditionalInformationType02]].sample.value

    additionalInformationType02.map {
      code0 =>
        when(mockReferenceDataService.getAdditionalInformationCode(eqTo(code0.code))(any()))
          .thenReturn(
            Future.successful(AdditionalInformationCode(code = code0.code, description = "describe me"))
          )
    }

    val result = transformer.transform(additionalInformationType02).apply(emptyUserAnswers).futureValue

    additionalInformationType02.zipWithIndex.map {
      case (refType, i) =>
        result.getSequenceNumber(AdditionalInformationSection(Index(i))) mustEqual refType.sequenceNumber
        result.getValue(AdditionalInformationCodePage(Index(i))).code mustEqual refType.code
        result.getValue(AdditionalInformationCodePage(Index(i))).description mustEqual "describe me"
        result.get(AdditionalInformationTextPage(Index(i))) mustEqual refType.text
    }
  }

  "must transform data at HC level" in {
    import pages.houseConsignment.index.additionalinformation.*
    import pages.sections.houseConsignment.index.additionalInformation.AdditionalInformationSection

    val additionalInformationType02: Seq[AdditionalInformationType02] = arbitrary[Seq[AdditionalInformationType02]].sample.value

    additionalInformationType02.map {
      type0 =>
        when(mockReferenceDataService.getAdditionalInformationCode(eqTo(type0.code))(any()))
          .thenReturn(Future.successful(AdditionalInformationCode(code = type0.code, description = "describe me")))
    }

    val result = transformer.transform(additionalInformationType02, hcIndex).apply(emptyUserAnswers).futureValue

    additionalInformationType02.zipWithIndex.map {
      case (refType, i) =>
        result.getSequenceNumber(AdditionalInformationSection(hcIndex, Index(i))) mustEqual refType.sequenceNumber
        result.getValue(HouseConsignmentAdditionalInformationCodePage(hcIndex, Index(i))).value mustEqual refType.code
        result.getValue(HouseConsignmentAdditionalInformationCodePage(hcIndex, Index(i))).description mustEqual "describe me"
        result.get(HouseConsignmentAdditionalInformationTextPage(hcIndex, Index(i))) mustEqual refType.text
    }
  }

  "must transform data at Item level" in {
    import pages.houseConsignment.index.items.additionalinformation.*
    import pages.sections.houseConsignment.index.items.additionalInformation.AdditionalInformationSection

    val additionalInformationType02: Seq[AdditionalInformationType02] = arbitrary[Seq[AdditionalInformationType02]].sample.value

    additionalInformationType02.map {
      type0 =>
        when(mockReferenceDataService.getAdditionalInformationCode(eqTo(type0.code))(any()))
          .thenReturn(
            Future.successful(AdditionalInformationCode(code = type0.code, description = "describe me"))
          )
    }

    val result = transformer.transform(additionalInformationType02, hcIndex, itemIndex).apply(emptyUserAnswers).futureValue

    additionalInformationType02.zipWithIndex.map {
      case (refType, i) =>
        result.getSequenceNumber(AdditionalInformationSection(hcIndex, itemIndex, Index(i))) mustEqual refType.sequenceNumber
        result.getValue(HouseConsignmentItemAdditionalInformationCodePage(hcIndex, itemIndex, Index(i))).value mustEqual refType.code
        result.getValue(HouseConsignmentItemAdditionalInformationCodePage(hcIndex, itemIndex, Index(i))).description mustEqual "describe me"
        result.get(HouseConsignmentItemAdditionalInformationTextPage(hcIndex, itemIndex, Index(i))) mustEqual refType.text
    }
  }

}
