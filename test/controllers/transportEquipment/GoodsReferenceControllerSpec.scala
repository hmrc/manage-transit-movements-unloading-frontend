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

package controllers.transportEquipment

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import forms.SelectableFormProvider
import generators.Generators
import models.reference.Item
import models.{Index, NormalMode, SelectableList}
import pages.transportEquipment.index.GoodsReferencePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.transportEquipment.index.GoodsReferenceViewModel
import views.html.transportEquipment.index.GoodsReferenceView

class GoodsReferenceControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val viewModel: GoodsReferenceViewModel = GoodsReferenceViewModel(emptyUserAnswers, None)

  private val items = SelectableList(Seq(Item(123, "description")))

  private val formProvider = new SelectableFormProvider()
  private val mode         = NormalMode
  private val form         = formProvider(mode, "transport.equipment.selectItems", items)

  private lazy val controllerRoute = controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(arrivalId, Index(0), mode).url

  "GoodsReferenceController" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, controllerRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[GoodsReferenceView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, arrivalId, Index(0), mrn, viewModel, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(GoodsReferencePage(equipmentIndex), Item(123, "description"))
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, controllerRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "123"))

      val view = injector.instanceOf[GoodsReferenceView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, arrivalId, Index(0), mrn, viewModel, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, controllerRoute)
        .withFormUrlEncodedBody(("value", "123"))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST //TODO: update once we have navigation in place
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, controllerRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[GoodsReferenceView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, arrivalId, Index(0), mrn, viewModel, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, controllerRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, controllerRoute)
        .withFormUrlEncodedBody(("value", "123"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
