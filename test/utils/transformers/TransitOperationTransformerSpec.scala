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
import generated.TransitOperationType10
import generators.Generators
import models.reference.SecurityType
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.SecurityTypePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService

import scala.concurrent.Future

class TransitOperationTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer: TransitOperationTransformer = app.injector.instanceOf[TransitOperationTransformer]

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

    val TransitOperationType10: TransitOperationType10 = arbitrary[TransitOperationType10].sample.value

    when(mockReferenceDataService.getSecurityType(eqTo(TransitOperationType10.security))(any()))
      .thenReturn(Future.successful(SecurityType(TransitOperationType10.security, "test2")))

    val result = transformer.transform(TransitOperationType10).apply(emptyUserAnswers).futureValue

    result.getValue(SecurityTypePage).code mustEqual TransitOperationType10.security
    result.getValue(SecurityTypePage).description mustEqual "test2"

  }

}
