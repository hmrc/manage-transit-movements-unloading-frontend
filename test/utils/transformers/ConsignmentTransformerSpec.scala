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
import generated.ConsignmentType05
import generators.Generators
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.QuestionPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsPath, Json}

import scala.concurrent.Future

class ConsignmentTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[ConsignmentTransformer]

  private lazy val mockTransportEquipmentTransformer      = mock[TransportEquipmentTransformer]
  private lazy val mockDepartureTransportMeansTransformer = mock[DepartureTransportMeansTransformer]
  private lazy val mockSupportingDocumentTransformer      = mock[SupportingDocumentTransformer]
  private lazy val mockTransportDocumentTransformer       = mock[TransportDocumentTransformer]
  private lazy val mockHouseConsignmentTransformer        = mock[HouseConsignmentTransformer]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[TransportEquipmentTransformer].toInstance(mockTransportEquipmentTransformer),
        bind[DepartureTransportMeansTransformer].toInstance(mockDepartureTransportMeansTransformer),
        bind[SupportingDocumentTransformer].toInstance(mockSupportingDocumentTransformer),
        bind[TransportDocumentTransformer].toInstance(mockTransportDocumentTransformer),
        bind[HouseConsignmentTransformer].toInstance(mockHouseConsignmentTransformer)
      )

  private case object FakeTransportEquipmentSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "transportEquipment"
  }

  private case object FakeDepartureTransportMeansSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "departureTransportMeans"
  }

  private case object FakeSupportingDocumentSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "supportingDocument"
  }

  private case object FakeTransportDocumentSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "transportDocument"
  }

  private case object FakeHouseConsignmentSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "houseConsignment"
  }

  "must transform data" - {
    "when consignment defined" in {
      forAll(arbitrary[ConsignmentType05]) {
        consignment =>
          when(mockTransportEquipmentTransformer.transform(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(FakeTransportEquipmentSection, Json.obj("foo" -> "bar")))
            }

          when(mockDepartureTransportMeansTransformer.transform(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(FakeDepartureTransportMeansSection, Json.obj("foo" -> "bar")))
            }

          when(mockSupportingDocumentTransformer.transform(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(FakeSupportingDocumentSection, Json.obj("foo" -> "bar")))
            }

          when(mockTransportDocumentTransformer.transform(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(FakeTransportDocumentSection, Json.obj("foo" -> "bar")))
            }

          when(mockHouseConsignmentTransformer.transform(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(FakeHouseConsignmentSection, Json.obj("foo" -> "bar")))
            }

          val result = transformer.transform(Some(consignment)).apply(emptyUserAnswers).futureValue

          result.getValue(FakeTransportEquipmentSection) mustBe Json.obj("foo" -> "bar")
          result.getValue(FakeDepartureTransportMeansSection) mustBe Json.obj("foo" -> "bar")
          result.getValue(FakeSupportingDocumentSection) mustBe Json.obj("foo" -> "bar")
          result.getValue(FakeTransportDocumentSection) mustBe Json.obj("foo" -> "bar")
          result.getValue(FakeHouseConsignmentSection) mustBe Json.obj("foo" -> "bar")
      }
    }

    "when consignment undefined" in {
      val result = transformer.transform(None).apply(emptyUserAnswers).futureValue

      result.get(FakeTransportEquipmentSection) must not be defined
      result.get(FakeDepartureTransportMeansSection) must not be defined
      result.get(FakeSupportingDocumentSection) must not be defined
      result.get(FakeTransportDocumentSection) must not be defined
      result.get(FakeHouseConsignmentSection) must not be defined
    }
  }
}
