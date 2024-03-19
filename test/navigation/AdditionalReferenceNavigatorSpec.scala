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
import models.reference.AdditionalReferenceType
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalReference._

class AdditionalReferenceNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new AdditionalReferenceNavigator

  "AdditionalReferenceNavigator" - {

    "in Normal mode" - {

      val mode = NormalMode

      "must go from AdditionalReferenceTypePage to AddAnotherAdditionalReferenceController" in {

        val additionalReference = arbitrary[AdditionalReferenceType].sample.value

        val userAnswers = emptyUserAnswers
          .setValue(AdditionalReferenceTypePage(itemIndex), additionalReference)

        navigator
          .nextPage(AdditionalReferenceNumberPage(itemIndex), mode, userAnswers)
          .mustBe(controllers.additionalReference.index.routes.AddAnotherAdditionalReferenceController.onPageLoad(arrivalId, mode))

      }

      "must go from AdditionalReferenceNumberPage to AddAnotherAdditionalReferenceController" in {

        val userAnswers = emptyUserAnswers
          .setValue(AdditionalReferenceNumberPage(itemIndex), "test")

        navigator
          .nextPage(AdditionalReferenceNumberPage(itemIndex), mode, userAnswers)
          .mustBe(controllers.additionalReference.index.routes.AddAnotherAdditionalReferenceController.onPageLoad(arrivalId, mode))

      }

    }
  }
}
