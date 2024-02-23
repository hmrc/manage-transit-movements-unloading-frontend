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
import models.reference.Country
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.incident.EndorsementCountryPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future

class EndorsementTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[EndorsementTransformer]

  private lazy val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ReferenceDataConnector].toInstance(mockRefDataConnector)
      )

  "must transform data" - {
    "when endorsements defined" in {

      val endorsement: Option[generated.EndorsementType03] = arbitrary[Option[generated.EndorsementType03]].sample.value

      endorsement.map {
        type0 =>
          when(mockRefDataConnector.getCountry(eqTo(type0.country))(any(), any()))
            .thenReturn(
              Future.successful(Country(type0.country, "country near the sea"))
            )
      }

      val result = transformer.transform(endorsement, index).apply(emptyUserAnswers).futureValue

      result.get(EndorsementCountryPage(index)).map(_.code) mustBe endorsement.map(_.country)
      result.get(EndorsementCountryPage(index)).map(_.description) mustBe endorsement.map(
        _ => "country near the sea"
      )

    }

  }
}
