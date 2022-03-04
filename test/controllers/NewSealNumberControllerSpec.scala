/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.NewSealNumberFormProvider
import matchers.JsonMatchers
import models.{Index, MovementReferenceNumber, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import pages.NewSealNumberPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.UnloadingPermissionService
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class NewSealNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  val formProvider = new NewSealNumberFormProvider()
  val form         = formProvider()
  val index        = Index(0)

  lazy val newSealNumberRoute = routes.NewSealNumberController.onPageLoad(arrivalId, index, NormalMode).url

  private def mockUnloadingPermissionService = mock[UnloadingPermissionService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUnloadingPermissionService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingPermissionService].toInstance(mockUnloadingPermissionService))

  "NewSealNumber Controller" - {

    "must return OK and the correct view for a GET" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(GET, newSealNumberRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"      -> form,
        "mrn"       -> mrn,
        "arrivalId" -> arrivalId,
        "mode"      -> NormalMode,
        "index"     -> index.display
      )

      templateCaptor.getValue mustEqual "newSealNumber.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers = emptyUserAnswers.set(NewSealNumberPage(index), "answer").success.value

      setExistingUserAnswers(userAnswers)

      val request        = FakeRequest(GET, newSealNumberRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> "answer"))

      val expectedJson = Json.obj(
        "form"      -> filledForm,
        "mrn"       -> mrn,
        "arrivalId" -> arrivalId,
        "mode"      -> NormalMode,
        "index"     -> index.display
      )

      templateCaptor.getValue mustEqual "newSealNumber.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "onSubmit" - {
      "must redirect to the next page when valid data is submitted" in {
        checkArrivalStatus()
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        when(mockUnloadingPermissionService.convertSeals(any())(any(), any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))

        val userAnswers = emptyUserAnswers.set(NewSealNumberPage(index), "answer").success.value
        setExistingUserAnswers(userAnswers)

        val request =
          FakeRequest(POST, newSealNumberRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        checkArrivalStatus()
        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        when(mockUnloadingPermissionService.convertSeals(any())(any(), any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))

        val userAnswers = emptyUserAnswers.set(NewSealNumberPage(index), "answer").success.value
        setExistingUserAnswers(userAnswers)

        val request        = FakeRequest(POST, newSealNumberRoute).withFormUrlEncodedBody(("value", ""))
        val boundForm      = form.bind(Map("value" -> ""))
        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val expectedJson = Json.obj(
          "form"      -> boundForm,
          "mrn"       -> mrn,
          "arrivalId" -> arrivalId,
          "mode"      -> NormalMode,
          "index"     -> index.display
        )

        templateCaptor.getValue mustEqual "newSealNumber.njk"
        jsonCaptor.getValue must containJson(expectedJson)
      }

      "must redirect to Session Expired for a GET if no existing data is found" in {
        checkArrivalStatus()
        setNoExistingUserAnswers()

        val request = FakeRequest(GET, newSealNumberRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
      }

      "must redirect to Session Expired for a POST if no existing data is found" in {
        checkArrivalStatus()
        setNoExistingUserAnswers()

        val request =
          FakeRequest(POST, newSealNumberRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
      }

      "must redirect to the correct page when seals already in the UserAnswers" in {
        checkArrivalStatus()
        val userAnswers = UserAnswers(arrivalId, mrn, eoriNumber, Json.obj("seals" -> Seq("Seals01")))

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        setExistingUserAnswers(userAnswers)

        val request =
          FakeRequest(POST, routes.NewSealNumberController.onPageLoad(arrivalId, Index(1), NormalMode).url)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }

      "redirect to error page when no UserAnswers returned from unloading permissions service" ignore {
        checkArrivalStatus()
        val ua = UserAnswers(arrivalId, MovementReferenceNumber("41", "IT", "0211001000782"), eoriNumber, Json.obj())
        setExistingUserAnswers(ua)

        val request =
          FakeRequest(POST, routes.NewSealNumberController.onPageLoad(arrivalId, Index(0), NormalMode).url)
            .withFormUrlEncodedBody(("value", "answer"))

        route(app, request).value

        //todo: Test this
      }
    }
  }
}
