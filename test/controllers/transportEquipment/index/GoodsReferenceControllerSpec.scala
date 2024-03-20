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
import controllers.routes
import forms.SelectableFormProvider
import generators.Generators
import models.reference.GoodsReference
import models.{Index, NormalMode, SelectableList}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.GoodsReferenceService
import views.html.transportEquipment.index.GoodsReferenceView

class GoodsReferenceControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val mockGoodsReferenceService = mock[GoodsReferenceService]

  private val goodsReferences = Seq(
    GoodsReference(1, "description 1"),
    GoodsReference(2, "description 2")
  )

  private val goodsReference = goodsReferences.head

  private val formProvider = new SelectableFormProvider()
  private val mode         = NormalMode
  private val form         = formProvider(mode, "transport.equipment.selectItems", SelectableList(goodsReferences))

  private lazy val controllerRoute = controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(arrivalId, Index(0), itemIndex, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[GoodsReferenceService].toInstance(mockGoodsReferenceService))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockGoodsReferenceService)

    when(mockGoodsReferenceService.getGoodsReferences(any(), any(), any()))
      .thenReturn(goodsReferences)
  }

  "GoodsReferenceController" - {

    "must return OK and the correct view for a GET" in {

      when(mockGoodsReferenceService.getGoodsReference(any(), any(), any()))
        .thenReturn(None)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, controllerRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[GoodsReferenceView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, arrivalId, Index(0), itemIndex, mrn, goodsReferences, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockGoodsReferenceService.getGoodsReference(any(), any(), any()))
        .thenReturn(Some(goodsReference))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, controllerRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> goodsReference.declarationGoodsItemNumber.toString()))

      val view = injector.instanceOf[GoodsReferenceView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, arrivalId, Index(0), itemIndex, mrn, goodsReferences, mode)(request, messages).toString
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
        view(boundForm, arrivalId, Index(0), itemIndex, mrn, goodsReferences, mode)(request, messages).toString
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
