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
import models._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.mvc.Result
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnloadingGuidanceNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  implicit private class ResultOps(result: Result) {
    def toUrl: String = redirectLocation(Future(result)).value
  }

  "UnloadingGuidanceNavigator" - {
    "must redirect to" - {
      "unloading type page" - {
        "when newAuth is false" in {
          val result = UnloadingGuidanceNavigator.unloadingGuidanceNavigate(arrivalId = arrivalId, newAuth = false, goodsTooLarge = None).toUrl
          result mustBe routes.UnloadingTypeController.onPageLoad(arrivalId, NormalMode).url
        }
      }
      "large unsealed goods record discrepancies yes no page" - {
        "when newAuth is true and goodsTooLarge is true" in {
          val result = UnloadingGuidanceNavigator.unloadingGuidanceNavigate(arrivalId = arrivalId, newAuth = true, goodsTooLarge = Some(true)).toUrl
          result mustBe routes.LargeUnsealedGoodsRecordDiscrepanciesYesNoController.onPageLoad(arrivalId, NormalMode).url
        }
      }
      "seals replaced by customs authority yes no page" - {
        "when newAuth is true and goodsTooLarge is false" in {
          val result = UnloadingGuidanceNavigator.unloadingGuidanceNavigate(arrivalId = arrivalId, newAuth = true, goodsTooLarge = Some(false)).toUrl
          result mustBe routes.SealsReplacedByCustomsAuthorityYesNoController.onPageLoad(arrivalId, NormalMode).url
        }
      }
      "goods too large for container yes no page" - {
        "when newAuth is true and goodsTooLarge has not been answered" in {
          val result = UnloadingGuidanceNavigator.unloadingGuidanceNavigate(arrivalId = arrivalId, newAuth = true, goodsTooLarge = None).toUrl
          result mustBe routes.GoodsTooLargeForContainerYesNoController.onPageLoad(arrivalId, NormalMode).url
        }
      }
    }
  }
}
