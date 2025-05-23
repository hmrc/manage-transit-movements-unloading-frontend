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
import forms.SelectableFormProvider.PackageTypeFormProvider
import generators.Generators
import models.{NormalMode, SelectableList}
import navigation.houseConsignment.index.items.PackagesNavigator.PackagesNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.houseConsignment.index.items.packages.PackageTypePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ReferenceDataService
import viewModels.houseConsignment.index.items.packages.PackageTypeViewModel
import viewModels.houseConsignment.index.items.packages.PackageTypeViewModel.PackageTypeViewModelProvider
import views.html.houseConsignment.index.items.packages.PackageTypeView

import scala.concurrent.Future

class PackageTypeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val packageType1    = arbitraryPackageType.arbitrary.sample.get
  private val packageType2    = arbitraryPackageType.arbitrary.sample.get
  private val packageTypeList = SelectableList(Seq(packageType1, packageType2))

  private val mockViewModelProvider = mock[PackageTypeViewModelProvider]
  private val viewModel             = arbitrary[PackageTypeViewModel].sample.value

  private val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  private lazy val packageTypeRoute =
    routes.PackageTypeController
      .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
      .url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind(classOf[PackagesNavigatorProvider]).toInstance(FakeConsignmentItemNavigators.fakePackagesNavigatorProvider),
        bind(classOf[PackageTypeViewModelProvider]).toInstance(mockViewModelProvider),
        bind(classOf[ReferenceDataService]).toInstance(mockReferenceDataService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)

    when(mockViewModelProvider.apply(any(), any(), any())(any()))
      .thenReturn(viewModel)
  }

  private val formProvider = new PackageTypeFormProvider()
  private val field        = formProvider.field

  private val houseConsignmentMode = NormalMode
  private val itemMode             = NormalMode
  private val packageMode          = NormalMode

  private val form = formProvider(packageMode, "houseConsignment.index.item.packageType", packageTypeList, itemIndex.display, houseConsignmentIndex.display)

  "PackageType Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockReferenceDataService.getPackageTypes()(any())).thenReturn(Future.successful(packageTypeList))
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, packageTypeRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[PackageTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(
          viewModel,
          form,
          mrn,
          arrivalId,
          packageTypeList.values,
          houseConsignmentMode,
          itemMode,
          packageMode,
          houseConsignmentIndex,
          itemIndex,
          packageIndex
        )(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockReferenceDataService.getPackageTypes()(any())).thenReturn(Future.successful(packageTypeList))
      val userAnswers = emptyUserAnswers.setValue(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageType1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, packageTypeRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map(field -> packageType1.code))

      val view = injector.instanceOf[PackageTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(
          viewModel,
          filledForm,
          mrn,
          arrivalId,
          packageTypeList.values,
          houseConsignmentMode,
          itemMode,
          packageMode,
          houseConsignmentIndex,
          itemIndex,
          packageIndex
        )(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockReferenceDataService.getPackageTypes()(any())).thenReturn(Future.successful(packageTypeList))
      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, packageTypeRoute)
        .withFormUrlEncodedBody((field, packageType1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockReferenceDataService.getPackageTypes()(any())).thenReturn(Future.successful(packageTypeList))

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, packageTypeRoute).withFormUrlEncodedBody((field, "invalid value"))
      val boundForm = form.bind(Map(field -> "invalid value"))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[PackageTypeView]

      contentAsString(result) mustEqual
        view(
          viewModel,
          boundForm,
          mrn,
          arrivalId,
          packageTypeList.values,
          houseConsignmentMode,
          itemMode,
          packageMode,
          houseConsignmentIndex,
          itemIndex,
          packageIndex
        )(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, packageTypeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, packageTypeRoute)
        .withFormUrlEncodedBody((field, packageType1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
