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

package utils.answersHelpers

import base.SpecBase
import models.Index
import play.api.libs.json.{JsArray, JsValue, Json}

class AnswersHelperSpec extends SpecBase {

  private val answersHelper = new AnswersHelper(emptyUserAnswers)
  import answersHelper._

  "mapWithIndex" - {
    "must filter out js objects which only contain a sequence number" in {
      val array = Json
        .parse("""
          |[
          |  {
          |    "sequenceNumber" : "1",
          |    "foo" : "foo"
          |  },
          |  {
          |    "sequenceNumber" : "2"
          |  },
          |  {
          |    "sequenceNumber" : "3",
          |    "bar" : "bar"
          |  }
          |]
          |""".stripMargin)
        .as[JsArray]

      val result = array.mapWithIndex[(JsValue, Index)] {
        case (jsValue, index) => (jsValue, index)
      }

      result.size mustBe 2

      result.head._1 mustBe Json.obj(
        "sequenceNumber" -> "1",
        "foo"            -> "foo"
      )
      result.head._2 mustBe Index(0)

      result(1)._1 mustBe Json.obj(
        "sequenceNumber" -> "3",
        "bar"            -> "bar"
      )
      result(1)._2 mustBe Index(1)
    }
  }
}
