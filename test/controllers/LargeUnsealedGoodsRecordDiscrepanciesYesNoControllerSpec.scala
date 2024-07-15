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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import models.{NormalMode, UserAnswers}
import navigation.Navigation
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import pages.LargeUnsealedGoodsRecordDiscrepanciesYesNoPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.transformers.IE043Transformer
import views.html.LargeUnsealedGoodsRecordDiscrepanciesYesNoView
import scala.concurrent.Future

class LargeUnsealedGoodsRecordDiscrepanciesYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("largeUnsealedGoodsRecordDiscrepanciesYesNo")
  private val mode         = NormalMode

  private lazy val largeUnsealedGoodsRecordDiscrepanciesRoute =
    controllers.routes.LargeUnsealedGoodsRecordDiscrepanciesYesNoController.onPageLoad(arrivalId, mode).url

  private val mockTransformer = mock[IE043Transformer]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[Navigation].toInstance(fakeNavigation),
        bind[IE043Transformer].toInstance(mockTransformer)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockTransformer)
  }

  "LargeUnsealedGoodsRecordDiscrepanciesYesNoPage Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, largeUnsealedGoodsRecordDiscrepanciesRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[LargeUnsealedGoodsRecordDiscrepanciesYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage, true)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, largeUnsealedGoodsRecordDiscrepanciesRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "true"))

      val view = injector.instanceOf[LargeUnsealedGoodsRecordDiscrepanciesYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, mode)(request, messages).toString
    }

    "must redirect to the next page when Yes is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, largeUnsealedGoodsRecordDiscrepanciesRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      verifyNoInteractions(mockTransformer)
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, largeUnsealedGoodsRecordDiscrepanciesRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[LargeUnsealedGoodsRecordDiscrepanciesYesNoView]

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, largeUnsealedGoodsRecordDiscrepanciesRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, largeUnsealedGoodsRecordDiscrepanciesRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
