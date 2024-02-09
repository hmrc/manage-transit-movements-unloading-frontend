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
import generated.GoodsReferenceType02
import generators.Generators
import models.Index
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportEquipment.index.ItemPage

class ItemsTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[ItemsTransformer]

  "must transform data" in {
    forAll(arbitrary[Seq[GoodsReferenceType02]]) {
      goodsReferenceSeq =>
        goodsReferenceSeq.zipWithIndex.map {
          case (goodsReference, i) =>
            val result = transformer.transform(goodsReferenceSeq, equipmentIndex).apply(emptyUserAnswers).futureValue

            result.getValue(ItemPage(equipmentIndex, Index(i))).declarationGoodsItemNumber mustBe goodsReference.declarationGoodsItemNumber
        }
    }
  }
}
