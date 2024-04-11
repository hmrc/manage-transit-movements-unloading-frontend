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
import forms.AddAnotherFormProvider
import generators.Generators
import models.{CheckMode, Index, Mode, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.ListItem
import viewModels.transportEquipment.index.ApplyAnotherItemViewModel
import viewModels.transportEquipment.index.ApplyAnotherItemViewModel.ApplyAnotherItemViewModelProvider
import views.html.transportEquipment.index.ApplyAnotherItemView

class ApplyAnotherItemControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar with Generators {

  private val formProvider = new AddAnotherFormProvider()

  private def form(viewModel: ApplyAnotherItemViewModel, equipmentIndex: Index) =
    formProvider(viewModel.prefix, viewModel.allowMore, equipmentIndex.display)

  private val equipmentMode      = NormalMode
  private val goodsReferenceMode = NormalMode

  private lazy val applyAnotherItemRoute =
    routes.ApplyAnotherItemController.onPageLoad(arrivalId, equipmentMode, goodsReferenceMode, equipmentIndex).url

  private lazy val mockViewModelProvider = mock[ApplyAnotherItemViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[ApplyAnotherItemViewModelProvider]).toInstance(mockViewModelProvider))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
  }

  private val listItem          = arbitrary[ListItem].sample.value
  private val listItems         = Seq.fill(Gen.choose(1, frontendAppConfig.maxItems - 1).sample.value)(listItem)
  private val maxedOutListItems = Seq.fill(frontendAppConfig.maxItems)(listItem)

  private val viewModel = arbitrary[ApplyAnotherItemViewModel].sample.value

  private val emptyViewModel       = viewModel.copy(listItems = Nil)
  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  "ApplyAnotherItem Controller" - {

    "must return OK and the correct view for a GET" - {
      "when 0 goods references" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any(), any(), any())(any()))
          .thenReturn(emptyViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, applyAnotherItemRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[ApplyAnotherItemView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(emptyViewModel, equipmentIndex), mrn, arrivalId, emptyViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any(), any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, applyAnotherItemRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[ApplyAnotherItemView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(notMaxedOutViewModel, equipmentIndex), mrn, arrivalId, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any(), any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, applyAnotherItemRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[ApplyAnotherItemView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(maxedOutViewModel, equipmentIndex), mrn, arrivalId, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "when max limit not reached" - {
      "when yes submitted" - {
        "must redirect to goods reference page at next index" in {
          val modeCombinations = Gen.oneOf[(Mode, Mode)](
            (NormalMode, NormalMode),
            (CheckMode, NormalMode),
            (CheckMode, CheckMode)
          )
          forAll(modeCombinations) {
            case (equipmentMode, goodsReferenceMode) =>
              lazy val applyAnotherItemRoute =
                routes.ApplyAnotherItemController.onPageLoad(arrivalId, equipmentMode, goodsReferenceMode, equipmentIndex).url

              when(mockViewModelProvider.apply(any(), any(), any(), any(), any(), any())(any()))
                .thenReturn(notMaxedOutViewModel)

              setExistingUserAnswers(emptyUserAnswers)

              val request = FakeRequest(POST, applyAnotherItemRoute)
                .withFormUrlEncodedBody(("value", "true"))

              val result = route(app, request).value

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual controllers.transportEquipment.index.routes.GoodsReferenceController
                .onPageLoad(arrivalId, equipmentIndex, notMaxedOutViewModel.nextIndex, equipmentMode, goodsReferenceMode)
                .url
          }
        }
      }

      "when no submitted" - {
        "must redirect to next page" - {
          "when equipment mode is CheckMode" in {
            forAll(arbitrary[Mode]) {
              goodsReferenceMode =>
                lazy val applyAnotherItemRoute =
                  routes.ApplyAnotherItemController.onPageLoad(arrivalId, CheckMode, goodsReferenceMode, equipmentIndex).url

                when(mockViewModelProvider.apply(any(), any(), any(), any(), any(), any())(any()))
                  .thenReturn(notMaxedOutViewModel)

                setExistingUserAnswers(emptyUserAnswers)

                val request = FakeRequest(POST, applyAnotherItemRoute)
                  .withFormUrlEncodedBody(("value", "false"))

                val result = route(app, request).value

                status(result) mustEqual SEE_OTHER

                redirectLocation(result).value mustEqual
                  controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId).url
            }
          }

          "when equipment mode is NormalMode" in {
            val goodsReferenceMode = NormalMode

            lazy val applyAnotherItemRoute =
              routes.ApplyAnotherItemController.onPageLoad(arrivalId, NormalMode, goodsReferenceMode, equipmentIndex).url

            when(mockViewModelProvider.apply(any(), any(), any(), any(), any(), any())(any()))
              .thenReturn(notMaxedOutViewModel)

            setExistingUserAnswers(emptyUserAnswers)

            val request = FakeRequest(POST, applyAnotherItemRoute)
              .withFormUrlEncodedBody(("value", "false"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.transportEquipment.routes.AddAnotherEquipmentController.onPageLoad(arrivalId, equipmentMode).url
          }
        }
      }
    }

    "when max limit reached" - {
      "must redirect to next page" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any(), any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, applyAnotherItemRoute)
          .withFormUrlEncodedBody(("value", ""))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.transportEquipment.routes.AddAnotherEquipmentController.onPageLoad(arrivalId, equipmentMode).url
      }
    }

    "must return a Bad Request and errors" - {
      "when invalid data is submitted and max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any(), any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, applyAnotherItemRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form(notMaxedOutViewModel, equipmentIndex).bind(Map("value" -> ""))

        val result = route(app, request).value

        val view = injector.instanceOf[ApplyAnotherItemView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, mrn, arrivalId, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, applyAnotherItemRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(POST, applyAnotherItemRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
