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
  private val service                       = new NewAuthYesNoSubmissionService(mockSessionRepository, mockTransformer)
  implicit private val ec: ExecutionContext = ExecutionContext.global

  val redirectCall       = mock[UserAnswers => Call]
  val userAnswers        = mock[UserAnswers]
  val transformedAnswers = mock[UserAnswers]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(redirectCall)
    reset(userAnswers)
    reset(transformedAnswers)
  }

  "call transformer and session repository when the answer changes to true" in {
    when(userAnswers.hasAnswerChanged(NewAuthYesNoPage, true)).thenReturn(true)
    when(userAnswers.wipeAndTransform(any())).thenReturn(Future.successful(transformedAnswers))
    when(transformedAnswers.set(NewAuthYesNoPage, true)).thenReturn(Success(userAnswers))
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
    when(redirectCall(any())).thenReturn(Call("GET", "/some-url"))

    service.submitNewAuth(true, userAnswers, redirectCall).map {
      result =>
        verify(mockTransformer).transform(any())
        verify(mockSessionRepository).set(userAnswers)
        result mustBe Redirect(Call("GET", "/some-url"))
    }
  }

  "not call transformer when the answer does not change" in {
    when(userAnswers.hasAnswerChanged(NewAuthYesNoPage, false)).thenReturn(false)
    when(userAnswers.set(NewAuthYesNoPage, false)).thenReturn(Success(userAnswers))
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
    when(redirectCall(any())).thenReturn(Call("GET", "/some-url"))

    service.submitNewAuth(false, userAnswers, redirectCall).map {
      result =>
        verify(mockTransformer, never()).transform(any())
        verify(mockSessionRepository).set(userAnswers)
        result mustBe Results.Redirect(Call("GET", "/some-url"))
    }
  }

  "update answer and not transform data when answer changes to false" in {
    when(userAnswers.hasAnswerChanged(NewAuthYesNoPage, false)).thenReturn(true)
    when(userAnswers.set(NewAuthYesNoPage, false)).thenReturn(Success(userAnswers))
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
    when(redirectCall(any())).thenReturn(Call("GET", "/some-url"))

    service.submitNewAuth(false, userAnswers, redirectCall).map {
      result =>
        verify(mockTransformer, never()).transform(any())
        verify(mockSessionRepository).set(userAnswers)
        result mustBe Results.Redirect(Call("GET", "/some-url"))
    }
  }
}
