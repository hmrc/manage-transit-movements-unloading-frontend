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
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.{AdditionalReferenceType, Country}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import pages.departureMeansOfTransport._

class AdditionalReferenceNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new AdditionalReferenceNavigator

  "DepartureTransportMeansNavigator" - {

    "in Checkmode" - {

      val mode = CheckMode

      "must go from AdditionalReferenceTypePage to UnloadingFindingsPage page" - {

        val userAnswers = emptyUserAnswers.setValue(AdditionalReferenceTypePage(additionalReferenceIndex), AdditionalReferenceType("test", "test"))

        navigator
          .nextPage(AdditionalReferenceTypePage(additionalReferenceIndex), mode, userAnswers)
          .mustBe(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
      }

      "must go from AdditionalReferenceNumberPage to UnloadingFindingsPage page" - {

        val userAnswers = emptyUserAnswers.setValue(AdditionalReferenceNumberPage(additionalReferenceIndex), "test")

        navigator
          .nextPage(AdditionalReferenceNumberPage(additionalReferenceIndex), mode, userAnswers)
          .mustBe(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
      }

    }
  }
}
