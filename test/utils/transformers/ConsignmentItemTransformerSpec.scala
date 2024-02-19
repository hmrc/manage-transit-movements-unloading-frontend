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
import pages.houseConsignment.index.items.{DeclarationGoodsItemNumberPage, GoodsItemNumberPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsPath, Json}

import scala.Seq
import scala.concurrent.Future

class ConsignmentItemTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[ConsignmentItemTransformer]

  "must transform data" - {

    "when ConsignmentItem defined" in {
      forAll(arbitrary[Seq[ConsignmentItemType04]]) {
        consignmentItems =>
          consignmentItems.zipWithIndex.map {
            case (item, i) =>
              val itemIndex = Index(i)

              val result = transformer.transform(consignmentItems, hcIndex).apply(emptyUserAnswers).futureValue

              result.getValue(DeclarationGoodsItemNumberPage(hcIndex, itemIndex)) mustBe item.declarationGoodsItemNumber
              result.getValue(GoodsItemNumberPage(hcIndex, itemIndex)) mustBe item.goodsItemNumber
          }

      }
    }

    "when ConsignmentItem undefined" in {
      val result = transformer.transform(Seq.empty, hcIndex).apply(emptyUserAnswers).futureValue

      result.get(DeclarationGoodsItemNumberPage(hcIndex, itemIndex)) must not be defined
      result.get(GoodsItemNumberPage(hcIndex, itemIndex)) must not be defined
    }
  }

}
