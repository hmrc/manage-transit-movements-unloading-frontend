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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.{CombinedNomenclatureCodePage, CustomsUnionAndStatisticsCodePage, GrossWeightPage}

class HouseConsignmentItemNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new HouseConsignmentItemNavigator

  "HouseConsignmentItemNavigator" - {

    "in Check mode" - {

      val mode = CheckMode

      "must go from Change Gross Mass Page to House Consignment Item Declaration Summary Page" - {

        val userAnswers = emptyUserAnswers.setValue(GrossWeightPage(houseConsignmentIndex, itemIndex), BigDecimal(123.45))

        navigator
          .nextPage(GrossWeightPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }

      "must go from Customs Union and Statistics Code Page to House Consignment Item Declaration Summary Page" - {

        val userAnswers = emptyUserAnswers.setValue(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex), "12345")

        navigator
          .nextPage(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }

      "must go from Combined Nomenclature Page to House Consignment Item Declaration Summary Page" - {

        val userAnswers = emptyUserAnswers.setValue(CombinedNomenclatureCodePage(houseConsignmentIndex, itemIndex), "23")

        navigator
          .nextPage(CombinedNomenclatureCodePage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }
    }
  }
}
