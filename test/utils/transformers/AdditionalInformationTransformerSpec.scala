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
import connectors.ReferenceDataConnector
import generators.Generators
import models.Index
import models.reference.{AdditionalInformationCode, AdditionalInformationType}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalInformation.AdditionalInformationCodePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import generated.AdditionalInformationType02
import pages.houseConsignment.index.items.additionalinformation.HouseConsignmentAdditionalInformationCodePage

import scala.concurrent.Future

class AdditionalInformationTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer: AdditionalInformationTransformer = app.injector.instanceOf[AdditionalInformationTransformer]

  private lazy val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ReferenceDataConnector].toInstance(mockRefDataConnector)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRefDataConnector)
  }

  "must transform data" in {

    val additionalInformationType02: Seq[AdditionalInformationType02] = arbitrary[Seq[AdditionalInformationType02]].sample.value

    additionalInformationType02.map {
      code0 =>
        when(mockRefDataConnector.getAdditionalInformationCode(eqTo(code0.code))(any(), any()))
          .thenReturn(
            Future.successful(AdditionalInformationCode(code = code0.code, description = "describe me"))
          )
    }

    val result = transformer.transform(additionalInformationType02).apply(emptyUserAnswers).futureValue

    additionalInformationType02.zipWithIndex.map {
      case (refType, i) =>
        result.getValue(AdditionalInformationCodePage(Index(i))).code mustBe refType.code
        result.getValue(AdditionalInformationCodePage(Index(i))).description mustBe "describe me"
    }

  }

  "must transform data at Item level" in {
    val additionalInformationType02: Seq[AdditionalInformationType02] = arbitrary[Seq[AdditionalInformationType02]].sample.value

    additionalInformationType02.map {
      type0 =>
        when(mockRefDataConnector.getAdditionalInformationType(eqTo(type0.code))(any(), any()))
          .thenReturn(
            Future.successful(AdditionalInformationType(code = type0.code, description = "describe me"))
          )
    }

    val result = transformer.transform(additionalInformationType02, hcIndex, itemIndex).apply(emptyUserAnswers).futureValue

    additionalInformationType02.zipWithIndex.map {
      case (refType, i) =>
        result.getValue(HouseConsignmentAdditionalInformationCodePage(hcIndex, itemIndex, Index(i))).value mustBe refType.code
        result.getValue(HouseConsignmentAdditionalInformationCodePage(hcIndex, itemIndex, Index(i))).description mustBe "describe me"
    }
  }

}
