/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import base.SpecBase
import org.scalacheck.Arbitrary.arbitrary
import pages.{AreAnySealsBrokenPage, CanSealsBeReadPage}

class StateOfSealsSpec extends SpecBase {

  "StateOfSeals" - {

    "must return Some(true)" - {
      "when seals readable and not broken" in {
        val userAnswers = emptyUserAnswers
          .setValue(CanSealsBeReadPage, true)
          .setValue(AreAnySealsBrokenPage, false)

        val stateOfSeals = StateOfSeals(userAnswers)

        stateOfSeals.value mustBe Some(true)
      }
    }

    "must return Some(false)" - {
      "when seals readable and broken" in {
        val userAnswers = emptyUserAnswers
          .setValue(CanSealsBeReadPage, true)
          .setValue(AreAnySealsBrokenPage, true)

        val stateOfSeals = StateOfSeals(userAnswers)

        stateOfSeals.value mustBe Some(false)
      }

      "when seals unreadable and broken" in {
        val userAnswers = emptyUserAnswers
          .setValue(CanSealsBeReadPage, false)
          .setValue(AreAnySealsBrokenPage, true)

        val stateOfSeals = StateOfSeals(userAnswers)

        stateOfSeals.value mustBe Some(false)
      }

      "when seals unreadable and not broken" in {
        val userAnswers = emptyUserAnswers
          .setValue(CanSealsBeReadPage, false)
          .setValue(AreAnySealsBrokenPage, false)

        val stateOfSeals = StateOfSeals(userAnswers)

        stateOfSeals.value mustBe Some(false)
      }
    }

    "must return None" - {
      "when can seals be read is unanswered" in {
        forAll(arbitrary[Boolean]) {
          areAnySealsBroken =>
            val userAnswers = emptyUserAnswers
              .setValue(AreAnySealsBrokenPage, areAnySealsBroken)

            val stateOfSeals = StateOfSeals(userAnswers)

            stateOfSeals.value mustBe None
        }
      }

      "when are seals broken is unanswered" in {
        forAll(arbitrary[Boolean]) {
          canSealsBeRead =>
            val userAnswers = emptyUserAnswers
              .setValue(CanSealsBeReadPage, canSealsBeRead)

            val stateOfSeals = StateOfSeals(userAnswers)

            stateOfSeals.value mustBe None
        }
      }
    }
  }
}
