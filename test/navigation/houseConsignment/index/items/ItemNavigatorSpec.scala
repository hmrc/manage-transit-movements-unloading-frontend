/*
 * Copyright 2023 HM Revenue & Customs
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

package navigation.houseConsignment.index.items

import base.SpecBase
import generators.Generators
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items._

class ItemNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new ItemNavigator

  "ItemNavigator" - {

    "in Check mode" - {

      val mode = CheckMode

      "must go from Gross Mass page to CrossCheck page" in {
        forAll(arbitrary[BigDecimal]) {
          grossWeight =>
            val userAnswers = emptyUserAnswers.setValue(GrossWeightPage(houseConsignmentIndex, itemIndex), grossWeight)

            navigator
              .nextPage(GrossWeightPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
              .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex))
        }
      }

      "must go from NetWeight page to CrossCheck page" in {
        forAll(arbitrary[Double]) {
          netWeight =>
            val userAnswers = emptyUserAnswers.setValue(NetWeightPage(houseConsignmentIndex, itemIndex), netWeight)

            navigator
              .nextPage(NetWeightPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
              .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex))
        }
      }

      "must go from CombinedNomenclatureCode page to CrossCheck page" in {
        forAll(nonEmptyString) {
          code =>
            val userAnswers = emptyUserAnswers.setValue(CombinedNomenclatureCodePage(houseConsignmentIndex, itemIndex), code)

            navigator
              .nextPage(CombinedNomenclatureCodePage(houseConsignmentIndex, itemIndex), mode, userAnswers)
              .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex))
        }
      }

      "must go from CommodityCode page to CrossCheck page" in {
        forAll(nonEmptyString) {
          code =>
            val userAnswers = emptyUserAnswers.setValue(CommodityCodePage(houseConsignmentIndex, itemIndex), code)

            navigator
              .nextPage(CommodityCodePage(houseConsignmentIndex, itemIndex), mode, userAnswers)
              .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex))
        }
      }
    }
  }
}
