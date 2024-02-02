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

package controllers.houseConsignment.index.items

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.SelectableFormProvider
import generators.Generators
import models.{NormalMode, SelectableList}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.houseConsignment.index.items.PackageTypePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.PackagesService
import viewModels.houseConsignment.index.items.PackageTypeViewModel
import views.html.houseConsignment.index.items.PackageTypeView

import scala.concurrent.Future

class PackageTypeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val packageType1    = arbitraryPackageType.arbitrary.sample.get
  private val packageType2    = arbitraryPackageType.arbitrary.sample.get
  private val packageTypeList = SelectableList(Seq(packageType1, packageType2))

  private val mockPreviousDocumentService: PackagesService = mock[PackagesService]

  private lazy val packageTypeRoute =
    controllers.houseConsignment.index.items.routes.PackageTypeController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[PackagesService]).toInstance(mockPreviousDocumentService))

  private val formProvider = new SelectableFormProvider()
  private val mode         = NormalMode

  private val form                    = formProvider(mode, houseConsignmentIndex, itemIndex, "houseConsignment.index.item.packageType", packageTypeList)
  val viewModel: PackageTypeViewModel = PackageTypeViewModel(mode, itemIndex, houseConsignmentIndex)

  "PackageType Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockPreviousDocumentService.getPackageTypes()(any())).thenReturn(Future.successful(packageTypeList))
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, packageTypeRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[PackageTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(viewModel, form, arrivalId, packageTypeList.values, mode, houseConsignmentIndex, itemIndex, packageIndex)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockPreviousDocumentService.getPackageTypes()(any())).thenReturn(Future.successful(packageTypeList))
      val userAnswers = emptyUserAnswers.setValue(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageType1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, packageTypeRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> packageType1.code))

      val view = injector.instanceOf[PackageTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(viewModel, filledForm, arrivalId, packageTypeList.values, mode, houseConsignmentIndex, itemIndex, packageIndex)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockPreviousDocumentService.getPackageTypes()(any())).thenReturn(Future.successful(packageTypeList))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, packageTypeRoute)
        .withFormUrlEncodedBody(("value", packageType1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockPreviousDocumentService.getPackageTypes()(any())).thenReturn(Future.successful(packageTypeList))
      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, packageTypeRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[PackageTypeView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(viewModel, boundForm, arrivalId, packageTypeList.values, mode, houseConsignmentIndex, itemIndex, packageIndex)(request, messages).toString
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
        .withFormUrlEncodedBody(("value", packageType1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
