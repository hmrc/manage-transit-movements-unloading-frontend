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
import generated.ConsignmentItemType04
import generators.Generators
import models.Index
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.QuestionPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsPath, Json}

import scala.concurrent.Future

class ConsignmentItemTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[ConsignmentItemTransformer]

  private lazy val mockCommodityTransformer = mock[CommodityTransformer]
  private lazy val mockPackagingTransformer = mock[PackagingTransformer]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[CommodityTransformer].toInstance(mockCommodityTransformer),
        bind[PackagingTransformer].toInstance(mockPackagingTransformer)
      )

  private case class FakeCommoditySection(itemIndex: Index) extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ itemIndex.position.toString \ "commodity"
  }

  private case class FakePackagingSection(itemIndex: Index) extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ itemIndex.position.toString \ "packaging"
  }

  "must transform data" in {
    forAll(arbitrary[Seq[ConsignmentItemType04]]) {
      consignmentItems =>
        consignmentItems.zipWithIndex.map {
          case (_, i) =>
            val itemIndex = Index(i)

            when(mockCommodityTransformer.transform(any(), any(), eqTo(itemIndex)))
              .thenReturn {
                ua => Future.successful(ua.setValue(FakeCommoditySection(itemIndex), Json.obj("foo" -> i.toString)))
              }

            when(mockPackagingTransformer.transform(any(), any(), eqTo(itemIndex))(any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(FakePackagingSection(itemIndex), Json.obj("foo" -> i.toString)))
              }
        }

        val result = transformer.transform(consignmentItems, hcIndex).apply(emptyUserAnswers).futureValue

        consignmentItems.zipWithIndex.map {
          case (_, i) =>
            val itemIndex = Index(i)

            result.getValue(FakeCommoditySection(itemIndex)) mustBe Json.obj("foo" -> i.toString)
            result.getValue(FakePackagingSection(itemIndex)) mustBe Json.obj("foo" -> i.toString)
        }
    }
  }
}
