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
import generated.TranshipmentType
import generators.Generators
import models.reference.{Country, TransportMeansIdentification}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.incident.replacementMeansOfTransport.{IdentificationPage, NationalityPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService

import scala.concurrent.Future

class ReplacementMeansOfTransportTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[ReplacementMeansOfTransportTransformer]

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

  "must transform data" - {
    "when replacementMeansOfTransport defined" in {
      forAll(arbitrary[TranshipmentType], Gen.alphaNumStr) {
        (transhipment, description) =>
          beforeEach()

          val country = Country(transhipment.TransportMeans.nationality, description)

          val identification = TransportMeansIdentification(transhipment.TransportMeans.typeOfIdentification, description)

          when(mockReferenceDataService.getCountry(any())(any()))
            .thenReturn(Future.successful(country))

          when(mockReferenceDataService.getMeansOfTransportIdentificationType(any())(any()))
            .thenReturn(Future.successful(identification))

          val result = transformer.transform(Some(transhipment), index).apply(emptyUserAnswers).futureValue

          result.getValue(NationalityPage(index)) mustEqual country
          result.getValue(IdentificationPage(index)) mustEqual identification

          verify(mockReferenceDataService).getCountry(eqTo(transhipment.TransportMeans.nationality))(any())
          verify(mockReferenceDataService).getMeansOfTransportIdentificationType(eqTo(transhipment.TransportMeans.typeOfIdentification))(any())
      }
    }

    "when endorsement undefined" in {
      val result = transformer.transform(None, index).apply(emptyUserAnswers).futureValue

      result.get(NationalityPage(index)) must not be defined
      result.get(IdentificationPage(index)) must not be defined

      verifyNoInteractions(mockReferenceDataService)
    }
  }
}
