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
import forms.SelectableFormProvider.DocumentTypeFormProvider
import generators.Generators
import models.reference.DocumentType
import models.{CheckMode, Index, Mode, NormalMode, SelectableList}
import navigation.DocumentNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.documents.TypePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DocumentsService
import viewModels.documents.TypeViewModel
import viewModels.documents.TypeViewModel.TypeViewModelProvider
import views.html.documents.TypeView

import scala.concurrent.Future

class TypeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val document1              = arbitrary[DocumentType](arbitraryTransportDocument).sample.value
  private val document2              = arbitrary[DocumentType](arbitraryTransportDocument).sample.value
  private val document3              = arbitrary[DocumentType](arbitrarySupportDocument).sample.value
  private val document4              = arbitrary[DocumentType](arbitrarySupportDocument).sample.value
  private val transportDocumentList  = SelectableList(Seq(document1, document2))
  private val supportingDocumentList = SelectableList(Seq(document3, document4))
  private val documentsList          = SelectableList(transportDocumentList.values ++ supportingDocumentList.values)

  private lazy val formProvider = new DocumentTypeFormProvider()
  private val field             = formProvider.field

  private val viewModel             = arbitrary[TypeViewModel].sample.value
  private val mockViewModelProvider = mock[TypeViewModelProvider]

  private val mockDocumentTypesService: DocumentsService = mock[DocumentsService]
  private def typeRoute(mode: Mode)                      = routes.TypeController.onPageLoad(arrivalId, mode, documentIndex).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)

    when(mockViewModelProvider.apply(any(), any())(any()))
      .thenReturn(viewModel)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind(classOf[DocumentNavigator]).toInstance(FakeConsignmentNavigators.fakeDocumentNavigator),
        bind(classOf[DocumentsService]).toInstance(mockDocumentTypesService),
        bind[TypeViewModelProvider].toInstance(mockViewModelProvider)
      )

  "Type Controller" - {

    "CheckMode" - {
      "when transport document type selected" - {
        val form = formProvider(CheckMode, "document.type", transportDocumentList)

        "must populate the view correctly on a GET when the question has previously been answered" in {

          when(mockDocumentTypesService.getDocumentList(any(), any(), any())(any())).thenReturn(Future.successful(transportDocumentList))
          val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), document1)
          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, typeRoute(CheckMode))

          val result = route(app, request).value

          val filledForm = form.bind(Map(field -> document1.value))

          val view = injector.instanceOf[TypeView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(filledForm, mrn, arrivalId, CheckMode, transportDocumentList.values, viewModel, documentIndex)(request, messages, frontendAppConfig).toString
        }

        "must redirect to the next page when valid data is submitted" in {

          when(mockDocumentTypesService.getDocumentList(any(), any(), any())(any())).thenReturn(Future.successful(transportDocumentList))
          when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

          val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), document1)
          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(POST, typeRoute(CheckMode))
            .withFormUrlEncodedBody((field, document1.value))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          when(mockDocumentTypesService.getDocumentList(any(), any(), any())(any())).thenReturn(Future.successful(transportDocumentList))
          val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), document1)
          setExistingUserAnswers(userAnswers)

          val request   = FakeRequest(POST, typeRoute(CheckMode)).withFormUrlEncodedBody((field, "invalid value"))
          val boundForm = form.bind(Map(field -> "invalid value"))

          val result = route(app, request).value

          val view = injector.instanceOf[TypeView]

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(boundForm, mrn, arrivalId, CheckMode, transportDocumentList.values, viewModel, documentIndex)(request, messages, frontendAppConfig).toString
        }

        "must redirect to Session Expired for a GET if no existing data is found" in {

          setNoExistingUserAnswers()

          val request = FakeRequest(GET, typeRoute(CheckMode))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
        }

        "must redirect to Session Expired for a POST if no existing data is found" in {

          setNoExistingUserAnswers()

          val request = FakeRequest(POST, typeRoute(CheckMode))
            .withFormUrlEncodedBody((field, document1.code))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
        }
      }

      "when supporting document type selected" - {
        val form = formProvider(CheckMode, "document.type", supportingDocumentList)

        "must populate the view correctly on a GET when the question has previously been answered" in {

          when(mockDocumentTypesService.getDocumentList(any(), any(), any())(any())).thenReturn(Future.successful(supportingDocumentList))
          val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), document3)
          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, typeRoute(CheckMode))

          val result = route(app, request).value

          val filledForm = form.bind(Map(field -> document3.value))

          val view = injector.instanceOf[TypeView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(filledForm, mrn, arrivalId, CheckMode, supportingDocumentList.values, viewModel, documentIndex)(request, messages, frontendAppConfig).toString
        }

        "must redirect to the next page when valid data is submitted" in {

          when(mockDocumentTypesService.getDocumentList(any(), any(), any())(any())).thenReturn(Future.successful(supportingDocumentList))
          when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

          val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), document3)
          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(POST, typeRoute(CheckMode))
            .withFormUrlEncodedBody((field, document4.value))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          when(mockDocumentTypesService.getDocumentList(any(), any(), any())(any())).thenReturn(Future.successful(supportingDocumentList))
          val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), document3)
          setExistingUserAnswers(userAnswers)

          val request   = FakeRequest(POST, typeRoute(CheckMode)).withFormUrlEncodedBody((field, "invalid value"))
          val boundForm = form.bind(Map(field -> "invalid value"))

          val result = route(app, request).value

          val view = injector.instanceOf[TypeView]

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(boundForm, mrn, arrivalId, CheckMode, supportingDocumentList.values, viewModel, documentIndex)(request, messages, frontendAppConfig).toString
        }

        "must redirect to Session Expired for a GET if no existing data is found" in {

          setNoExistingUserAnswers()

          val request = FakeRequest(GET, typeRoute(CheckMode))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
        }

        "must redirect to Session Expired for a POST if no existing data is found" in {

          setNoExistingUserAnswers()

          val request = FakeRequest(POST, typeRoute(CheckMode))
            .withFormUrlEncodedBody((field, document1.code))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
        }
      }
    }

    "NormalMode" - {
      val form = formProvider(NormalMode, "document.type", documentsList)

      "must return OK and the correct view for a GET" in {

        when(mockDocumentTypesService.getDocumentList(any(), any(), any())(any())).thenReturn(Future.successful(documentsList))
        val userAnswers = emptyUserAnswers
          .setValue(TypePage(Index(0)), document1)
          .setValue(TypePage(Index(1)), document2)
          .setValue(TypePage(Index(2)), document3)
          .setValue(TypePage(Index(3)), document4)
        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, typeRoute(NormalMode))

        val result = route(app, request).value

        val filledForm = form.bind(Map(field -> document1.value))

        val view = injector.instanceOf[TypeView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, mrn, arrivalId, NormalMode, documentsList.values, viewModel, documentIndex)(request, messages, frontendAppConfig).toString
      }

      "must filter out maxed document types that cannot be added any more" in {

        when(mockDocumentTypesService.getDocumentList(any(), any(), any())(any())).thenReturn(Future.successful(documentsList))
        val maxTransportLimit = frontendAppConfig.maxTransportDocumentsConsignment
        val supportDocsSize   = 2

        val withTransportDocs = (0 until maxTransportLimit).foldLeft(emptyUserAnswers) {
          (answers, index) => answers.setValue(TypePage(Index(index)), document1)
        }

        val withBothDocs = (maxTransportLimit until maxTransportLimit + supportDocsSize).foldLeft(withTransportDocs) {
          (answers, index) => answers.setValue(TypePage(Index(index)), document3)
        }

        setExistingUserAnswers(withBothDocs)

        val nextIndex = Index(maxTransportLimit + supportDocsSize)

        val request = FakeRequest(GET, routes.TypeController.onPageLoad(arrivalId, NormalMode, nextIndex).url)

        val result = route(app, request).value

        val filledForm = form

        val view = injector.instanceOf[TypeView]

        status(result) mustEqual OK

        // view should have only support docs listed. new transport docs cannot be added since they reached the limit
        contentAsString(result) mustEqual
          view(filledForm, mrn, arrivalId, NormalMode, supportingDocumentList.values, viewModel, nextIndex)(request, messages, frontendAppConfig).toString
      }

      "must redirect to the next page when valid data is submitted" in {

        when(mockDocumentTypesService.getDocumentList(any(), any(), any())(any())).thenReturn(Future.successful(transportDocumentList))
        when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

        val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), document1)
        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, typeRoute(NormalMode))
          .withFormUrlEncodedBody((field, document1.value))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        when(mockDocumentTypesService.getDocumentList(any(), any(), any())(any())).thenReturn(Future.successful(transportDocumentList))
        val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), document1)
        setExistingUserAnswers(userAnswers)

        val request   = FakeRequest(POST, typeRoute(NormalMode)).withFormUrlEncodedBody((field, "invalid value"))
        val boundForm = form.bind(Map(field -> "invalid value"))

        val result = route(app, request).value

        val view = injector.instanceOf[TypeView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, mrn, arrivalId, NormalMode, transportDocumentList.values, viewModel, documentIndex)(request, messages, frontendAppConfig).toString
      }

      "must redirect to Session Expired for a GET if no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(GET, typeRoute(NormalMode))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
      }

      "must redirect to Session Expired for a POST if no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(POST, typeRoute(NormalMode))
          .withFormUrlEncodedBody((field, document1.code))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
      }
    }
  }
}
