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
import navigation.SealNavigator.SealNavigatorProvider
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportEquipment.index.seals.SealIdentificationNumberPage

class SealNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigatorProvider = new SealNavigatorProvider

  "SealNavigator" - {

    "in NormalMode" - {

      val equipmentMode = NormalMode
      val sealMode      = NormalMode
      val navigator     = navigatorProvider.apply(equipmentMode)

      "must go from SealIdentificationNumber page to AddAnotherSeal page" in {
        val userAnswers = emptyUserAnswers.setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), "seal1")

        navigator
          .nextPage(SealIdentificationNumberPage(equipmentIndex, sealIndex), sealMode, userAnswers)
          .mustBe(controllers.transportEquipment.index.routes.AddAnotherSealController.onPageLoad(arrivalId, equipmentMode, sealMode, equipmentIndex))

      }
    }

    "in CheckMode" - {

      val equipmentMode = arbitrary[Mode].sample.value
      val sealMode      = CheckMode
      val navigator     = navigatorProvider.apply(equipmentMode)

      "must go from SealIdentificationNumber page to UnloadingFindings page" in {

        val userAnswers = emptyUserAnswers.setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), "seal1")

        navigator
          .nextPage(SealIdentificationNumberPage(equipmentIndex, sealIndex), sealMode, userAnswers)
          .mustBe(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
      }
    }
  }
}
