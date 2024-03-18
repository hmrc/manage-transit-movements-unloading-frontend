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
import models.reference.GoodsReference
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import pages.sections.ItemSection
import pages.transportEquipment.index.ItemPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.GoodsReferenceService
import views.html.transportEquipment.index.RemoveItemYesNoView

class RemoveGoodsReferenceYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val declarationGoodsItemNumber = BigInt(123)
  private val itemDescription            = "shirts"

  private val formProvider              = new YesNoFormProvider()
  private val form                      = formProvider("transportEquipment.index.item.removeItemYesNo", equipmentIndex.display)
  private lazy val removeItemYesNoRoute = routes.RemoveGoodsReferenceYesNoController.onPageLoad(arrivalId, equipmentIndex, itemIndex).url

  private val mockGoodsReferenceService = mock[GoodsReferenceService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[GoodsReferenceService].toInstance(mockGoodsReferenceService))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockGoodsReferenceService)

    when(mockGoodsReferenceService.getGoodsReference(any(), any(), any()))
      .thenReturn(Some(GoodsReference(declarationGoodsItemNumber, itemDescription)))
  }

  "RemoveItemYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.setValue(ItemPage(equipmentIndex, itemIndex), declarationGoodsItemNumber)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, removeItemYesNoRoute)
      val result  = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[RemoveItemYesNoView]

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, equipmentIndex, itemIndex, Some("Item 123 - shirts"))(request, messages).toString
    }

    "must redirect to the next page" - {
      "when yes is submitted" in {

        val userAnswers = emptyUserAnswers.setValue(ItemPage(equipmentIndex, itemIndex), declarationGoodsItemNumber)
        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeItemYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.ApplyAnotherItemController.onPageLoad(arrivalId, NormalMode, equipmentIndex).url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(ItemSection(equipmentIndex, itemIndex)) mustNot be(defined)
      }

      "when no is submitted" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, removeItemYesNoRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.ApplyAnotherItemController.onPageLoad(arrivalId, NormalMode, equipmentIndex).url

        verifyNoInteractions(mockSessionRepository)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers.setValue(ItemPage(equipmentIndex, itemIndex), declarationGoodsItemNumber)
      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, removeItemYesNoRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[RemoveItemYesNoView]

      contentAsString(result) mustEqual
        view(boundForm, mrn, arrivalId, equipmentIndex, itemIndex, Some("Item 123 - shirts"))(request, messages).toString
    }

    "must redirect for a GET" - {
      "to Session Expired if no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(GET, removeItemYesNoRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
      }

      "if no item is found" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, removeItemYesNoRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.ApplyAnotherItemController.onPageLoad(arrivalId, NormalMode, equipmentIndex).url
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

      "if no item is found" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, removeItemYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.ApplyAnotherItemController.onPageLoad(arrivalId, NormalMode, equipmentIndex).url
      }
    }
  }
}
