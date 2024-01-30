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
import generated.CommodityType08
import generators.Generators
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{ItemDescriptionPage, QuestionPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsPath, Json}

import scala.concurrent.Future

class CommodityTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[CommodityTransformer]

  private lazy val mockGoodsMeasureTransformer = mock[GoodsMeasureTransformer]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[GoodsMeasureTransformer].toInstance(mockGoodsMeasureTransformer)
      )

  private case object FakeGoodsMeasureSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "goodsMeasure"
  }

  "must transform data" in {
    forAll(arbitrary[CommodityType08]) {
      commodity =>
        when(mockGoodsMeasureTransformer.transform(any(), any(), any()))
          .thenReturn {
            ua => Future.successful(ua.setValue(FakeGoodsMeasureSection, Json.obj("foo" -> "bar")))
          }

        val result = transformer.transform(commodity, hcIndex, itemIndex).apply(emptyUserAnswers).futureValue

        result.getValue(ItemDescriptionPage(hcIndex, itemIndex)) mustBe commodity.descriptionOfGoods
        result.getValue(FakeGoodsMeasureSection) mustBe Json.obj("foo" -> "bar")
    }
  }
}
