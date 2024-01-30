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
import generated.ConsignorType06
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{ConsignorIdentifierPage, ConsignorNamePage}

class ConsignorTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[ConsignorTransformer]

  "must transform data" - {
    "when consignee defined" in {
      forAll(arbitrary[ConsignorType06]) {
        consignor =>
          val result = transformer.transform(Some(consignor), hcIndex).apply(emptyUserAnswers).futureValue

          result.get(ConsignorIdentifierPage(hcIndex)) mustBe consignor.identificationNumber
          result.get(ConsignorNamePage(hcIndex)) mustBe consignor.name
      }
    }

    "when consignee undefined" in {
      val result = transformer.transform(None, hcIndex).apply(emptyUserAnswers).futureValue

      result.get(ConsignorIdentifierPage(hcIndex)) must not be defined
      result.get(ConsignorNamePage(hcIndex)) must not be defined
    }
  }
}
