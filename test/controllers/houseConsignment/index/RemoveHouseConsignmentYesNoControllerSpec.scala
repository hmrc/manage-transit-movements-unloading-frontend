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

package controllers.houseConsignment.index

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import generators.Generators
import models.reference.Country
import models.{Index, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.houseConsignment.index.{CountryOfDestinationPage, GrossWeightPage}
import pages.sections.HouseConsignmentSection
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.houseConsignment.index.RemoveHouseConsignmentYesNoView

import scala.concurrent.Future

class RemoveHouseConsignmentYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider = new YesNoFormProvider()

  private def form(houseConsignmentIndex: Index) =
    formProvider("houseConsignment.index.removeHouseConsignmentYesNo", houseConsignmentIndex.display)
  private val mode = NormalMode

  private lazy val removeHouseConsignmentRoute =
    controllers.houseConsignment.index.routes.RemoveHouseConsignmentYesNoController
      .onPageLoad(arrivalId, houseConsignmentIndex, mode)
      .url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "RemoveHouseConsignmentYesNoController Controller" - {

    "must return OK and the correct view for a GET" in {

      forAll(arbitrary[BigDecimal], arbitrary[Country]) {
        (grossWeight, country) =>
          val userAnswers = emptyUserAnswers
            .setValue(GrossWeightPage(houseConsignmentIndex), grossWeight)
            .setValue(CountryOfDestinationPage(houseConsignmentIndex), country)

          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, removeHouseConsignmentRoute)

          val result = route(app, request).value

          val view = injector.instanceOf[RemoveHouseConsignmentYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              form(houseConsignmentIndex),
              mrn,
              arrivalId,
              houseConsignmentIndex,
              mode
            )(request, messages).toString
      }
    }

    "when yes submitted" - {
      "must redirect to add another house consignment and remove house consignment at specified index" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = emptyUserAnswers
          .setValue(GrossWeightPage(houseConsignmentIndex), BigDecimal(123.4))
          .setValue(CountryOfDestinationPage(houseConsignmentIndex), Country("GB", "United Kingdom"))

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeHouseConsignmentRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.houseConsignment.routes.AddAnotherHouseConsignmentController
            .onPageLoad(arrivalId, mode)
            .url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(HouseConsignmentSection(houseConsignmentIndex)) mustNot be(defined)
      }
    }

    "when no submitted" - {
      "must redirect to add another consignment and not remove house consignment at specified index" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        val userAnswers = emptyUserAnswers
          .setValue(GrossWeightPage(houseConsignmentIndex), BigDecimal(123.4))
          .setValue(CountryOfDestinationPage(houseConsignmentIndex), Country("GB", "United Kingdom"))

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeHouseConsignmentRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.houseConsignment.routes.AddAnotherHouseConsignmentController
            .onPageLoad(arrivalId, mode)
            .url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(HouseConsignmentSection(houseConsignmentIndex)) must be(defined)
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, removeHouseConsignmentRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.houseConsignment.routes.AddAnotherHouseConsignmentController
          .onPageLoad(arrivalId, mode)
          .url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      forAll(arbitrary[BigDecimal], arbitrary[Country]) {
        (grossWeight, country) =>
          val userAnswers = emptyUserAnswers
            .setValue(GrossWeightPage(houseConsignmentIndex), grossWeight)
            .setValue(CountryOfDestinationPage(houseConsignmentIndex), country)

          setExistingUserAnswers(userAnswers)

          val invalidAnswer = ""

          val request    = FakeRequest(POST, removeHouseConsignmentRoute).withFormUrlEncodedBody(("value", ""))
          val filledForm = form(houseConsignmentIndex).bind(Map("value" -> invalidAnswer))

          val result = route(app, request).value

          status(result) mustEqual BAD_REQUEST

          val view = injector.instanceOf[RemoveHouseConsignmentYesNoView]

          contentAsString(result) mustEqual
            view(filledForm, mrn, arrivalId, houseConsignmentIndex, mode)(request, messages).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, removeHouseConsignmentRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, removeHouseConsignmentRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
