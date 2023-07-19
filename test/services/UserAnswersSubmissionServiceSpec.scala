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

package services

import base.SpecBase
import play.api.libs.json.Json

class UserAnswersSubmissionServiceSpec extends SpecBase {

  "removeNavigationData" - {

    "must remove navigation element from JsObject" in {

      val data = Json.obj(("otherData" -> "foo"), ("navigationData" -> "bar"))

      val userAnswers = emptyUserAnswers.copy(data = data)

      val expectedData = Json.obj(("otherData" -> "foo"))

      val expectedUserAnswers = userAnswers.copy(data = expectedData)

      val result = UserAnswersSubmissionService.removeNavigationData(userAnswers)

      result.toOption.value mustBe expectedUserAnswers
    }

    "must return same userAnswers if data element does not exist" in {

      val data = Json.obj(("otherData" -> "foo"))

      val userAnswers = emptyUserAnswers.copy(data = data)

      val result = UserAnswersSubmissionService.removeNavigationData(userAnswers)

      result.toOption.value mustBe userAnswers
    }
  }

  "setStateOfSeals" - {

    "must set element stateOfSeals to 1" - {

      "when seals can be read and no seals are broken" in {

        val data = Json.obj(
          "navigationData" ->
            Json.obj(
              "canSealsBeRead"    -> true,
              "areAnySealsBroken" -> false
            )
        )

        val userAnswers = emptyUserAnswers.copy(data = data)

        val expectedData = Json.obj(
          "n1:CC044C" -> Json.obj(
            "UnloadingRemark" -> Json.obj(
              "stateOfSeals" -> "1"
            )
          ),
          "navigationData" -> Json.obj(
            "canSealsBeRead"    -> true,
            "areAnySealsBroken" -> false
          )
        )

        val expectedUserAnswers = userAnswers.copy(data = expectedData)

        val result = UserAnswersSubmissionService.setStateOfSeals(userAnswers)

        result.toOption.value mustBe expectedUserAnswers
      }
    }

    "must set element stateOfSeals to 0" - {

      "when seals cannot be read and no seals are broken" in {

        val data = Json.obj(
          "navigationData" ->
            Json.obj(
              "canSealsBeRead"    -> false,
              "areAnySealsBroken" -> false
            )
        )

        val userAnswers = emptyUserAnswers.copy(data = data)

        val expectedData = Json.obj(
          "n1:CC044C" -> Json.obj(
            "UnloadingRemark" -> Json.obj(
              "stateOfSeals" -> "0"
            )
          ),
          "navigationData" -> Json.obj(
            "canSealsBeRead"    -> false,
            "areAnySealsBroken" -> false
          )
        )

        val expectedUserAnswers = userAnswers.copy(data = expectedData)

        val result = UserAnswersSubmissionService.setStateOfSeals(userAnswers)

        result.toOption.value mustBe expectedUserAnswers
      }

      "when seals can be read and seals are broken" in {

        val data = Json.obj(
          "navigationData" ->
            Json.obj(
              "canSealsBeRead"    -> true,
              "areAnySealsBroken" -> true
            )
        )

        val userAnswers = emptyUserAnswers.copy(data = data)

        val expectedData = Json.obj(
          "n1:CC044C" -> Json.obj(
            "UnloadingRemark" -> Json.obj(
              "stateOfSeals" -> "0"
            )
          ),
          "navigationData" -> Json.obj(
            "canSealsBeRead"    -> true,
            "areAnySealsBroken" -> true
          )
        )

        val expectedUserAnswers = userAnswers.copy(data = expectedData)

        val result = UserAnswersSubmissionService.setStateOfSeals(userAnswers)

        result.toOption.value mustBe expectedUserAnswers
      }

    }

    "must not set element stateOfSeals" - {

      "when no seal data is available" in {

        val userAnswers = emptyUserAnswers

        val result = UserAnswersSubmissionService.setStateOfSeals(userAnswers)

        result.toOption.value mustBe userAnswers
      }
    }
  }

  "userAnswersToSubmission" - {

    "must set seal state and remove navigation data" in {

      val data = Json.obj(
        "navigationData" ->
          Json.obj(
            "canSealsBeRead"    -> true,
            "areAnySealsBroken" -> false
          )
      )

      val userAnswers = emptyUserAnswers.copy(data = data)

      val expectedData = Json.obj(
        "n1:CC044C" -> Json.obj(
          "UnloadingRemark" -> Json.obj(
            "stateOfSeals" -> "1"
          )
        )
      )

      val expectedUserAnswers = userAnswers.copy(data = expectedData)

      val result = UserAnswersSubmissionService.userAnswersToSubmission(userAnswers)

      result.toOption.value mustBe expectedUserAnswers
    }
  }

}
