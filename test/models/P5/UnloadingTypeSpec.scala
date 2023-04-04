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

package models.P5

import base.SpecBase
import models.UnloadingType
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class UnloadingTypeSpec extends SpecBase with ScalaCheckPropertyChecks {

  "UnloadingType" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(UnloadingType.values)

      forAll(gen) {
        unloadingType =>
          JsString(unloadingType.toString).validate[UnloadingType].asOpt.value mustEqual unloadingType
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] retryUntil (!UnloadingType.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>
          JsString(invalidValue).validate[UnloadingType] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(UnloadingType.values)

      forAll(gen) {
        unloadingType =>
          Json.toJson(unloadingType) mustEqual JsString(unloadingType.toString)
      }
    }
  }
}
