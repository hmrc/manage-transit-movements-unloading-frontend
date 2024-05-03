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

package navigation.houseConsignment.index

import base.SpecBase
import generators.Generators
import models._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.GrossWeightPage

class HouseConsignmentNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new HouseConsignmentNavigator

  "GrossWeightNavigator" - {

    "in Check mode" - {

      val mode = CheckMode

      "must go from GrossWeightPage to HouseConsignmentController" in {

        val userAnswers = emptyUserAnswers
          .setValue(GrossWeightPage(hcIndex), BigDecimal("12.0"))

        navigator
          .nextPage(GrossWeightPage(hcIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))

      }

    }
  }
}
