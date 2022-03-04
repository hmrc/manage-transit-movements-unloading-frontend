package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.$className$FormProvider
import matchers.JsonMatchers
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.$className$Page
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.Future

class $className$ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  val formProvider = new $className$FormProvider()
  val form = formProvider()

  lazy val $className;format="decap"$Route = routes.$className$Controller.onPageLoad(arrivalId, NormalMode).url

  "$className$ Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, $className;format="decap"$Route)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"   -> form,
        "mode"   -> NormalMode,
        "mrn"    -> mrn,
        "arrivalId" -> arrivalId,
        "radios" -> Radios.yesNo(form("value"))
      )

      templateCaptor.getValue mustEqual "$className;format="decap"$.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers = UserAnswers(arrivalId, mrn, eoriNumber).set($className$Page, true).success.value
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, $className;format="decap"$Route)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> "true"))

      val expectedJson = Json.obj(
        "form"   -> filledForm,
        "mode"   -> NormalMode,
        "mrn"    -> mrn,
        "arrivalId" -> arrivalId,
        "radios" -> Radios.yesNo(form("value"))
      )

      templateCaptor.getValue mustEqual "$className;format="decap"$.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, $className;format="decap"$Route)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, $className;format="decap"$Route).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"   -> boundForm,
        "mode"   -> NormalMode,
        "mrn"    -> mrn,
        "arrivalId" -> arrivalId,
        "radios" -> Radios.yesNo(form("value"))
      )

      templateCaptor.getValue mustEqual "$className;format="decap"$.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, $className;format="decap"$Route)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, $className;format="decap"$Route)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
