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

package controllers.transportEquipment.index

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import generators.Generators
import models.UserAnswers
import models.reference.Item
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.verify
import pages.sections.ItemSection
import pages.transportEquipment.index.ItemPage
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.transportEquipment.index.RemoveItemYesNoView

class RemoveItemYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val itemIdNumber              = Item(123, "description")
  private val formProvider              = new YesNoFormProvider()
  private val form                      = formProvider("transportEquipment.index.item.removeItemYesNo", equipmentIndex.display, itemIdNumber)
  private lazy val removeItemYesNoRoute = routes.RemoveGoodsReferenceYesNoController.onPageLoad(arrivalId, equipmentIndex, itemIndex).url

  "RemoveItemYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.setValue(ItemPage(equipmentIndex, itemIndex), itemIdNumber)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, removeItemYesNoRoute)
      val result  = route(app, request).value

      val view = injector.instanceOf[RemoveItemYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, equipmentIndex, itemIndex, itemIdNumber.toString, None)(request, messages).toString
    }

    "must redirect to the next page" - {
      "when yes is submitted" ignore {

        val userAnswers = emptyUserAnswers.setValue(ItemPage(equipmentIndex, itemIndex), itemIdNumber)
        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeItemYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual Call("GET", "#") // TODO Should go to addAnotherItemController

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(ItemSection(equipmentIndex, itemIndex)) mustNot be(defined)
      }

      "when no is submitted" ignore {
        val userAnswers = emptyUserAnswers.setValue(ItemPage(equipmentIndex, itemIndex), itemIdNumber)
        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeItemYesNoRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual Call("GET", "#") // TODO Should go to addAnotherItemController
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers.setValue(ItemPage(equipmentIndex, itemIndex), itemIdNumber)
      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, removeItemYesNoRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[RemoveItemYesNoView]

      contentAsString(result) mustEqual
        view(boundForm, mrn, arrivalId, equipmentIndex, itemIndex, itemIdNumber.value, None)(request, messages).toString
    }

    "must redirect for a GET" - {
      "to Session Expired if no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(GET, removeItemYesNoRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
      }

      "if no item is found" ignore {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, removeItemYesNoRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual Call("GET", "#") // TODO Should go to addAnotherItemController
      }
    }

    "must redirect for a POST" - {
      "to Session Expired if no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(POST, removeItemYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
      }

      "if no item is found" ignore {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, removeItemYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual Call("GET", "#") // TODO Should go to addAnotherItemController
      }
    }
  }
}
