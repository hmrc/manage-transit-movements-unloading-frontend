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
import navigation.houseConsignment.index.items.PackagesNavigator.PackagesNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.houseConsignment.index.items.packages.AddPackageShippingMarkYesNoPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.houseConsignment.index.items.packages.AddPackageShippingMarkYesNoView

import scala.concurrent.Future

class AddPackageShippingMarkYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("houseConsignment.index.items.packages.addPackageShippingMarkYesNo")

  private val houseConsignmentMode = NormalMode
  private val itemMode             = NormalMode
  private val packageMode          = NormalMode

  private lazy val addPackageShippingMarkYesNoRoute =
    routes.AddPackageShippingMarkYesNoController
      .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
      .url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind(classOf[PackagesNavigatorProvider]).toInstance(FakeConsignmentItemNavigators.fakePackagesNavigatorProvider)
      )

  "PackageShippingMarkYesNoController" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, addPackageShippingMarkYesNoRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[AddPackageShippingMarkYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), true)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, addPackageShippingMarkYesNoRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "true"))

      val view = injector.instanceOf[AddPackageShippingMarkYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)(request,
                                                                                                                                      messages
        ).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, addPackageShippingMarkYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, addPackageShippingMarkYesNoRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[AddPackageShippingMarkYesNoView]

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)(request,
                                                                                                                                      messages
        ).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addPackageShippingMarkYesNoRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addPackageShippingMarkYesNoRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
