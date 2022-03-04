/*
 * Copyright 2022 HM Revenue & Customs
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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class NavigatorUnloadingPermissionSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new NavigatorUnloadingPermission

  "NavigatorUnloadingPermission" - {

    "in Check mode" - {

      "must go from Date Goods Unloaded page to check your answers page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(DateGoodsUnloadedPage, CheckMode, answers, None)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.id))
        }
      }
    }

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(UnknownPage, NormalMode, answers, None)
              .mustBe(routes.IndexController.onPageLoad(arrivalId))
        }
      }

      "must go from date goods unloaded page" - {

        "to can seals be read page when seals exist" in {
          forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission]) {
            (answers, unloadingPermission) =>
              val unloadingPermissionCopy = unloadingPermission.copy(seals = Some(Seals(2, Seq("seal1", "seal2"))))
              navigator
                .nextPage(DateGoodsUnloadedPage, NormalMode, answers, Some(unloadingPermissionCopy))
                .mustBe(routes.CanSealsBeReadController.onPageLoad(answers.id, NormalMode))
          }
        }

        "to unloading summary page when no seals exist" in {
          forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission]) {
            (answers, unloadingPermission) =>
              val unloadingPermissionCopy = unloadingPermission.copy(seals = None)
              navigator
                .nextPage(DateGoodsUnloadedPage, NormalMode, answers, Some(unloadingPermissionCopy))
                .mustBe(routes.UnloadingSummaryController.onPageLoad(answers.id))
          }
        }
      }
    }
  }
}
