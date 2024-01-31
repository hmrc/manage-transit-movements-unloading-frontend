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

package controllers.houseConsignment.index.items

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import forms.CUSCodeFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.houseConsignment.index.items.CustomsUnionAndStatisticsCodePage
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.houseConsignment.index.items.CustomsUnionAndStatisticsCodeView

import scala.concurrent.Future

class CustomsUnionAndStatisticsCodeControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider = new CUSCodeFormProvider()
  private val form         = formProvider("houseConsignment.item.customsUnionAndStatisticsCode", itemIndex.display, houseConsignmentIndex.display)
  private val mode         = NormalMode

  private lazy val customsUnionAndStatisticsCodeRoute =
    controllers.houseConsignment.index.items.routes.CustomsUnionAndStatisticsCodeController.onPageLoad(arrivalId, mode, houseConsignmentIndex, itemIndex).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "CustomsUnionAndStatisticsCode Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, customsUnionAndStatisticsCodeRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[CustomsUnionAndStatisticsCodeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, mode, houseConsignmentIndex, itemIndex)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex), "validCode")
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, customsUnionAndStatisticsCodeRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "validCode"))

      val view = injector.instanceOf[CustomsUnionAndStatisticsCodeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, mode, houseConsignmentIndex, itemIndex)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, customsUnionAndStatisticsCodeRoute)
        .withFormUrlEncodedBody(("value", "validCode"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, customsUnionAndStatisticsCodeRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[CustomsUnionAndStatisticsCodeView]

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, mode, houseConsignmentIndex, itemIndex)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, customsUnionAndStatisticsCodeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, customsUnionAndStatisticsCodeRoute)
        .withFormUrlEncodedBody(("value", "validCode"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
