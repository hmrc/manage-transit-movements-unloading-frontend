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
import generated.CC043CType
import generators.Generators
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.QuestionPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsPath, Json}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class IE043TransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[IE043Transformer]

  private lazy val mockConsignmentTransformer                      = mock[ConsignmentTransformer]
  private lazy val mockCustomsOfficeOfDestinationActualTransformer = mock[CustomsOfficeOfDestinationActualTransformer]
  private lazy val mockTransitOperationTransformer                 = mock[TransitOperationTransformer]
  private lazy val mockHotPTransformer                             = mock[HolderOfTheTransitProcedureTransformer]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ConsignmentTransformer].toInstance(mockConsignmentTransformer),
        bind[CustomsOfficeOfDestinationActualTransformer].toInstance(mockCustomsOfficeOfDestinationActualTransformer),
        bind[TransitOperationTransformer].toInstance(mockTransitOperationTransformer),
        bind[HolderOfTheTransitProcedureTransformer].toInstance(mockHotPTransformer)
      )

  private case object FakeConsignmentSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "consignment"
  }

  private case object FakeTransitOperationSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "transitOperation"
  }

  private case object FakeCustomsOfficeOfDestinationActualSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "CustomsOfficeOfDestinationActual"
  }

  private case object HotPSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "HolderOfTheTransitProcedure" \ "Address"
  }

  "must transform data" in {
    forAll(arbitrary[CC043CType]) {
      _ =>
        when(mockConsignmentTransformer.transform(any())(any()))
          .thenReturn {
            ua => Future.successful(ua.setValue(FakeConsignmentSection, Json.obj("foo" -> "bar")))
          }
        when(mockCustomsOfficeOfDestinationActualTransformer.transform(any())(any()))
          .thenReturn {
            ua => Future.successful(ua.futureValue.setValue(FakeCustomsOfficeOfDestinationActualSection, Json.obj("foo1" -> "bar1")))
          }
        when(mockHotPTransformer.transform(any())(any()))
          .thenReturn {
            ua => Future.successful(ua.setValue(HotPSection, Json.obj("country" -> "GB")))
          }

        when(mockTransitOperationTransformer.transform(any())(any()))
          .thenReturn {
            ua => Future.successful(ua.setValue(FakeTransitOperationSection, Json.obj("foo" -> "bar")))
          }

        val result = transformer.transform(emptyUserAnswers).futureValue

        result.getValue(FakeConsignmentSection) mustBe Json.obj("foo" -> "bar")
        result.getValue(FakeCustomsOfficeOfDestinationActualSection) mustBe Json.obj("foo1" -> "bar1")
        result.getValue(FakeTransitOperationSection) mustBe Json.obj("foo" -> "bar")
        result.getValue(HotPSection) mustBe Json.obj("country" -> "GB")
    }
  }
}
