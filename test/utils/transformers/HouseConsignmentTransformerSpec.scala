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
import generated.HouseConsignmentType04
import generators.Generators
import models.Index
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.QuestionPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsPath, Json}

import scala.concurrent.Future

class HouseConsignmentTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[HouseConsignmentTransformer]

  private lazy val mockConsigneeTransformer               = mock[ConsigneeTransformer]
  private lazy val mockConsignorTransformer               = mock[ConsignorTransformer]
  private lazy val mockDepartureTransportMeansTransformer = mock[DepartureTransportMeansTransformer]
  private lazy val mockConsignmentItemTransformer         = mock[ConsignmentItemTransformer]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ConsigneeTransformer].toInstance(mockConsigneeTransformer),
        bind[ConsignorTransformer].toInstance(mockConsignorTransformer),
        bind[DepartureTransportMeansTransformer].toInstance(mockDepartureTransportMeansTransformer),
        bind[ConsignmentItemTransformer].toInstance(mockConsignmentItemTransformer)
      )

  private case class FakeConsigneeSection(hcIndex: Index) extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ hcIndex.position.toString \ "consignee"
  }

  private case class FakeConsignorSection(hcIndex: Index) extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ hcIndex.position.toString \ "consignor"
  }

  private case class FakeDepartureTransportMeansSection(hcIndex: Index) extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ hcIndex.position.toString \ "departureTransportMeans"
  }

  private case class FakeConsignmentItemSection(hcIndex: Index) extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ hcIndex.position.toString \ "consignmentItems"
  }

  "must transform data" in {
    forAll(arbitrary[Seq[HouseConsignmentType04]]) {
      houseConsignments =>
        houseConsignments.zipWithIndex.map {
          case (_, i) =>
            when(mockConsigneeTransformer.transform(any(), any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(FakeConsigneeSection(Index(i)), Json.obj("foo" -> "bar")))
              }

            when(mockConsignorTransformer.transform(any(), any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(FakeConsignorSection(Index(i)), Json.obj("foo" -> "bar")))
              }

            when(mockDepartureTransportMeansTransformer.transform(any(), any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(FakeDepartureTransportMeansSection(Index(i)), Json.obj("foo" -> "bar")))
              }

            when(mockConsignmentItemTransformer.transform(any(), any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(FakeConsignmentItemSection(Index(i)), Json.obj("foo" -> "bar")))
              }

            val result = transformer.transform(houseConsignments).apply(emptyUserAnswers).futureValue

            result.getValue(FakeConsigneeSection(Index(i))) mustBe Json.obj("foo" -> "bar")
            result.getValue(FakeConsignorSection(Index(i))) mustBe Json.obj("foo" -> "bar")
            result.getValue(FakeDepartureTransportMeansSection(Index(i))) mustBe Json.obj("foo" -> "bar")
            result.getValue(FakeConsignmentItemSection(Index(i))) mustBe Json.obj("foo" -> "bar")
        }
    }
  }
}
