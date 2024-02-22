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
import forms.SelectableFormProvider
import generators.Generators
import models.reference.DocumentType
import models.{CheckMode, SelectableList}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import pages.documents.TypePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DocumentsService
import views.html.documents.TypeView

import scala.concurrent.Future

class TypeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val document1              = arbitrary[DocumentType](arbitraryTransportDocument).sample.value
  private val document2              = arbitrary[DocumentType](arbitraryTransportDocument).sample.value
  private val document3              = arbitrary[DocumentType](arbitrarySupportDocument).sample.value
  private val document4              = arbitrary[DocumentType](arbitrarySupportDocument).sample.value
  private val transportDocumentList  = SelectableList(Seq(document1, document2))
  private val supportingDocumentList = SelectableList(Seq(document3, document4))

  private lazy val formProvider = new SelectableFormProvider()
  private val mode              = CheckMode

  private val mockDocumentTypesService: DocumentsService = mock[DocumentsService]
  private lazy val typeRoute                             = routes.TypeController.onPageLoad(arrivalId, mode, documentIndex).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[DocumentsService]).toInstance(mockDocumentTypesService))

  "Type Controller" - {

    "CheckMode" - {
      "when transport document type selected" - {
        val form = formProvider(mode, "document.type", transportDocumentList)

        "must populate the view correctly on a GET when the question has previously been answered" in {

          when(mockDocumentTypesService.getTransportDocuments()(any())).thenReturn(Future.successful(transportDocumentList))
          val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), document1)
          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, typeRoute)

          val result = route(app, request).value

          val filledForm = form.bind(Map("value" -> document1.value))

          val view = injector.instanceOf[TypeView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(filledForm, mrn, arrivalId, mode, transportDocumentList.values, documentIndex)(request, messages).toString
        }

        "must redirect to the next page when valid data is submitted" in {

          when(mockDocumentTypesService.getTransportDocuments()(any())).thenReturn(Future.successful(transportDocumentList))
          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), document1)
          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(POST, typeRoute)
            .withFormUrlEncodedBody(("value", document1.value))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          when(mockDocumentTypesService.getTransportDocuments()(any())).thenReturn(Future.successful(transportDocumentList))
          val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), document1)
          setExistingUserAnswers(userAnswers)

          val request   = FakeRequest(POST, typeRoute).withFormUrlEncodedBody(("value", "invalid value"))
          val boundForm = form.bind(Map("value" -> "invalid value"))

          val result = route(app, request).value

          val view = injector.instanceOf[TypeView]

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(boundForm, mrn, arrivalId, mode, transportDocumentList.values, documentIndex)(request, messages).toString
        }

        "must redirect to Session Expired for a GET if no existing data is found" in {

          setNoExistingUserAnswers()

          val request = FakeRequest(GET, typeRoute)

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
        }

        "must redirect to Session Expired for a POST if no existing data is found" in {

          setNoExistingUserAnswers()

          val request = FakeRequest(POST, typeRoute)
            .withFormUrlEncodedBody(("value", document1.code))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
        }
      }

      "when supporting document type selected" - {
        val form = formProvider(mode, "document.type", supportingDocumentList)

        "must populate the view correctly on a GET when the question has previously been answered" in {

          when(mockDocumentTypesService.getSupportingDocuments()(any())).thenReturn(Future.successful(supportingDocumentList))
          val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), document3)
          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, typeRoute)

          val result = route(app, request).value

          val filledForm = form.bind(Map("value" -> document3.value))

          val view = injector.instanceOf[TypeView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(filledForm, mrn, arrivalId, mode, supportingDocumentList.values, documentIndex)(request, messages).toString
        }

        "must redirect to the next page when valid data is submitted" in {

          when(mockDocumentTypesService.getSupportingDocuments()(any())).thenReturn(Future.successful(supportingDocumentList))
          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), document3)
          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(POST, typeRoute)
            .withFormUrlEncodedBody(("value", document4.value))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          when(mockDocumentTypesService.getSupportingDocuments()(any())).thenReturn(Future.successful(supportingDocumentList))
          val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), document3)
          setExistingUserAnswers(userAnswers)

          val request   = FakeRequest(POST, typeRoute).withFormUrlEncodedBody(("value", "invalid value"))
          val boundForm = form.bind(Map("value" -> "invalid value"))

          val result = route(app, request).value

          val view = injector.instanceOf[TypeView]

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(boundForm, mrn, arrivalId, mode, supportingDocumentList.values, documentIndex)(request, messages).toString
        }

        "must redirect to Session Expired for a GET if no existing data is found" in {

          setNoExistingUserAnswers()

          val request = FakeRequest(GET, typeRoute)

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
        }

        "must redirect to Session Expired for a POST if no existing data is found" in {

          setNoExistingUserAnswers()

          val request = FakeRequest(POST, typeRoute)
            .withFormUrlEncodedBody(("value", document1.code))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
        }
      }
    }
  }
}
