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

package controllers.transportEquipment.index.seals

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import generators.Generators
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.sections.SealSection
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.transportEquipment.index.seals.RemoveSealYesNoView

import scala.concurrent.Future

class RemoveSealYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val sealIdNumber = nonEmptyString.sample.value
  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("transportEquipment.index.seal.removeSealYesNo", equipmentIndex.display, sealIdNumber)

  private val equipmentMode = NormalMode

  private lazy val removeSealYesNoRoute = routes.RemoveSealYesNoController.onPageLoad(arrivalId, equipmentMode, equipmentIndex, sealIndex).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "RemoveSealYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), sealIdNumber)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, removeSealYesNoRoute)
      val result  = route(app, request).value

      val view = injector.instanceOf[RemoveSealYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, equipmentMode, equipmentIndex, sealIndex, sealIdNumber)(request, messages).toString
    }

    "must redirect to the next page" - {
      "when yes is submitted" in {

        when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)
        val userAnswers = emptyUserAnswers
          .setSequenceNumber(SealSection(equipmentIndex, sealIndex), BigInt(1))
          .setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), sealIdNumber)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeSealYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.transportEquipment.index.routes.AddAnotherSealController
            .onPageLoad(arrivalId, equipmentMode, equipmentIndex)
            .url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.getRemoved(SealSection(equipmentIndex, sealIndex)) mustBe true
      }

      "when no is submitted" in {
        when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)
        val userAnswers = emptyUserAnswers
          .setValue(SealSection(equipmentIndex, sealIndex), Json.obj())
          .setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), sealIdNumber)
        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeSealYesNoRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.transportEquipment.index.routes.AddAnotherSealController
            .onPageLoad(arrivalId, equipmentMode, equipmentIndex)
            .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers.setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), sealIdNumber)
      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, removeSealYesNoRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[RemoveSealYesNoView]

      contentAsString(result) mustEqual
        view(boundForm, mrn, arrivalId, equipmentMode, equipmentIndex, sealIndex, sealIdNumber)(request, messages).toString
    }

    "must redirect for a GET" - {
      "to Session Expired if no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(GET, removeSealYesNoRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
      }

      "if no seal is found" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, removeSealYesNoRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.transportEquipment.index.routes.AddAnotherSealController
            .onPageLoad(arrivalId, equipmentMode, equipmentIndex)
            .url
      }
    }

    "must redirect for a POST" - {
      "to Session Expired if no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(POST, removeSealYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
      }

      "if no seal is found" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, removeSealYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.transportEquipment.index.routes.AddAnotherSealController
            .onPageLoad(arrivalId, equipmentMode, equipmentIndex)
            .url
      }
    }
  }
}
