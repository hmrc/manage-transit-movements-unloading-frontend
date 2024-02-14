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
import controllers.actions.{DataRequiredAction, DataRequiredActionImpl}
import generated.AdditionalReferenceType03
import generators.Generators
import models.Index
import models.reference.AdditionalReferenceType
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalactic.anyvals.NonEmptySet
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AdditionalReferenceTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private lazy val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }
  private val transformer: AdditionalReferenceTransformer = app.injector.instanceOf[AdditionalReferenceTransformer]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super.guiceApplicationBuilder().overrides(bind[ReferenceDataConnector].toInstance(mockRefDataConnector))

  "must transform data" in {

    val additionalReferenceType0 = AdditionalReferenceType03("addRefVal", "description")
    //    forAll(arbitrary[Seq[AdditionalReferenceType03]]) {
    //      additionalReference =>
    //        additionalReference.zipWithIndex.map {
    //          case (addRef, i) =>
    println(additionalReferenceType0)

    when(mockRefDataConnector.getAdditionalReferenceType(eqTo(additionalReferenceType0.typeValue))(any(), any()))
      .thenReturn(
        Future.successful(AdditionalReferenceType(documentType = additionalReferenceType0.typeValue, description = "describe me"))
      )

    val result = transformer.transform(Seq(additionalReferenceType0)).apply(emptyUserAnswers).futureValue

    result.getValue(AdditionalReferenceTypePage(Index(0))).documentType mustBe additionalReferenceType0.typeValue
    result.getValue(AdditionalReferenceTypePage(Index(0))).description mustBe "describe me"
    result.get(AdditionalReferenceNumberPage(Index(0))) mustBe additionalReferenceType0.referenceNumber

    //        }
  }
  //  }

}
