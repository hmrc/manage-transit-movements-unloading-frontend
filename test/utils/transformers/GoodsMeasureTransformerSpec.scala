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
import generated.CUSTOM_GoodsMeasureType05
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.{GrossWeightPage, NetWeightPage}

class GoodsMeasureTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[GoodsMeasureTransformer]

  "must transform data" - {
    "when goods measure defined" in {
      forAll(arbitrary[CUSTOM_GoodsMeasureType05]) {
        goodsMeasure =>
          val result = transformer.transform(Some(goodsMeasure), hcIndex, itemIndex).apply(emptyUserAnswers).futureValue

          result.get(GrossWeightPage(hcIndex, itemIndex)) mustEqual goodsMeasure.grossMass
          result.get(NetWeightPage(hcIndex, itemIndex)) mustEqual goodsMeasure.netMass
      }
    }

    "when goods measure undefined" in {
      val result = transformer.transform(None, hcIndex, itemIndex).apply(emptyUserAnswers).futureValue

      result.get(GrossWeightPage(hcIndex, itemIndex)) must not be defined
      result.get(NetWeightPage(hcIndex, itemIndex)) must not be defined
    }
  }
}
