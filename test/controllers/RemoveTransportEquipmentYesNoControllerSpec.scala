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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import generators.Generators
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Gen
import pages.ContainerIdentificationNumberPage
import pages.sections.TransportEquipmentSection
import pages.transportEquipment.index.AddContainerIdentificationNumberYesNoPage
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpVerbs.GET
import views.html.RemoveTransportEquipmentYesNoView

import scala.concurrent.Future

class RemoveTransportEquipmentYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("transportEquipment.index.removeTransportEquipmentYesNo", transportEquipmentIndex.display)
  private val mode         = NormalMode

  private lazy val removeTransportEquipmentMeansRoute =
    controllers.transportEquipment.index.routes.RemoveTransportEquipmentYesNoController.onPageLoad(arrivalId, mode, transportEquipmentIndex).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "RemoveTransportEquipmentYesNoController Controller" - {

    "must return OK and the correct view for a GET" in {
      forAll(Gen.alphaNumStr) {
        containerId =>
          val userAnswers = emptyUserAnswers
            .setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), "Seal-1")
            .setValue(AddContainerIdentificationNumberYesNoPage(equipmentIndex), true)
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), containerId)

          setExistingUserAnswers(userAnswers)
          val insetText = Some(s"Container $containerId")

          val request = FakeRequest(GET, removeTransportEquipmentMeansRoute)

          val result = route(app, request).value

          val view = injector.instanceOf[RemoveTransportEquipmentYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, mrn, arrivalId, transportMeansIndex, insetText, mode)(request, messages).toString

      }
    }
    "when yes submitted" - {
      "must redirect to add another TransportEquipment and remove departureTransportMeans at specified index" in {
        when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)
        val userAnswers = emptyUserAnswers
          .setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), "Seal-1")
          .setValue(AddContainerIdentificationNumberYesNoPage(equipmentIndex), true)
          .setValue(ContainerIdentificationNumberPage(equipmentIndex), "CIN-1")

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeTransportEquipmentMeansRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.transportEquipment.routes.AddAnotherEquipmentController
          .onPageLoad(arrivalId, mode)
          .url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(TransportEquipmentSection(transportMeansIndex)) mustNot be(defined)
      }
    }

    "when no submitted" - {
      "must redirect to add another TransportEquipment and not remove TransportEquipment at specified index" in {
        when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

        val userAnswers = emptyUserAnswers
          .setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), "Seal-1")
          .setValue(AddContainerIdentificationNumberYesNoPage(equipmentIndex), true)
          .setValue(ContainerIdentificationNumberPage(equipmentIndex), "CIN-1")

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeTransportEquipmentMeansRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.transportEquipment.routes.AddAnotherEquipmentController
          .onPageLoad(arrivalId, mode)
          .url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(TransportEquipmentSection(transportMeansIndex)) must be(defined)
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val userAnswers = emptyUserAnswers
        .setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), "Seal-1")
        .setValue(AddContainerIdentificationNumberYesNoPage(equipmentIndex), true)
        .setValue(ContainerIdentificationNumberPage(equipmentIndex), "CIN-1")

      setExistingUserAnswers(userAnswers)
      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      val request = FakeRequest(POST, removeTransportEquipmentMeansRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.transportEquipment.routes.AddAnotherEquipmentController
        .onPageLoad(arrivalId, mode)
        .url

    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      forAll(Gen.alphaNumStr) {
        containerId =>
          val userAnswers = emptyUserAnswers
            .setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), "Seal-1")
            .setValue(AddContainerIdentificationNumberYesNoPage(equipmentIndex), true)
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), containerId)

          setExistingUserAnswers(userAnswers)

          val invalidAnswer = ""
          val request       = FakeRequest(POST, removeTransportEquipmentMeansRoute).withFormUrlEncodedBody(("value", ""))
          val filledForm    = form.bind(Map("value" -> invalidAnswer))
          val result        = route(app, request).value
          val insetText     = Some(s"Container $containerId")

          status(result) mustEqual BAD_REQUEST

          val view = injector.instanceOf[RemoveTransportEquipmentYesNoView]

          contentAsString(result) mustEqual
            view(filledForm, mrn, arrivalId, transportEquipmentIndex, insetText, mode)(request, messages).toString

      }
    }
    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, removeTransportEquipmentMeansRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, removeTransportEquipmentMeansRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
