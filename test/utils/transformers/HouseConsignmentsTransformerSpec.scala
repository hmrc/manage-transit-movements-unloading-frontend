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
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.QuestionPage
import pages.sections.HouseConsignmentSection
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsPath, Json}

import scala.concurrent.Future

class HouseConsignmentsTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[HouseConsignmentsTransformer]

  private lazy val mockConsigneeTransformer               = mock[ConsigneeTransformer]
  private lazy val mockConsignorTransformer               = mock[ConsignorTransformer]
  private lazy val mockDepartureTransportMeansTransformer = mock[DepartureTransportMeansTransformer]
  private lazy val mockConsignmentItemTransformer         = mock[ConsignmentItemTransformer]
  private lazy val mockPrevDocTransformer                 = mock[PreviousDocumentTransformer]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ConsigneeTransformer].toInstance(mockConsigneeTransformer),
        bind[ConsignorTransformer].toInstance(mockConsignorTransformer),
        bind[DepartureTransportMeansTransformer].toInstance(mockDepartureTransportMeansTransformer),
        bind[ConsignmentItemTransformer].toInstance(mockConsignmentItemTransformer),
        bind[PreviousDocumentTransformer].toInstance(mockPrevDocTransformer)
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

  private case class FakePreviousDocumentSection(hcIndex: Index) extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ hcIndex.position.toString \ "previousDocuments"
  }

  "must transform data" in {
    forAll(arbitrary[Seq[HouseConsignmentType04]]) {
      houseConsignments =>
        houseConsignments.zipWithIndex.map {
          case (_, i) =>
            val hcIndex = Index(i)

            when(mockConsigneeTransformer.transform(any(), eqTo(hcIndex))(any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(FakeConsigneeSection(hcIndex), Json.obj("foo" -> i.toString)))
              }

            when(mockConsignorTransformer.transform(any(), eqTo(hcIndex)))
              .thenReturn {
                ua => Future.successful(ua.setValue(FakeConsignorSection(hcIndex), Json.obj("foo" -> i.toString)))
              }

            when(mockDepartureTransportMeansTransformer.transform(any(), eqTo(hcIndex))(any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(FakeDepartureTransportMeansSection(hcIndex), Json.obj("foo" -> i.toString)))
              }

            when(mockConsignmentItemTransformer.transform(any(), eqTo(hcIndex))(any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(FakeConsignmentItemSection(hcIndex), Json.obj("foo" -> i.toString)))
              }

            when(mockPrevDocTransformer.transform(any(), eqTo(hcIndex))(any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(FakePreviousDocumentSection(hcIndex), Json.obj("foo" -> i.toString)))
              }
        }

        val result = transformer.transform(houseConsignments).apply(emptyUserAnswers).futureValue

        houseConsignments.zipWithIndex.map {
          case (hc, i) =>
            val hcIndex = Index(i)

            result.getSequenceNumber(HouseConsignmentSection(hcIndex)) mustBe hc.sequenceNumber
            result.getValue(FakeConsigneeSection(hcIndex)) mustBe Json.obj("foo" -> i.toString)
            result.getValue(FakeConsignorSection(hcIndex)) mustBe Json.obj("foo" -> i.toString)
            result.getValue(FakeDepartureTransportMeansSection(hcIndex)) mustBe Json.obj("foo" -> i.toString)
            result.getValue(FakeConsignmentItemSection(hcIndex)) mustBe Json.obj("foo" -> i.toString)
            result.getValue(FakePreviousDocumentSection(hcIndex)) mustBe Json.obj("foo" -> i.toString)
        }
    }
  }
}
