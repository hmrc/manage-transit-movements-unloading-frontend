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

import base.SpecBase
import generated.DangerousGoodsType01
import generators.Generators
import models.Index
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.DangerousGoodsPage
import pages.sections.houseConsignment.index.items.dangerousGoods.DangerousGoodsSection

import scala.concurrent.ExecutionContext.Implicits.global

class DangerousGoodsTransformerSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val transformer = new DangerousGoodsTransformer()

  "must transform data" in {
    forAll(arbitrary[Seq[DangerousGoodsType01]]) {
      dangerousGoods =>
        val result = transformer.transform(dangerousGoods, hcIndex, itemIndex).apply(emptyUserAnswers).futureValue

        dangerousGoods.zipWithIndex.map {
          case (dangerousGoods, i) =>
            result.getSequenceNumber(DangerousGoodsSection(hcIndex, itemIndex, Index(i))) mustEqual dangerousGoods.sequenceNumber
            result.getValue(DangerousGoodsPage(hcIndex, itemIndex, Index(i))) mustEqual dangerousGoods.UNNumber
        }
    }
  }
}
