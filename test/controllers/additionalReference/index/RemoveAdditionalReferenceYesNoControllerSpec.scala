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

package controllers.additionalReference.index

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import generators.Generators
import models.reference.AdditionalReferenceType
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Gen
import pages.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import pages.sections.additionalReference.AdditionalReferenceSection
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpVerbs.GET
import views.html.additionalReference.index.RemoveAdditionalReferenceYesNoView

import scala.concurrent.Future

class RemoveAdditionalReferenceYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("additionalReference.index.removeAdditionalReferenceYesNo", additionalReferenceIndex.display)
  private val mode         = NormalMode

  private lazy val removeAdditionalReferenceRoute =
    controllers.additionalReference.index.routes.RemoveAdditionalReferenceYesNoController.onPageLoad(arrivalId, additionalReferenceIndex, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "RemoveAdditionalReferenceYesNoController Controller" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers
        .setValue(AdditionalReferenceTypePage(additionalReferenceIndex), AdditionalReferenceType("Y015", "The rough diamonds are contained in ..."))
        .setValue(AdditionalReferenceNumberPage(additionalReferenceIndex), "addref-1")

      setExistingUserAnswers(userAnswers)
      val insetText = Some("Y015 - addref-1")

      val request = FakeRequest(GET, removeAdditionalReferenceRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[RemoveAdditionalReferenceYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, additionalReferenceIndex, insetText, mode)(request, messages).toString

    }
    "when yes submitted" - {
      "must redirect to add another Additional Reference and remove AdditionalReference at specified index" ignore {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        val userAnswers = emptyUserAnswers
          .setValue(AdditionalReferenceTypePage(additionalReferenceIndex), AdditionalReferenceType("Y015", "The rough diamonds are contained in ..."))
          .setValue(AdditionalReferenceNumberPage(additionalReferenceIndex), "addref-1")

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeAdditionalReferenceRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.additionalReference.index.routes.AdditionalReferenceNumberController
          .onPageLoad(arrivalId, additionalReferenceIndex, mode)
          .url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(AdditionalReferenceSection(additionalReferenceIndex)) mustNot be(defined)
      }
    }

    "when no submitted" - {
      "must redirect to add another Additional Reference and not remove AdditionalReference at specified index" ignore {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = emptyUserAnswers
          .setValue(AdditionalReferenceTypePage(additionalReferenceIndex), AdditionalReferenceType("Y015", "The rough diamonds are contained in ..."))
          .setValue(AdditionalReferenceNumberPage(additionalReferenceIndex), "addref-1")

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeAdditionalReferenceRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.additionalReference.index.routes.AdditionalReferenceNumberController
          .onPageLoad(arrivalId, additionalReferenceIndex, mode)
          .url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(AdditionalReferenceSection(additionalReferenceIndex)) must be(defined)
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val userAnswers = emptyUserAnswers
        .setValue(AdditionalReferenceTypePage(additionalReferenceIndex), AdditionalReferenceType("Y015", "The rough diamonds are contained in ..."))
        .setValue(AdditionalReferenceNumberPage(additionalReferenceIndex), "addref-1")

      setExistingUserAnswers(userAnswers)
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, removeAdditionalReferenceRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.additionalReference.index.routes.AdditionalReferenceNumberController
        .onPageLoad(arrivalId, additionalReferenceIndex, mode)
        .url

    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      forAll(Gen.alphaNumStr) {
        containerId =>
          val userAnswers = emptyUserAnswers
            .setValue(AdditionalReferenceTypePage(additionalReferenceIndex), AdditionalReferenceType("Y015", "The rough diamonds are contained in ..."))
            .setValue(AdditionalReferenceNumberPage(additionalReferenceIndex), "addref-1")

          setExistingUserAnswers(userAnswers)

          val invalidAnswer = ""
          val request       = FakeRequest(POST, removeAdditionalReferenceRoute).withFormUrlEncodedBody(("value", ""))
          val filledForm    = form.bind(Map("value" -> invalidAnswer))
          val result        = route(app, request).value
          val insetText     = Some("Y015 - addref-1")

          status(result) mustEqual BAD_REQUEST

          val view = injector.instanceOf[RemoveAdditionalReferenceYesNoView]

          contentAsString(result) mustEqual
            view(filledForm, mrn, arrivalId, additionalReferenceIndex, insetText, mode)(request, messages).toString

      }
    }
    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, removeAdditionalReferenceRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, removeAdditionalReferenceRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
