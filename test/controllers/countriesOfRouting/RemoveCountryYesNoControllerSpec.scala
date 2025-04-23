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

package controllers.countriesOfRouting

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import generators.Generators
import models.reference.Country
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.countriesOfRouting.CountryOfRoutingPage
import pages.sections.CountryOfRoutingSection
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.countriesOfRouting.RemoveCountryYesNoView

import scala.concurrent.Future

class RemoveCountryYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("countriesOfRouting.removeCountryYesNo", index)
  private val mode         = NormalMode

  private lazy val removeRoute =
    routes.RemoveCountryYesNoController.onPageLoad(arrivalId, mode, index).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure("feature-flags.phase-6-enabled" -> true)

  "RemoveCountryYesNoController" - {

    "must return OK and the correct view for a GET" in {

      forAll(arbitrary[Country]) {
        country =>
          val userAnswers = emptyUserAnswers
            .setValue(CountryOfRoutingPage(index), country)

          val insetText = Some(country.toString)

          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, removeRoute)

          val result = route(app, request).value

          val view = injector.instanceOf[RemoveCountryYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, mrn, arrivalId, index, mode, insetText)(request, messages).toString
      }
    }

    "when yes submitted" - {
      "must redirect to add another country and remove country at specified index" in {
        when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)
        val userAnswers = emptyUserAnswers.setValue(CountryOfRoutingSection(index), Json.obj())

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.AddAnotherCountryController.onPageLoad(arrivalId, mode).url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(CountryOfRoutingSection(index)) mustNot be(defined)
      }
    }

    "when no submitted" - {
      "must redirect to add another country and not remove country at specified index" in {
        when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)
        val userAnswers = emptyUserAnswers.setValue(CountryOfRoutingSection(index), Json.obj())

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.AddAnotherCountryController.onPageLoad(arrivalId, mode).url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(CountryOfRoutingSection(index)) must be(defined)
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers.setValue(CountryOfRoutingSection(index), Json.obj()))

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      val request = FakeRequest(POST, removeRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        routes.AddAnotherCountryController.onPageLoad(arrivalId, mode).url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      forAll(arbitrary[Country]) {
        country =>
          val userAnswers = emptyUserAnswers
            .setValue(CountryOfRoutingPage(index), country)

          val insetText = Some(country.toString)

          setExistingUserAnswers(userAnswers)

          val invalidAnswer = ""

          val request    = FakeRequest(POST, removeRoute).withFormUrlEncodedBody(("value", invalidAnswer))
          val filledForm = form.bind(Map("value" -> invalidAnswer))

          val result = route(app, request).value

          status(result) mustEqual BAD_REQUEST

          val view = injector.instanceOf[RemoveCountryYesNoView]

          contentAsString(result) mustEqual
            view(filledForm, mrn, arrivalId, index, mode, insetText)(request, messages).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, removeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to page not found for a GET if phase 6 is disabled" in {
      val app = super
        .guiceApplicationBuilder()
        .configure("feature-flags.phase-6-enabled" -> false)
        .build()

      running(app) {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, removeRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.ErrorController.notFound().url
      }
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, removeRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to page not found for a POST if phase 6 is disabled" in {
      val app = super
        .guiceApplicationBuilder()
        .configure("feature-flags.phase-6-enabled" -> false)
        .build()

      running(app) {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, removeRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.ErrorController.notFound().url
      }
    }
  }
}
