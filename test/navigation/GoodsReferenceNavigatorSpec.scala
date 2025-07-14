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

package navigation

import base.SpecBase
import generators.Generators
import models._
import navigation.GoodsReferenceNavigator.GoodsReferenceNavigatorProvider
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportEquipment.index.ItemPage

class GoodsReferenceNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigatorProvider = new GoodsReferenceNavigatorProvider

  "GoodsReferenceNavigator" - {

    "in NormalMode" - {

      val equipmentMode      = NormalMode
      val goodsReferenceMode = NormalMode
      val navigator          = navigatorProvider.apply(equipmentMode)

      "must go from goods reference page to apply another item page" in {
        val userAnswers = emptyUserAnswers.setValue(ItemPage(equipmentIndex, itemIndex), BigInt(1))

        navigator
          .nextPage(ItemPage(equipmentIndex, itemIndex), goodsReferenceMode, userAnswers)
          .mustEqual(
            controllers.transportEquipment.index.routes.ApplyAnotherItemController.onPageLoad(arrivalId, equipmentMode, equipmentIndex)
          )

      }
    }

    "in CheckMode" - {

      val equipmentMode      = arbitrary[Mode].sample.value
      val goodsReferenceMode = CheckMode
      val navigator          = navigatorProvider.apply(equipmentMode)

      "must go from goods reference page to cross-check page" in {

        val userAnswers = emptyUserAnswers.setValue(ItemPage(equipmentIndex, itemIndex), BigInt(1))

        navigator
          .nextPage(ItemPage(equipmentIndex, itemIndex), goodsReferenceMode, userAnswers)
          .mustEqual(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
      }
    }
  }
}
