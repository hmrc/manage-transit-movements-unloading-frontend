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

package utils

import base.SpecBase
import controllers.routes
import generators.Generators
import models.{CheckMode, Seals}
import org.scalacheck.Arbitrary.arbitrary
import pages._
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels.Text.Literal
import uk.gov.hmrc.viewmodels.{Html, MessageInterpolators}

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

          val userAnswers = emptyUserAnswers.set(AreAnySealsBrokenPage, true).success.value
          val helper      = new CheckYourAnswersHelper(userAnswers)
          val result      = helper.areAnySealsBroken

          result mustBe Some(
            Row(
              key = Key(msg"areAnySealsBroken.checkYourAnswersLabel"),
              value = Value(msg"site.yes"),
              actions = List(
                Action(
                  content = msg"site.edit",
                  href = routes.AreAnySealsBrokenController.onPageLoad(userAnswers.id, CheckMode).url,
                  visuallyHiddenText = Some(msg"areAnySealsBroken.checkYourAnswersLabel"),
                  attributes = Map("id" -> "change-are-any-seals-broken")
                )
              )
            )
          )
        }

        "when false" in {

          val userAnswers = emptyUserAnswers.set(AreAnySealsBrokenPage, false).success.value
          val helper      = new CheckYourAnswersHelper(userAnswers)
          val result      = helper.areAnySealsBroken

          result mustBe Some(
            Row(
              key = Key(msg"areAnySealsBroken.checkYourAnswersLabel"),
              value = Value(msg"site.no"),
              actions = List(
                Action(
                  content = msg"site.edit",
                  href = routes.AreAnySealsBrokenController.onPageLoad(userAnswers.id, CheckMode).url,
                  visuallyHiddenText = Some(msg"areAnySealsBroken.checkYourAnswersLabel"),
                  attributes = Map("id" -> "change-are-any-seals-broken")
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

          val userAnswers = emptyUserAnswers.set(CanSealsBeReadPage, true).success.value
          val helper      = new CheckYourAnswersHelper(userAnswers)
          val result      = helper.canSealsBeRead

          result mustBe Some(
            Row(
              key = Key(msg"canSealsBeRead.checkYourAnswersLabel"),
              value = Value(msg"site.yes"),
              actions = List(
                Action(
                  content = msg"site.edit",
                  href = routes.CanSealsBeReadController.onPageLoad(userAnswers.id, CheckMode).url,
                  visuallyHiddenText = Some(msg"canSealsBeRead.checkYourAnswersLabel"),
                  attributes = Map("id" -> "change-can-seals-be-read")
                )
              )
            )
          )
        }

        "when false" in {

          val userAnswers = emptyUserAnswers.set(CanSealsBeReadPage, false).success.value
          val helper      = new CheckYourAnswersHelper(userAnswers)
          val result      = helper.canSealsBeRead

          result mustBe Some(
            Row(
              key = Key(msg"canSealsBeRead.checkYourAnswersLabel"),
              value = Value(msg"site.no"),
              actions = List(
                Action(
                  content = msg"site.edit",
                  href = routes.CanSealsBeReadController.onPageLoad(userAnswers.id, CheckMode).url,
                  visuallyHiddenText = Some(msg"canSealsBeRead.checkYourAnswersLabel"),
                  attributes = Map("id" -> "change-can-seals-be-read")
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
        val result      = helper.seals(Nil)

        result mustBe None
      }
    }

    "must return Some(row)" - {
      "when list is not empty" - {
        "when single seal" in {

          forAll(arbitrary[String]) {
            str =>
              val userAnswers = emptyUserAnswers
              val helper      = new CheckYourAnswersHelper(userAnswers)
              val result      = helper.seals(Seq(str))

              result mustBe Some(
                Row(
                  key = Key(msg"checkYourAnswers.seals.checkYourAnswersLabel"),
                  value = Value(Html(str)),
                  actions = Nil
                )
              )
          }
        }

        "when multiple seals" in {

          forAll(listWithMaxLength[String](Seals.maxSeals)) {
            strs =>
              val userAnswers = emptyUserAnswers
              val helper      = new CheckYourAnswersHelper(userAnswers)
              val result      = helper.seals(strs)

              result mustBe Some(
                Row(
                  key = Key(msg"checkYourAnswers.seals.checkYourAnswersLabel"),
                  value = Value(Html(strs.mkString("<br>"))),
                  actions = Nil
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

        val userAnswers = emptyUserAnswers.set(DateGoodsUnloadedPage, LocalDate.parse("2000-01-01")).success.value
        val helper      = new CheckYourAnswersHelper(userAnswers)
        val result      = helper.dateGoodsUnloaded

        result mustBe Some(
          Row(
            key = Key(msg"dateGoodsUnloaded.checkYourAnswersLabel"),
            value = Value(Literal("1 January 2000")),
            actions = List(
              Action(
                content = msg"site.edit",
                href = routes.DateGoodsUnloadedController.onPageLoad(userAnswers.id, CheckMode).url,
                visuallyHiddenText = Some(msg"dateGoodsUnloaded.visually.hidden"),
                attributes = Map("id" -> "change-date-goods-unloaded")
              )
            )
          )
        )
      }
    }
  }

}
