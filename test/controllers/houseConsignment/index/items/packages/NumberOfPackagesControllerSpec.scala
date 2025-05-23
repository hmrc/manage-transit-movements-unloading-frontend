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

package controllers.houseConsignment.index.items.packages

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.NumberOfPackagesFormProvider
import generators.Generators
import models.NormalMode
import navigation.houseConsignment.index.items.PackagesNavigator.PackagesNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.houseConsignment.index.items.packages.NumberOfPackagesPage
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.houseConsignment.index.items.packages.NumberOfPackagesViewModel
import viewModels.houseConsignment.index.items.packages.NumberOfPackagesViewModel.NumberOfPackagesViewModelProvider
import views.html.houseConsignment.index.items.packages.NumberOfPackagesView

import scala.concurrent.Future

class NumberOfPackagesControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {
  val prefix = "houseConsignment.index.item.numberOfPackages"

  private val mockViewModelProvider = mock[NumberOfPackagesViewModelProvider]
  private val viewModel             = arbitrary[NumberOfPackagesViewModel].sample.value

  private val houseConsignmentMode = NormalMode
  private val itemMode             = NormalMode
  private val packageMode          = NormalMode

  val form: Form[BigInt] = new NumberOfPackagesFormProvider()(prefix, BigInt(0), Seq("2", "test"))

  private val validAnswer = "1"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind(classOf[PackagesNavigatorProvider]).toInstance(FakeConsignmentItemNavigators.fakePackagesNavigatorProvider),
        bind[NumberOfPackagesViewModelProvider].toInstance(mockViewModelProvider)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)

    when(mockViewModelProvider.apply(any(), any(), any())(any()))
      .thenReturn(viewModel)
  }

  lazy val totalNumberOfPackagesRoute: String =
    routes.NumberOfPackagesController.onPageLoad(arrivalId, hcIndex, itemIndex, index, houseConsignmentMode, itemMode, packageMode).url

  "TotalNumberOfPackages Controller" - {

    "must return OK and the correct view for a GET" in {
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, totalNumberOfPackagesRoute)
      val result  = route(app, request).value
      val view    = injector.instanceOf[NumberOfPackagesView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, arrivalId, mrn, hcIndex, itemIndex, index, houseConsignmentMode, itemMode, packageMode, viewModel)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers.setValue(NumberOfPackagesPage(hcIndex, itemIndex, index), BigInt(validAnswer))

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, totalNumberOfPackagesRoute)
      val result  = route(app, request).value

      status(result) mustEqual OK

      val filledForm = form.bind(Map("value" -> validAnswer))
      val view       = injector.instanceOf[NumberOfPackagesView]

      contentAsString(result) mustEqual
        view(filledForm, arrivalId, mrn, hcIndex, itemIndex, index, houseConsignmentMode, itemMode, packageMode, viewModel)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, totalNumberOfPackagesRoute)
          .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, totalNumberOfPackagesRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result    = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[NumberOfPackagesView]

      contentAsString(result) mustEqual
        view(boundForm, arrivalId, mrn, hcIndex, itemIndex, index, houseConsignmentMode, itemMode, packageMode, viewModel)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, totalNumberOfPackagesRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, totalNumberOfPackagesRoute)
          .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
