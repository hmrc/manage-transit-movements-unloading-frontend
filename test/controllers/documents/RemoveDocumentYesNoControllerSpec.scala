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

package controllers.documents

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import generators.Generators
import models.DocType.Support
import models.reference.DocumentType
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.documents.{DocumentReferenceNumberPage, TypePage}
import pages.sections.TransportMeansSection
import pages.sections.documents.DocumentSection
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.documents.RemoveDocumentYesNoView

import scala.concurrent.Future

class RemoveDocumentYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("document.removeDocumentYesNo")
  private val mode         = NormalMode

  private lazy val removeDocumentRoute =
    controllers.documents.routes.RemoveDocumentYesNoController.onPageLoad(arrivalId, mode, documentIndex).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "RemoveDocumentYesNoController Controller" - {

    "must return OK and the correct view for a GET" in {

      forAll(arbitrary[DocumentType], nonEmptyString) {
        (documentType, documentReferenceNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(TypePage(documentIndex), documentType)
            .setValue(DocumentReferenceNumberPage(documentIndex), documentReferenceNumber)

          val insetText = s"${documentType.`type`} - $documentReferenceNumber"

          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, removeDocumentRoute)

          val result = route(app, request).value

          val view = injector.instanceOf[RemoveDocumentYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, mrn, arrivalId, documentIndex, mode, insetText)(request, messages).toString
      }
    }

    "when yes submitted" - {
      "must redirect to add another document and remove document at specified index" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = emptyUserAnswers
          .setValue(TypePage(documentIndex), DocumentType(Support, "code", "desc"))
          .setValue(DocumentReferenceNumberPage(documentIndex), "1234")

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeDocumentRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        // TODO: update once AddAnotherDocumentController implemented
        redirectLocation(result).value mustEqual Call("GET", "/foo").url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(DocumentSection(documentIndex)) mustNot be(defined)
      }
    }

    "when no submitted" - {
      "must redirect to add another document and not remove document at specified index" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        val userAnswers = emptyUserAnswers
          .setValue(TypePage(documentIndex), DocumentType(Support, "code", "desc"))
          .setValue(DocumentReferenceNumberPage(documentIndex), "1234")

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeDocumentRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        // TODO: update once AddAnotherDocumentController implemented
        redirectLocation(result).value mustEqual Call("GET", "/foo").url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(DocumentSection(documentIndex)) must be(defined)
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers.setValue(TransportMeansSection(documentIndex), Json.obj()))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, removeDocumentRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      // TODO: update once AddAnotherDocumentController implemented
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      forAll(arbitrary[DocumentType], nonEmptyString) {
        (documentType, documentReferenceNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(TypePage(documentIndex), documentType)
            .setValue(DocumentReferenceNumberPage(documentIndex), documentReferenceNumber)

          val insetText = s"${documentType.`type`} - $documentReferenceNumber"

          setExistingUserAnswers(userAnswers)

          val invalidAnswer = ""

          val request    = FakeRequest(POST, removeDocumentRoute).withFormUrlEncodedBody(("value", ""))
          val filledForm = form.bind(Map("value" -> invalidAnswer))

          val result = route(app, request).value

          status(result) mustEqual BAD_REQUEST

          val view = injector.instanceOf[RemoveDocumentYesNoView]

          contentAsString(result) mustEqual
            view(filledForm, mrn, arrivalId, documentIndex, mode, insetText)(request, messages).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, removeDocumentRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, removeDocumentRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
