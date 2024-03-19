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

package utils.answersHelpers.consignment.houseConsignment.item

import org.scalacheck.Gen
import pages.houseConsignment.index.items.DangerousGoodsPage
import utils.answersHelpers.AnswersHelperSpecBase

class DangerousGoodsAnswersHelperSpec extends AnswersHelperSpecBase {

  "DangerousGoodsAnswerHelper" - {

    "dangerousGoodsRow" - {
      val page = DangerousGoodsPage(hcIndex, itemIndex, dangerousGoodsIndex)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new DangerousGoodsAnswerHelper(emptyUserAnswers, hcIndex, itemIndex, dangerousGoodsIndex)
          helper.dangerousGoodsRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new DangerousGoodsAnswerHelper(answers, hcIndex, itemIndex, additionalReferenceIndex)
              val result = helper.dangerousGoodsRow.value

              result.key.value mustBe "UN number 1"
              result.value.value mustBe value
              result.actions mustBe None

          }
        }
      }
    }
  }
}
