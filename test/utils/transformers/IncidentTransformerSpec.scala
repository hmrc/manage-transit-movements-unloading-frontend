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
import models.reference.Incident
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.incident.{IncidentCodePage, IncidentTextPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import org.mockito.ArgumentMatchers.{any, eq => eqTo}

import scala.concurrent.Future

class IncidentTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[IncidentTransformer]

  private lazy val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ReferenceDataConnector].toInstance(mockRefDataConnector)
      )

  "must transform data" - {
    "when incidents defined" in {

      val incidents: Seq[generated.IncidentType04] = arbitrary[Seq[generated.IncidentType04]].sample.value

      incidents.map {
        type0 =>
          when(mockRefDataConnector.getIncidentType(eqTo(type0.code))(any(), any()))
            .thenReturn(
              Future.successful(Incident(type0.code, "describe me"))
            )
      }

      val result = transformer.transform(incidents).apply(emptyUserAnswers).futureValue

      incidents.zipWithIndex.map {
        case (incident, i) =>
          result.getValue(IncidentCodePage(Index(i))).code mustBe incident.code
          result.getValue(IncidentCodePage(Index(i))).description mustBe "describe me"
          result.getValue(IncidentTextPage(Index(i))) mustBe incident.text

      }

    }

  }
}
