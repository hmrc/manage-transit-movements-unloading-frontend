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
import controllers.routes
import generators.Generators
import models.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.*

class ConsignmentNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new ConsignmentNavigator

  "Navigator" - {

    "in Normal mode" - {

      val mode = NormalMode

      "must go from GrossWeightPage to UnloadingFindings" in {

        val userAnswers = emptyUserAnswers.setValue(GrossWeightPage, BigDecimal(1))
        navigator
          .nextPage(GrossWeightPage, mode, userAnswers)
          .mustEqual(routes.UnloadingFindingsController.onPageLoad(userAnswers.id))
      }

      "must go from UniqueConsignmentReferencePage to UnloadingFindings" in {

        val userAnswers = emptyUserAnswers.setValue(UniqueConsignmentReferencePage, "foo")
        navigator
          .nextPage(UniqueConsignmentReferencePage, mode, userAnswers)
          .mustEqual(routes.UnloadingFindingsController.onPageLoad(userAnswers.id))
      }
    }

    "in Check mode" - {

      val mode = CheckMode

      "must go from GrossWeightPage to UnloadingFindings" in {

        val userAnswers = emptyUserAnswers.setValue(GrossWeightPage, BigDecimal(1))
        navigator
          .nextPage(GrossWeightPage, mode, userAnswers)
          .mustEqual(routes.UnloadingFindingsController.onPageLoad(userAnswers.id))
      }

      "must go from UniqueConsignmentReferencePage to UnloadingFindings" in {

        val userAnswers = emptyUserAnswers.setValue(UniqueConsignmentReferencePage, "foo")
        navigator
          .nextPage(UniqueConsignmentReferencePage, mode, userAnswers)
          .mustEqual(routes.UnloadingFindingsController.onPageLoad(userAnswers.id))
      }
    }
  }
}
