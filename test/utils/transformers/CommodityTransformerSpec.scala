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
import generated.CommodityType09
import generators.Generators
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.{CombinedNomenclatureCodePage, CommodityCodePage, CustomsUnionAndStatisticsCodePage, ItemDescriptionPage}
import pages.sections.houseConsignment.index.items.GoodsMeasureSection
import pages.sections.houseConsignment.index.items.dangerousGoods.DangerousGoodsListSection
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}

import scala.concurrent.Future

class CommodityTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[CommodityTransformer]

  private lazy val mockGoodsMeasureTransformer   = mock[GoodsMeasureTransformer]
  private lazy val mockDangerousGoodsTransformer = mock[DangerousGoodsTransformer]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[GoodsMeasureTransformer].toInstance(mockGoodsMeasureTransformer),
        bind[DangerousGoodsTransformer].toInstance(mockDangerousGoodsTransformer)
      )

  "must transform data" in {
    forAll(arbitrary[CommodityType09]) {
      commodity =>
        when(mockGoodsMeasureTransformer.transform(any(), any(), any()))
          .thenReturn {
            ua => Future.successful(ua.setValue(GoodsMeasureSection(hcIndex, itemIndex), Json.obj("foo" -> "bar")))
          }
        when(mockDangerousGoodsTransformer.transform(any(), any(), any()))
          .thenReturn {
            ua => Future.successful(ua.setValue(DangerousGoodsListSection(hcIndex, itemIndex), JsArray(Seq(Json.obj("foo1" -> "bar1")))))
          }

        val result = transformer.transform(commodity, hcIndex, itemIndex).apply(emptyUserAnswers).futureValue

        result.getValue(ItemDescriptionPage(hcIndex, itemIndex)) mustEqual commodity.descriptionOfGoods
        result.get(CustomsUnionAndStatisticsCodePage(hcIndex, itemIndex)) mustEqual commodity.cusCode
        result.get(CommodityCodePage(hcIndex, itemIndex)) mustEqual commodity.CommodityCode.map(_.harmonizedSystemSubHeadingCode)
        result.get(CombinedNomenclatureCodePage(hcIndex, itemIndex)) mustEqual commodity.CommodityCode.flatMap(_.combinedNomenclatureCode)
        result.getValue(GoodsMeasureSection(hcIndex, itemIndex)) mustEqual Json.obj("foo" -> "bar")
        result.getValue(DangerousGoodsListSection(hcIndex, itemIndex)) mustEqual JsArray(Seq(Json.obj("foo1" -> "bar1")))
    }
  }
}
