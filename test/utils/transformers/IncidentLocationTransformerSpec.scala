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
import generated.LocationType
import generators.Generators
import models.reference.{Country, QualifierOfIdentification}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.incident.location.*
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService

import scala.concurrent.Future

class IncidentLocationTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[IncidentLocationTransformer]

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
    forAll(arbitrary[LocationType], Gen.alphaNumStr, Gen.alphaNumStr) {
      (location, qualifierDescription, countryDescription) =>
        beforeEach()

        val qualifierOfIdentification = QualifierOfIdentification(location.qualifierOfIdentification, qualifierDescription)
        val country                   = Country(location.country, countryDescription)

        when(mockReferenceDataService.getQualifierOfIdentificationIncident(any())(any()))
          .thenReturn(Future.successful(qualifierOfIdentification))

        when(mockReferenceDataService.getCountry(any())(any()))
          .thenReturn(Future.successful(country))

        val result = transformer.transform(location, index).apply(emptyUserAnswers).futureValue

        result.getValue(QualifierOfIdentificationPage(index)) mustBe qualifierOfIdentification
        result.get(UNLocodePage(index)) mustBe location.UNLocode
        result.getValue(CountryPage(index)) mustBe country

        verify(mockReferenceDataService).getQualifierOfIdentificationIncident(eqTo(location.qualifierOfIdentification))(any())
        verify(mockReferenceDataService).getCountry(eqTo(location.country))(any())
    }
  }
}
