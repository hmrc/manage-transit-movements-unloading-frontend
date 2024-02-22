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
import generated.TransitOperationType14
import generators.Generators
import models.{DeclarationType, SecurityType}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{DeclarationTypePage, SecurityTypePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future

class TransitOperationTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer: TransitOperationTransformer = app.injector.instanceOf[TransitOperationTransformer]

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

    val transitOperationType14: TransitOperationType14 = arbitrary[TransitOperationType14].sample.value

    transitOperationType14.declarationType.map(
      dec =>
        when(mockRefDataConnector.getDeclarationType(eqTo(dec))(any(), any()))
          .thenReturn(Future.successful(DeclarationType(dec, "test1")))
    )

    when(mockRefDataConnector.getSecurityType(eqTo(transitOperationType14.security))(any(), any()))
      .thenReturn(Future.successful(SecurityType(transitOperationType14.security, "test2")))

    val result = transformer.transform(transitOperationType14).apply(emptyUserAnswers).futureValue
    val desc = transitOperationType14.declarationType match {
      case Some(_) => Some("test1")
      case None    => None
    }
    result.get(DeclarationTypePage).map(_.code) mustBe transitOperationType14.declarationType
    result.get(DeclarationTypePage).map(_.description) mustBe desc
    result.getValue(SecurityTypePage).code mustBe transitOperationType14.security
    result.getValue(SecurityTypePage).description mustBe "test2"

  }

}