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
import forms.TotalNumberOfPackagesFormProvider
import models.ErrorType.IncorrectValue
import models.{DefaultPointer, FunctionalError, UnloadingRemarksRejectionMessage}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import pages.TotalNumberOfPackagesPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UnloadingRemarksRejectionService
import views.html.TotalNumberOfPackagesRejectionView

import java.time.LocalDate
import scala.concurrent.Future

class TotalNumberOfPackagesRejectionControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider                    = new TotalNumberOfPackagesFormProvider()
  private val form                            = formProvider()
  private val validAnswer                     = 1
  private val mockRejectionService            = mock[UnloadingRemarksRejectionService]
  private lazy val totalNumberOfPackagesRoute = routes.TotalNumberOfPackagesRejectionController.onPageLoad(arrivalId).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRejectionService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService))

  "TotalNumberOfPackages Rejection Controller" - {

    "must populate the view correctly on a GET when the question has previously been answered" in {
      checkArrivalStatus()

      when(mockRejectionService.getRejectedValueAsInt(any(), any())(any())(any())).thenReturn(Future.successful(Some(validAnswer)))

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, totalNumberOfPackagesRoute)
      val view    = injector.instanceOf[TotalNumberOfPackagesRejectionView]
      val result  = route(app, request).value

      status(result) mustEqual OK

      val filledForm = form.bind(Map("value" -> validAnswer.toString))

      contentAsString(result) mustEqual
        view(filledForm, arrivalId)(request, messages).toString
    }

    "must render the Technical Difficulties page when get rejected value is None" in {
      checkArrivalStatus()
      when(mockRejectionService.getRejectedValueAsInt(any(), any())(any())(any())).thenReturn(Future.successful(None))

      val userAnswers = emptyUserAnswers.set(TotalNumberOfPackagesPage, validAnswer).success.value
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, totalNumberOfPackagesRoute)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()
      val originalValue    = "some reference"
      val errors           = Seq(FunctionalError(IncorrectValue, DefaultPointer(""), None, Some(originalValue)))
      val rejectionMessage = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)

      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any())).thenReturn(Future.successful(Some(rejectionMessage)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, totalNumberOfPackagesRoute)
        .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId).url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, totalNumberOfPackagesRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result    = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[TotalNumberOfPackagesRejectionView]

      contentAsString(result) mustEqual view(boundForm, arrivalId)(request, messages).toString
    }

    "must render the Technical Difficulties page when there is no rejection message on submission" in {
      checkArrivalStatus()
      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any())).thenReturn(Future.successful(None))

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, totalNumberOfPackagesRoute)
        .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url
    }
  }
}
