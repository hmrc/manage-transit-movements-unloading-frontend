/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.routes
import forms.AddAnotherFormProvider
import generators.Generators
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.ListItem
import viewModels.houseConsignment.index.items.packages.AddAnotherPackageViewModel
import viewModels.houseConsignment.index.items.packages.AddAnotherPackageViewModel.AddAnotherPackageViewModelProvider
import views.html.houseConsignment.index.items.packages.AddAnotherPackageView

class AddAnotherPackageControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar with Generators {

  private val formProvider = new AddAnotherFormProvider()

  private def form(viewModel: AddAnotherPackageViewModel) =
    formProvider(viewModel.prefix, viewModel.allowMore, viewModel.count, itemIndex.display, houseConsignmentIndex.display)

  private val mode = NormalMode

  private lazy val addAnotherPackageRoute =
    controllers.houseConsignment.index.items.packages.routes.AddAnotherPackageController
      .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode)
      .url

  private val mockViewModelProvider = mock[AddAnotherPackageViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[AddAnotherPackageViewModelProvider]).toInstance(mockViewModelProvider))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
  }

  private val listItem          = arbitrary[ListItem].sample.value
  private val listItems         = Seq.fill(Gen.choose(1, frontendAppConfig.maxPackages - 1).sample.value)(listItem)
  private val maxedOutListItems = Seq.fill(frontendAppConfig.maxPackages)(listItem)

  private val viewModel = arbitrary[AddAnotherPackageViewModel].sample.value

  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  "AddAnotherPackageController" - {
    "must return OK and the correct view for a GET" - {
      "when max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any(), any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherPackageRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherPackageView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(notMaxedOutViewModel), mrn, arrivalId, houseConsignmentIndex, itemIndex, notMaxedOutViewModel)(request,
                                                                                                                   messages,
                                                                                                                   frontendAppConfig
          ).toString
      }

      "when max limit reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any(), any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherPackageRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherPackageView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(maxedOutViewModel), mrn, arrivalId, houseConsignmentIndex, itemIndex, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "when max limit not reached" - {
      "when yes submitted" - {
        "must redirect to package type page at next index" in {
          when(mockViewModelProvider.apply(any(), any(), any(), any(), any()))
            .thenReturn(notMaxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, addAnotherPackageRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.houseConsignment.index.items.packages.routes.PackageTypeController
            .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, notMaxedOutViewModel.nextIndex, mode)
            .url
        }
      }

      "when no submitted" - {
        "must redirect to next page" in {
          when(mockViewModelProvider.apply(any(), any(), any(), any(), any()))
            .thenReturn(notMaxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, addAnotherPackageRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.houseConsignment.index.items.routes.AddAnotherItemController
            .onPageLoad(arrivalId, houseConsignmentIndex, mode)
            .url
        }
      }
    }

    "when max limit reached" - {
      "must redirect to next page" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any(), any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherPackageRoute)
          .withFormUrlEncodedBody(("value", ""))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.houseConsignment.index.items.routes.AddAnotherItemController
          .onPageLoad(arrivalId, houseConsignmentIndex, mode)
          .url
      }
    }

    "must return a Bad Request and errors" - {
      "when invalid data is submitted and max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any(), any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherPackageRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider(notMaxedOutViewModel.prefix,
                                     notMaxedOutViewModel.allowMore,
                                     notMaxedOutViewModel.count,
                                     itemIndex.display,
                                     houseConsignmentIndex.display
        )
          .bind(Map("value" -> ""))

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherPackageView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, mrn, arrivalId, houseConsignmentIndex, itemIndex, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addAnotherPackageRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addAnotherPackageRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
