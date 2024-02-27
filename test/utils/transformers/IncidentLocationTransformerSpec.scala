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
import generated.LocationType02
import generators.Generators
import models.reference.{Country, QualifierOfIdentification}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.QuestionPage
import pages.incident.location._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsPath, Json}

import scala.concurrent.Future

class IncidentLocationTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[IncidentLocationTransformer]

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

  private case object FakeIncidentLocationAddressSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "incidentAddressLocation"
  }

  "must transform data" in {
    forAll(arbitrary[LocationType02], Gen.alphaNumStr, Gen.alphaNumStr) {
      (location, qualifierDescription, countryDescription) =>
        beforeEach()

        val qualifierOfIdentification = QualifierOfIdentification(location.qualifierOfIdentification, qualifierDescription)
        val country                   = Country(location.country, countryDescription)

        when(mockReferenceDataConnector.getQualifierOfIdentificationIncident(any())(any(), any()))
          .thenReturn(Future.successful(qualifierOfIdentification))

        when(mockReferenceDataConnector.getCountry(any())(any(), any()))
          .thenReturn(Future.successful(country))

        val result = transformer.transform(location, index).apply(emptyUserAnswers).futureValue

        result.getValue(QualifierOfIdentificationPage(index)) mustBe qualifierOfIdentification
        result.get(UNLocodePage(index)) mustBe location.UNLocode
        result.getValue(CountryPage(index)) mustBe country
        result.getValue(FakeIncidentLocationAddressSection) mustBe Json.obj("foo" -> index.toString)

        verify(mockReferenceDataConnector).getQualifierOfIdentificationIncident(eqTo(location.qualifierOfIdentification))(any(), any())
        verify(mockReferenceDataConnector).getCountry(eqTo(location.country))(any(), any())
    }
  }
}
