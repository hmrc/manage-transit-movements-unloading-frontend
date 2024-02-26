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
import generated.EndorsementType03
import generators.Generators
import models.reference.Country
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.incident.endorsement._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future

class IncidentEndorsementTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[IncidentEndorsementTransformer]

  private lazy val mockReferenceDataConnector = mock[ReferenceDataConnector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ReferenceDataConnector].toInstance(mockReferenceDataConnector)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataConnector)
  }

  "must transform data" - {
    "when endorsement defined" in {
      forAll(arbitrary[EndorsementType03], Gen.alphaNumStr) {
        (endorsement, countryDescription) =>
          beforeEach()

          val country = Country(endorsement.country, countryDescription)

          when(mockReferenceDataConnector.getCountry(any())(any(), any()))
            .thenReturn(Future.successful(country))

          val result = transformer.transform(Some(endorsement), index).apply(emptyUserAnswers).futureValue

          result.getValue(EndorsementCountryPage(index)) mustBe country

          verify(mockReferenceDataConnector).getCountry(eqTo(endorsement.country))(any(), any())
      }
    }

    "when endorsement undefined" in {
      val result = transformer.transform(None, index).apply(emptyUserAnswers).futureValue

      result.get(EndorsementCountryPage(index)) must not be defined

      verifyNoInteractions(mockReferenceDataConnector)
    }
  }
}
