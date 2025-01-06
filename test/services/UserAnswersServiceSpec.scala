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

package services

import base.{AppWithDefaultMockFixtures, SpecBase}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import pages.QuestionPage
import play.api.libs.json.{JsObject, JsPath, Json}
import utils.transformers.IE043Transformer

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserAnswersServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val dataTransformer = mock[IE043Transformer]
  private val service         = new UserAnswersService(dataTransformer)

  private case object FooPage extends QuestionPage[String] {
    override def path: JsPath = JsPath \ "foo"
  }

  private case object BarPage extends QuestionPage[String] {
    override def path: JsPath = JsPath \ "bar"
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(dataTransformer)
  }

  private val now = Instant.now()

  private val jsonBeforeEverything = Json
    .parse(s"""
         |{
         |  "foo" : "fooValue",
         |  "bar" : "barValue",
         |  "someDummyTransformedData" : {
         |    "foo" : "bar"
         |  },
         |  "someDummyDiscrepancies" : {
         |    "foo" : "bar"
         |  }
         |}
         |""".stripMargin)
    .as[JsObject]

  private val jsonAfterWipe = Json
    .parse(s"""
         |{}
         |""".stripMargin)
    .as[JsObject]

  private val jsonAfterTransformation = Json
    .parse(s"""
         |{
         |  "someDummyTransformedData" : {
         |    "foo" : "bar"
         |  }
         |}
         |""".stripMargin)
    .as[JsObject]

  private val jsonAfterRetention = Json
    .parse(s"""
         |{
         |  "someDummyTransformedData" : {
         |    "foo" : "bar"
         |  },
         |  "foo" : "fooValue",
         |  "bar" : "barValue"
         |}
         |""".stripMargin)
    .as[JsObject]

  private val userAnswersBeforeEverything    = emptyUserAnswers.copy(data = jsonBeforeEverything, lastUpdated = now)
  private val userAnswersAfterWipe           = emptyUserAnswers.copy(data = jsonAfterWipe, lastUpdated = now)
  private val userAnswersAfterTransformation = emptyUserAnswers.copy(data = jsonAfterTransformation, lastUpdated = now)
  private val userAnswersAfterRetention      = emptyUserAnswers.copy(data = jsonAfterRetention, lastUpdated = now)

  "UserAnswersService" - {
    "wipeAndTransform" - {
      "wipe all data and apply the transformation block" in {
        val result = service.wipeAndTransform(userAnswersBeforeEverything) {
          ua => Future.successful(ua)
        }

        whenReady(result) {
          updatedAnswers =>
            updatedAnswers.data mustBe userAnswersAfterWipe.data
        }
      }
    }

    "retainAndTransform" - {
      "retain the specified page's data and apply the transformation and wipe the rest out" in {
        when(dataTransformer.transform(any())(any(), any()))
          .thenReturn(Future.successful(userAnswersAfterTransformation))

        val result = service.retainAndTransform(userAnswersBeforeEverything, FooPage, BarPage)

        whenReady(result) {
          updatedAnswers =>
            updatedAnswers.data mustBe userAnswersAfterRetention.data
        }
      }
    }
  }
}
