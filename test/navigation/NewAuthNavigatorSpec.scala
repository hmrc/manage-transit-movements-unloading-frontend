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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.NewAuthYesNoPage

class NewAuthNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator = new NewAuthNavigator

  "NewAuthNavigator" - {

    "in NormalMode" - {

      "must go from NewAuthYesNo page to OtherThingsToReport page if the answer is true" in {
        val userAnswers = emptyUserAnswers.setValue(NewAuthYesNoPage, true)

        navigator
          .nextPage(NewAuthYesNoPage, NormalMode, userAnswers)
          .mustBe(controllers.routes.OtherThingsToReportController.onPageLoad(arrivalId, NormalMode))
      }

      "must go from NewAuthYesNo page to UnloadingType page if the answer is false" in {
        val userAnswers = emptyUserAnswers.setValue(NewAuthYesNoPage, false)

        navigator
          .nextPage(NewAuthYesNoPage, NormalMode, userAnswers)
          .mustBe(controllers.routes.UnloadingTypeController.onPageLoad(arrivalId, NormalMode))
      }
    }
  }
}
