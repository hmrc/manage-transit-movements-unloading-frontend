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

package controllers.houseConsignment.index.departureMeansOfTransport

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import generators.Generators
import models.reference.TransportMeansIdentification
import models.removable.TransportMeans
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.houseConsignment.index.departureMeansOfTransport.{TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
import pages.sections.houseConsignment.index.departureTransportMeans.TransportMeansSection
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.houseConsignment.index.departureMeansOfTransport.RemoveDepartureMeansOfTransportYesNoView

import scala.concurrent.Future

class RemoveDepartureMeansOfTransportYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("houseConsignment.index.departureMeansOfTransport.removeDepartureMeansOfTransportYesNo")
  private val mode         = NormalMode

  private lazy val removeDepartureTransportMeansRoute =
    controllers.houseConsignment.index.departureMeansOfTransport.routes.RemoveDepartureMeansOfTransportYesNoController
      .onPageLoad(arrivalId, houseConsignmentIndex, transportMeansIndex, mode)
      .url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "RemoveBorderTransportYesNoController Controller" - {

    "must return OK and the correct view for a GET" in {

      forAll(arbitrary[TransportMeansIdentification], nonEmptyString) {
        (identifier, identificationNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(TransportMeansIdentificationPage(houseConsignmentIndex, transportMeansIndex), identifier)
            .setValue(VehicleIdentificationNumberPage(houseConsignmentIndex, transportMeansIndex), identificationNumber)

          val insetText = TransportMeans(transportMeansIndex, Some(identifier), Some(identificationNumber)).forRemoveDisplay

          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, removeDepartureTransportMeansRoute)

          val result = route(app, request).value

          val view = injector.instanceOf[RemoveDepartureMeansOfTransportYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, mrn, arrivalId, mode, houseConsignmentIndex, transportMeansIndex, insetText)(request, messages).toString
      }
    }

    "when yes submitted" - {
      "must redirect to add another departureTransportMeans and remove departureTransportMeans at specified index" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        val userAnswers = emptyUserAnswers.setValue(TransportMeansSection(houseConsignmentIndex, transportMeansIndex), Json.obj())

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeDepartureTransportMeansRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(
          result
        ).value mustEqual controllers.houseConsignment.index.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController
          .onPageLoad(arrivalId, houseConsignmentIndex, mode)
          .url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(TransportMeansSection(houseConsignmentIndex, transportMeansIndex)) mustNot be(defined)
      }
    }

    "when no submitted" - {
      "must redirect to add another departureTransportMeans and not remove departureTransportMeans at specified index" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        val userAnswers = emptyUserAnswers.setValue(TransportMeansSection(houseConsignmentIndex, transportMeansIndex), Json.obj())

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeDepartureTransportMeansRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(
          result
        ).value mustEqual controllers.houseConsignment.index.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController
          .onPageLoad(arrivalId, houseConsignmentIndex, mode)
          .url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(TransportMeansSection(houseConsignmentIndex, transportMeansIndex)) must be(defined)
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers.setValue(TransportMeansSection(houseConsignmentIndex, transportMeansIndex), Json.obj()))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, removeDepartureTransportMeansRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.houseConsignment.index.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController
        .onPageLoad(arrivalId, houseConsignmentIndex, mode)
        .url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      forAll(arbitrary[TransportMeansIdentification], nonEmptyString) {
        (identifier, identificationNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(TransportMeansIdentificationPage(houseConsignmentIndex, transportMeansIndex), identifier)
            .setValue(VehicleIdentificationNumberPage(houseConsignmentIndex, transportMeansIndex), identificationNumber)

          val insetText = TransportMeans(transportMeansIndex, Some(identifier), Some(identificationNumber)).forRemoveDisplay

          setExistingUserAnswers(userAnswers)

          val invalidAnswer = ""

          val request    = FakeRequest(POST, removeDepartureTransportMeansRoute).withFormUrlEncodedBody(("value", ""))
          val filledForm = form.bind(Map("value" -> invalidAnswer))

          val result = route(app, request).value

          status(result) mustEqual BAD_REQUEST

          val view = injector.instanceOf[RemoveDepartureMeansOfTransportYesNoView]

          contentAsString(result) mustEqual
            view(filledForm, mrn, arrivalId, mode, houseConsignmentIndex, transportMeansIndex, insetText)(request, messages).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, removeDepartureTransportMeansRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, removeDepartureTransportMeansRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
