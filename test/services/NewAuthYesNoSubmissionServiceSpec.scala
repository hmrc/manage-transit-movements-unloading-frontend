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
import models.UserAnswers
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import pages.NewAuthYesNoPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Call, Results}
import utils.transformers.IE043Transformer

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class NewAuthYesNoSubmissionServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val mockTransformer               = mock[IE043Transformer]
  private val service                       = new NewAuthYesNoSubmissionService(mockTransformer)
  implicit private val ec: ExecutionContext = ExecutionContext.global

  val userAnswers        = mock[UserAnswers]
  val transformedAnswers = mock[UserAnswers]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(userAnswers)
    reset(transformedAnswers)
  }

  "NewAuthYesNoSubmissionService should" - {
    "call transformer and update UserAnswers when the answer changes to true" in {
      when(userAnswers.hasAnswerChanged(NewAuthYesNoPage, true)).thenReturn(true)
      when(userAnswers.wipeAndTransform(any())).thenReturn(Future.successful(transformedAnswers))
      when(transformedAnswers.set(NewAuthYesNoPage, true)).thenReturn(Success(transformedAnswers))

      service.updateUserAnswers(true, userAnswers).map {
        result =>
          verify(mockTransformer).transform(any())
          verify(userAnswers).wipeAndTransform(any())
          result mustBe transformedAnswers
      }
    }

    "not call transformer and only update UserAnswers when the answer has not changed" in {
      when(userAnswers.hasAnswerChanged(NewAuthYesNoPage, false)).thenReturn(false)
      when(userAnswers.set(NewAuthYesNoPage, false)).thenReturn(Success(userAnswers))

      service.updateUserAnswers(false, userAnswers).map {
        result =>
          verify(mockTransformer, never()).transform(any())
          verify(userAnswers, never()).wipeAndTransform(any())
          result mustBe userAnswers
      }
    }

    "not call transformer but update UserAnswers when the answer changes to false" in {
      when(userAnswers.hasAnswerChanged(NewAuthYesNoPage, false)).thenReturn(true)
      when(userAnswers.set(NewAuthYesNoPage, false)).thenReturn(Success(userAnswers))

      service.updateUserAnswers(false, userAnswers).map {
        result =>
          verify(mockTransformer, never()).transform(any())
          verify(userAnswers, never()).wipeAndTransform(any())
          result mustBe userAnswers
      }
    }
  }
}
