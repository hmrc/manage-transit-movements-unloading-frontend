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
import generated.AdditionalReferenceType03
import generators.Generators
import models.Index
import models.reference.AdditionalReferenceType
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalReference.AdditionalReferencePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future

class AdditionalReferenceTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer: AdditionalReferenceTransformer = app.injector.instanceOf[AdditionalReferenceTransformer]

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

    val additionalReferenceType03 = Seq(
      AdditionalReferenceType03(sequenceNumber = "num1", typeValue = "type1"),
      AdditionalReferenceType03(sequenceNumber = "num2", typeValue = "type2"),
      AdditionalReferenceType03(sequenceNumber = "num3", typeValue = "type3", referenceNumber = Some("ref3"))
    )

    when(mockRefDataConnector.getAdditionalReferenceType(eqTo("type1"))(any(), any()))
      .thenReturn(
        Future.successful(AdditionalReferenceType(documentType = "type1", description = "describe me"))
      )

    when(mockRefDataConnector.getAdditionalReferenceType(eqTo("type2"))(any(), any()))
      .thenReturn(
        Future.successful(AdditionalReferenceType(documentType = "type2", description = "describe me"))
      )

    when(mockRefDataConnector.getAdditionalReferenceType(eqTo("type3"))(any(), any()))
      .thenReturn(
        Future.successful(AdditionalReferenceType(documentType = "type3", description = "describe me"))
      )

    val result = transformer.transform(additionalReferenceType03).apply(emptyUserAnswers).futureValue

    result.getValue(AdditionalReferencePage(Index(0))).documentType mustBe "type1"
    result.getValue(AdditionalReferencePage(Index(0))).description mustBe "describe me"
    result.getValue(AdditionalReferencePage(Index(0))).referenceNumber mustBe None

    result.getValue(AdditionalReferencePage(Index(1))).documentType mustBe "type2"
    result.getValue(AdditionalReferencePage(Index(1))).description mustBe "describe me"
    result.getValue(AdditionalReferencePage(Index(1))).referenceNumber mustBe None

    result.getValue(AdditionalReferencePage(Index(2))).documentType mustBe "type3"
    result.getValue(AdditionalReferencePage(Index(2))).description mustBe "describe me"
    result.getValue(AdditionalReferencePage(Index(2))).referenceNumber mustBe Some("ref3")

  }

}
