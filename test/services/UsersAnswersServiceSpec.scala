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
import models.{ArrivalId, EoriNumber, MovementReferenceNumber, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import pages.NewAuthYesNoPage
import pages.sections.OtherQuestionsSection
import play.api.libs.json.{JsBoolean, JsObject, JsTrue, Json}
import utils.transformers.IE043Transformer

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class UsersAnswersServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val dataTransformer               = mock[IE043Transformer]
  private val service                       = new UsersAnswersService(dataTransformer)
  implicit private val ec: ExecutionContext = ExecutionContext.global

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(dataTransformer)
  }

  val now = Instant.now()

  val jsonBeforeEverything = Json
    .parse(s"""
         |{
         |  "otherQuestions" : {
         |    "userChoseNewProcedure" : true,
         |    "foo" : "bar"
         |  },
         |  "someDummyTransformedData" : {
         |    "foo" : "bar"
         |  },
         |  "someDummyDiscrepancies" : {
         |    "foo" : "bar"
         |  }
         |}
         |""".stripMargin)
    .as[JsObject]

  val jsonAfterRetainOtherQuestionsAndWipe = Json
    .parse(s"""
         |{
         |  "otherQuestions" : {
         |    "userChoseNewProcedure" : true,
         |    "foo" : "bar"
         |  }
         |}
         |""".stripMargin)
    .as[JsObject]

  val jsonAfterWipe = Json
    .parse(s"""
         |{}
         |""".stripMargin)
    .as[JsObject]

  val jsonAfterTransformation = Json
    .parse(s"""
         |{
         |  "someDummyTransformedData" : {
         |    "foo" : "bar"
         |  }
         |}
         |""".stripMargin)
    .as[JsObject]

  val userAnswersBeforeEverything                 = emptyUserAnswers.copy(data = jsonBeforeEverything, lastUpdated = now)
  val userAnswersAfterWipe                        = emptyUserAnswers.copy(data = jsonAfterWipe, lastUpdated = now)
  val userAnswersAfterRetainOtherQuestionsAndWipe = emptyUserAnswers.copy(data = jsonAfterRetainOtherQuestionsAndWipe, lastUpdated = now)
  val userAnswersAfterTransformation              = emptyUserAnswers.copy(data = jsonAfterTransformation, lastUpdated = now)

  val transformedData = Json.obj("someDummyTransformedData" -> Json.obj("foo" -> "bar"))

  "UsersAnswersService" - {
    "wipeAndTransform" - {
      "wipe all data and apply the transformation block" in {
        val result = service.wipeAndTransform(userAnswersBeforeEverything, ua => Future.successful(ua))

        whenReady(result) {
          updatedAnswers =>
            updatedAnswers.data mustBe userAnswersAfterWipe.data
        }
      }
    }

    "retainAndTransform" - {
      "retain the specified page's data and apply the transformation and wipe the rest out" in {
        val pageToRetain = OtherQuestionsSection
        val result = service.retainAndTransform(userAnswersBeforeEverything, pageToRetain) {
          ua =>
            Future.successful(ua)
        }

        whenReady(result) {
          updatedAnswers =>
            updatedAnswers.data mustBe userAnswersAfterRetainOtherQuestionsAndWipe.data
        }
      }
    }

    "updateConditionalAndWipe" - {
      "wipe everything but DidUserChooseNewProcedurePage and transform data when the value has changed and value is true" in {
        println(JsTrue)
        println(JsBoolean(true))
        when(dataTransformer.transform(any())(any(), any()))
          .thenReturn(Future.successful(userAnswersAfterTransformation))

        val result = service.updateConditionalAndWipe(page = NewAuthYesNoPage, value = true, userAnswersBeforeEverything)

        whenReady(result) {
          updatedAnswers =>
            verify(dataTransformer).transform(eqTo(userAnswersAfterWipe))(any(), any())
            updatedAnswers.data mustBe transformedData ++ Json.obj("otherQuestions" -> Json.obj("userChoseNewProcedure" -> true, "newAuthYesNo" -> true))
        }
      }

      "update but do not wipe or transform data when the value has not changed" in {
        val unchangedUserAnswers = emptyUserAnswers.copy(data = Json.obj("otherQuestions" -> Json.obj("newAuthYesNo" -> false)))

        val result = service.updateConditionalAndWipe(page = NewAuthYesNoPage, value = false, unchangedUserAnswers)

        whenReady(result) {
          updatedAnswers =>
            verify(dataTransformer, never()).transform(eqTo(userAnswersAfterWipe))(any(), any())
            updatedAnswers mustBe unchangedUserAnswers
        }
      }

      "update but do not wipe or transform data when the value has changed but the value is false" in {
        val initialUserAnswers = emptyUserAnswers.copy(data = Json.obj("otherQuestions" -> Json.obj("newAuthYesNo" -> true)), lastUpdated = now)
        val updatedUserAnswers = emptyUserAnswers.copy(data = Json.obj("otherQuestions" -> Json.obj("newAuthYesNo" -> false)), lastUpdated = now)

        val result = service.updateConditionalAndWipe(page = NewAuthYesNoPage, value = false, initialUserAnswers)

        whenReady(result) {
          updatedAnswers =>
            verify(dataTransformer, never()).transform(eqTo(userAnswersAfterWipe))(any(), any())
            updatedAnswers mustBe updatedUserAnswers
        }
      }
    }
  }
}
