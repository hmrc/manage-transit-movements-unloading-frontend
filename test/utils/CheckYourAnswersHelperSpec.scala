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

package utils

import base.SpecBase
import controllers.p5.routes
import generators.Generators
import models.{CheckMode, Index, Seal, Seals}
import org.scalacheck.Arbitrary.arbitrary
import pages._
import queries.SealsQuery
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import java.time.LocalDate

class CheckYourAnswersHelperSpec extends SpecBase with Generators {

  "when .areAnySealsBroken" - {

    "must return None" - {
      "when AreAnySealsBrokenPage is undefined" in {

        val userAnswers = emptyUserAnswers
        val helper      = new CheckYourAnswersHelper(userAnswers)
        val result      = helper.areAnySealsBroken

        result mustBe None
      }
    }

    "must return Some(row)" - {
      "when AreAnySealsBrokenPage is defined" - {
        "when true" in {

          val userAnswers = emptyUserAnswers.setValue(AreAnySealsBrokenPage, true)
          val helper      = new CheckYourAnswersHelper(userAnswers)
          val result      = helper.areAnySealsBroken

          result mustBe Some(
            SummaryListRow(
              key = "Are any of the seals broken?".toKey,
              value = Value("Yes".toText),
              actions = Some(
                Actions(items =
                  List(
                    ActionItem(
                      content = "Change".toText,
                      href = controllers.p5.routes.AreAnySealsBrokenController.onPageLoad(userAnswers.id, CheckMode).url,
                      visuallyHiddenText = Some("if any of the seals are broken"),
                      attributes = Map("id" -> "change-are-any-seals-broken")
                    )
                  )
                )
              )
            )
          )
        }

        "when false" in {

          val userAnswers = emptyUserAnswers.setValue(AreAnySealsBrokenPage, false)
          val helper      = new CheckYourAnswersHelper(userAnswers)
          val result      = helper.areAnySealsBroken

          result mustBe Some(
            SummaryListRow(
              key = "Are any of the seals broken?".toKey,
              value = Value("No".toText),
              actions = Some(
                Actions(items =
                  List(
                    ActionItem(
                      content = "Change".toText,
                      href = controllers.p5.routes.AreAnySealsBrokenController.onPageLoad(userAnswers.id, CheckMode).url,
                      visuallyHiddenText = Some("if any of the seals are broken"),
                      attributes = Map("id" -> "change-are-any-seals-broken")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }
  }

  "when .canSealsBeRead" - {

    "must return None" - {
      "when CanSealsBeReadPage is undefined" in {

        val userAnswers = emptyUserAnswers
        val helper      = new CheckYourAnswersHelper(userAnswers)
        val result      = helper.canSealsBeRead

        result mustBe None
      }
    }

    "must return Some(row)" - {
      "when CanSealsBeReadPage is defined" - {
        "when true" in {

          val userAnswers = emptyUserAnswers.setValue(CanSealsBeReadPage, true)
          val helper      = new CheckYourAnswersHelper(userAnswers)
          val result      = helper.canSealsBeRead

          result mustBe Some(
            SummaryListRow(
              key = "Are all the seal identification numbers or marks readable?".toKey,
              value = Value("Yes".toText),
              actions = Some(
                Actions(items =
                  List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.CanSealsBeReadController.onPageLoad(userAnswers.id, CheckMode).url,
                      visuallyHiddenText = Some("if all the seal identification numbers or marks are readable"),
                      attributes = Map("id" -> "change-can-seals-be-read")
                    )
                  )
                )
              )
            )
          )
        }

        "when false" in {

          val userAnswers = emptyUserAnswers.setValue(CanSealsBeReadPage, false)
          val helper      = new CheckYourAnswersHelper(userAnswers)
          val result      = helper.canSealsBeRead

          result mustBe Some(
            SummaryListRow(
              key = "Are all the seal identification numbers or marks readable?".toKey,
              value = Value("No".toText),
              actions = Some(
                Actions(items =
                  List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.CanSealsBeReadController.onPageLoad(userAnswers.id, CheckMode).url,
                      visuallyHiddenText = Some("if all the seal identification numbers or marks are readable"),
                      attributes = Map("id" -> "change-can-seals-be-read")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }
  }

  "when .seals" - {

    "must return None" - {
      "when list is empty" in {

        val userAnswers = emptyUserAnswers
        val helper      = new CheckYourAnswersHelper(userAnswers)
        val result      = helper.seals

        result mustBe None
      }
    }

    "must return Some(row)" - {
      "when list is not empty" - {
        "when single seal" in {

          forAll(arbitrary[Seal]) {
            seal =>
              val userAnswers = emptyUserAnswers.setValue(SealPage(Index(0)), seal)
              val helper      = new CheckYourAnswersHelper(userAnswers)
              val result      = helper.seals

              result mustBe Some(
                SummaryListRow(
                  key = "Official customs seal numbers".toKey,
                  value = Value(HtmlContent(seal.sealId)),
                  actions = None
                )
              )
          }
        }

        "when multiple seals" in {

          forAll(listWithMaxLength[Seal](Seals.maxSeals)) {
            seals =>
              val userAnswers = emptyUserAnswers.setValue(SealsQuery, seals)
              val helper      = new CheckYourAnswersHelper(userAnswers)
              val result      = helper.seals

              result mustBe Some(
                SummaryListRow(
                  key = "Official customs seal numbers".toKey,
                  value = Value(HtmlContent(seals.map(_.sealId).mkString("<br>"))),
                  actions = None
                )
              )
          }
        }
      }
    }
  }

  "when .dateGoodsUnloaded" - {

    "must return None" - {
      "when DateGoodsUnloadedPage is undefined" in {

        val userAnswers = emptyUserAnswers
        val helper      = new CheckYourAnswersHelper(userAnswers)
        val result      = helper.dateGoodsUnloaded

        result mustBe None
      }
    }

    "must return Some(row)" - {
      "when DateGoodsUnloadedPage is defined" in {

        val userAnswers = emptyUserAnswers.setValue(DateGoodsUnloadedPage, LocalDate.parse("2000-01-01"))
        val helper      = new CheckYourAnswersHelper(userAnswers)
        val result      = helper.dateGoodsUnloaded

        result mustBe Some(
          SummaryListRow(
            key = "When were the goods unloaded?".toKey,
            value = Value("1 January 2000".toText),
            actions = Some(
              Actions(items =
                List(
                  ActionItem(
                    content = "Change".toText,
                    href = controllers.p5.routes.DateGoodsUnloadedController.onPageLoad(userAnswers.id, CheckMode).url,
                    visuallyHiddenText = Some("the date when the goods were unloaded"),
                    attributes = Map("id" -> "change-date-goods-unloaded")
                  )
                )
              )
            )
          )
        )
      }
    }
  }

}
