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

package controllers.houseConsignment.index.items

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import generators.Generators
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.departureMeansOfTransport.VehicleIdentificationNumberPage
import pages.houseConsignment.index.items.CommodityCodePage
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.houseConsignment.index.items.RemoveCommodityCodeYesNoView

import scala.concurrent.Future

class RemoveCommodityCodeYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("houseConsignment.removeCommodityCodeYesNo", houseConsignmentIndex.display, itemIndex.display)
  private val mode         = NormalMode

  private lazy val removeCommodityCodeRoute =
    controllers.houseConsignment.index.items.routes.RemoveCommodityCodeYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "RemoveCommodityCodeYesNoController " - {

    "must return OK and the correct view for a GET" in {

      forAll(nonEmptyString) {
        commodityCode =>
          val userAnswers = emptyUserAnswers
            .setValue(CommodityCodePage(houseConsignmentIndex, itemIndex), commodityCode)
            .setValue(VehicleIdentificationNumberPage(transportMeansIndex), commodityCode)

          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, removeCommodityCodeRoute)

          val result = route(app, request).value

          val view = injector.instanceOf[RemoveCommodityCodeYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, mrn, arrivalId, houseConsignmentIndex, itemIndex, mode, Some(commodityCode))(request, messages).toString
      }
    }

    "when yes submitted" - {
      "must redirect to house consignment summary and remove commodity code at specified index" in {
        val userAnswers = emptyUserAnswers.setValue(CommodityCodePage(houseConsignmentIndex, itemIndex), "3")

        setExistingUserAnswers(userAnswers)
        when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

        val request = FakeRequest(POST, removeCommodityCodeRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex).url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(CommodityCodePage(houseConsignmentIndex, itemIndex)) mustNot be(defined)
      }
    }

    "when no submitted" - {
      "must redirect to house consignment summary and not remove departureTransportMeans at specified index" in {
        when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)
        val userAnswers = emptyUserAnswers.setValue(CommodityCodePage(houseConsignmentIndex, itemIndex), "3")

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeCommodityCodeRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex).url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(CommodityCodePage(houseConsignmentIndex, itemIndex)) must be(defined)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      forAll(nonEmptyString) {
        commodityCode =>
          val userAnswers = emptyUserAnswers
            .setValue(CommodityCodePage(houseConsignmentIndex, itemIndex), commodityCode)

          setExistingUserAnswers(userAnswers)

          val invalidAnswer = ""

          val request    = FakeRequest(POST, removeCommodityCodeRoute).withFormUrlEncodedBody(("value", ""))
          val filledForm = form.bind(Map("value" -> invalidAnswer))

          val result = route(app, request).value

          status(result) mustEqual BAD_REQUEST

          val view = injector.instanceOf[RemoveCommodityCodeYesNoView]

          contentAsString(result) mustEqual
            view(filledForm, mrn, arrivalId, houseConsignmentIndex, itemIndex, mode, Some(commodityCode))(request, messages).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, removeCommodityCodeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, removeCommodityCodeRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
