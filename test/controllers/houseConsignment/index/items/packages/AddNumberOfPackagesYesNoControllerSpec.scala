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
import forms.YesNoFormProvider
import models.NormalMode
import models.reference.PackageType
import navigation.houseConsignment.index.items.PackagesNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.houseConsignment.index.items.packages.{AddNumberOfPackagesYesNoPage, PackageTypePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.houseConsignment.index.items.packages.AddNumberOfPackagesYesNoView

import scala.concurrent.Future

class AddNumberOfPackagesYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("houseConsignment.index.items.packages.addNumberOfPackagesYesNo")
  private val mode         = NormalMode

  private val packageType = PackageType("1A", "Drum, steel")

  private lazy val addNumberOfPackagesYesNoRoute =
    routes.AddNumberOfPackagesYesNoController.onPageLoad(arrivalId, mode, houseConsignmentIndex, itemIndex, packageIndex).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind(classOf[PackagesNavigator]).toInstance(FakeConsignmentItemNavigators.fakePackagesNavigator)
      )

  "AddNumberOfPackagesYesNoController" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(
        emptyUserAnswers
          .setValue(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageType)
      )

      val request = FakeRequest(GET, addNumberOfPackagesYesNoRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[AddNumberOfPackagesYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, houseConsignmentIndex, itemIndex, packageIndex, packageType.toString, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .setValue(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageType)
        .setValue(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), true)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, addNumberOfPackagesYesNoRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "true"))

      val view = injector.instanceOf[AddNumberOfPackagesYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, houseConsignmentIndex, itemIndex, packageIndex, packageType.toString, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(
        emptyUserAnswers
          .setValue(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageType)
      )

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, addNumberOfPackagesYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(
        emptyUserAnswers.setValue(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageType)
      )

      val invalidAnswer = ""

      val request    = FakeRequest(POST, addNumberOfPackagesYesNoRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[AddNumberOfPackagesYesNoView]

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, houseConsignmentIndex, itemIndex, packageIndex, packageType.toString, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addNumberOfPackagesYesNoRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addNumberOfPackagesYesNoRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
