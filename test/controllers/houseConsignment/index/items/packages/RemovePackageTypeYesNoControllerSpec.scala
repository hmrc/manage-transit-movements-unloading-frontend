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
import generators.Generators
import models.reference.PackageType
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.houseConsignment.index.items.packages.{NumberOfPackagesPage, PackageTypePage}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.houseConsignment.index.items.packages.RemovePackageTypeYesNoView

import scala.concurrent.Future

class RemovePackageTypeYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider = new YesNoFormProvider()
  private val packageType  = arbitrary[PackageType].sample.value
  private val form         = formProvider("houseConsignment.index.items.packages.removePackageTypeYesNo", packageType.toString)
  private val mode         = NormalMode

  private lazy val removePackageTypeYesNoRoute =
    controllers.houseConsignment.index.items.packages.routes.RemovePackageTypeYesNoController
      .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode)
      .url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "RemovePackageTypeYesNoController " - {

    "must return OK and the correct view for a GET" in {

      forAll(arbitrary[BigInt]) {
        quantity =>
          val userAnswers = emptyUserAnswers
            .setValue(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), quantity)
            .setValue(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageType)

          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, removePackageTypeYesNoRoute)

          val result = route(app, request).value

          val view = injector.instanceOf[RemovePackageTypeYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, mrn, arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode, Some(s"${quantity.toString} ${packageType.toString}"))(request,
                                                                                                                                                    messages
            ).toString
      }
    }

    "when yes submitted" - {
      "must redirect to addAnotherPackageType page and remove package type at specified index" ignore {
        forAll(arbitrary[BigInt]) {
          quantity =>
            val userAnswers = emptyUserAnswers
              .setValue(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), quantity)
              .setValue(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageType)

            setExistingUserAnswers(userAnswers)
            when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

            val request = FakeRequest(POST, removePackageTypeYesNoRoute)
              .withFormUrlEncodedBody(("value", "true"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual Call("GET", "#").url // TODO should go to addAnother route

            val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(userAnswersCaptor.capture())
            userAnswersCaptor.getValue.get(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex)) mustNot be(defined)
        }

      }
    }

    "when no submitted" - {
      "must redirect to addAnotherPackageType page and not remove package type at specified index" ignore {

        forAll(arbitrary[BigInt]) {
          quantity =>
            val userAnswers = emptyUserAnswers
              .setValue(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), quantity)
              .setValue(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageType)

            setExistingUserAnswers(userAnswers)
            when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

            val request = FakeRequest(POST, removePackageTypeYesNoRoute)
              .withFormUrlEncodedBody(("value", "false"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual Call("GET", "#").url // TODO should go to addAnother route

            val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(userAnswersCaptor.capture())
            userAnswersCaptor.getValue.get(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex)) must be(defined)
        }

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      forAll(arbitrary[BigInt]) {
        quantity =>
          val userAnswers = emptyUserAnswers
            .setValue(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), quantity)
            .setValue(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageType)

          setExistingUserAnswers(userAnswers)

          val invalidAnswer = ""

          val request    = FakeRequest(POST, removePackageTypeYesNoRoute).withFormUrlEncodedBody(("value", ""))
          val filledForm = form.bind(Map("value" -> invalidAnswer))

          val result = route(app, request).value

          status(result) mustEqual BAD_REQUEST

          val view = injector.instanceOf[RemovePackageTypeYesNoView]

          contentAsString(result) mustEqual
            view(filledForm, mrn, arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode, Some(s"${quantity.toString} ${packageType.toString}"))(
              request,
              messages
            ).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, removePackageTypeYesNoRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, removePackageTypeYesNoRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
