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

package controllers.houseConsignment.index.items

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.AddAnotherFormProvider
import generators.Generators
import models.{CheckMode, Index, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.houseConsignment.index.items.{AddAnotherItemPage, DeclarationGoodsItemNumberPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.GoodsReferenceService
import viewModels.ListItem
import viewModels.houseConsignment.index.items.AddAnotherItemViewModel
import viewModels.houseConsignment.index.items.AddAnotherItemViewModel.AddAnotherItemViewModelProvider
import views.html.houseConsignment.index.items.AddAnotherItemView

import scala.util.Success

class AddAnotherItemControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar with Generators {

  private val formProvider = new AddAnotherFormProvider()

  private def form(viewModel: AddAnotherItemViewModel) =
    formProvider(viewModel.prefix, viewModel.allowMore, houseConsignmentIndex)

  private val houseConsignmentMode = NormalMode

  private lazy val addAnotherItemRoute =
    routes.AddAnotherItemController.onPageLoad(arrivalId, houseConsignmentIndex, houseConsignmentMode).url

  private val mockViewModelProvider = mock[AddAnotherItemViewModelProvider]

  private val mockGoodsReferenceService = mock[GoodsReferenceService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind(classOf[AddAnotherItemViewModelProvider]).toInstance(mockViewModelProvider),
        bind(classOf[GoodsReferenceService]).toInstance(mockGoodsReferenceService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
    reset(mockGoodsReferenceService)
  }

  private val listItem          = arbitrary[ListItem].sample.value
  private val listItems         = Seq.fill(Gen.choose(1, frontendAppConfig.maxHouseConsignmentItem - 1).sample.value)(listItem)
  private val maxedOutListItems = Seq.fill(frontendAppConfig.maxHouseConsignmentItem)(listItem)

  private val viewModel = arbitrary[AddAnotherItemViewModel].sample.value

  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  "AddAnotherItemController" - {
    "must return OK and the correct view for a GET" - {
      "when max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherItemRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherItemView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(notMaxedOutViewModel), mrn, arrivalId, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherItemRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherItemView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(maxedOutViewModel), mrn, arrivalId, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" - {
      "when max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers.setValue(AddAnotherItemPage(houseConsignmentIndex), true))

        val request = FakeRequest(GET, addAnotherItemRoute)

        val result = route(app, request).value

        val filledForm = form(notMaxedOutViewModel).bind(Map("value" -> "true"))

        val view = injector.instanceOf[AddAnotherItemView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, mrn, arrivalId, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers.setValue(AddAnotherItemPage(houseConsignmentIndex), true))

        val request = FakeRequest(GET, addAnotherItemRoute)

        val result = route(app, request).value

        val filledForm = form(maxedOutViewModel).bind(Map("value" -> "true"))

        val view = injector.instanceOf[AddAnotherItemView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, mrn, arrivalId, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "when max limit not reached" - {
      "when yes submitted" - {
        "must redirect to add description page at next index and set declaration goods item number" in {
          val nextIndex           = Index(0)
          val initialAnswers      = emptyUserAnswers
          val answersAfterCleanup = emptyUserAnswers
          val answersAfterSet     = emptyUserAnswers.setValue(DeclarationGoodsItemNumberPage(houseConsignmentIndex, nextIndex), BigInt(1))

          when(mockViewModelProvider.apply(any(), any(), any(), any())(any()))
            .thenReturn(notMaxedOutViewModel.copy(nextIndex = nextIndex))

          when(mockGoodsReferenceService.removeEmptyItems(any(), any()))
            .thenReturn(answersAfterCleanup)

          when(mockGoodsReferenceService.setNextDeclarationGoodsItemNumber(any(), any(), any()))
            .thenReturn(Success(answersAfterSet))

          setExistingUserAnswers(initialAnswers)

          val request = FakeRequest(POST, addAnotherItemRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.houseConsignment.index.items.routes.DescriptionController
            .onPageLoad(arrivalId, houseConsignmentMode, NormalMode, houseConsignmentIndex, nextIndex)
            .url

          verify(mockViewModelProvider).apply(eqTo(answersAfterCleanup), eqTo(arrivalId), eqTo(houseConsignmentIndex), eqTo(houseConsignmentMode))(any())

          val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(userAnswersCaptor.capture())
          userAnswersCaptor.getValue mustEqual answersAfterSet

          verify(mockGoodsReferenceService).removeEmptyItems(eqTo(initialAnswers), eqTo(houseConsignmentIndex))
          verify(mockGoodsReferenceService).setNextDeclarationGoodsItemNumber(eqTo(answersAfterCleanup), eqTo(houseConsignmentIndex), eqTo(itemIndex))
        }
      }

      "when no submitted" - {
        "must redirect to next page" - {
          "when adding house consignment" in {
            val houseConsignmentMode = NormalMode

            val initialAnswers      = emptyUserAnswers
            val answersAfterCleanup = emptyUserAnswers

            when(mockViewModelProvider.apply(any(), any(), any(), any())(any()))
              .thenReturn(notMaxedOutViewModel)

            when(mockGoodsReferenceService.removeEmptyItems(any(), any()))
              .thenReturn(answersAfterCleanup)

            setExistingUserAnswers(initialAnswers)

            val request = FakeRequest(POST, routes.AddAnotherItemController.onPageLoad(arrivalId, houseConsignmentIndex, houseConsignmentMode).url)
              .withFormUrlEncodedBody(("value", "false"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.houseConsignment.routes.AddAnotherHouseConsignmentController.onPageLoad(arrivalId, houseConsignmentMode).url

            verify(mockViewModelProvider).apply(eqTo(answersAfterCleanup), eqTo(arrivalId), eqTo(houseConsignmentIndex), eqTo(houseConsignmentMode))(any())
          }

          "when changing house consignment" in {
            val houseConsignmentMode = CheckMode

            val initialAnswers      = emptyUserAnswers
            val answersAfterCleanup = emptyUserAnswers

            when(mockViewModelProvider.apply(any(), any(), any(), any())(any()))
              .thenReturn(notMaxedOutViewModel)

            when(mockGoodsReferenceService.removeEmptyItems(any(), any()))
              .thenReturn(answersAfterCleanup)

            setExistingUserAnswers(initialAnswers)

            val request = FakeRequest(POST, routes.AddAnotherItemController.onPageLoad(arrivalId, houseConsignmentIndex, houseConsignmentMode).url)
              .withFormUrlEncodedBody(("value", "false"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex).url

            verify(mockViewModelProvider).apply(eqTo(answersAfterCleanup), eqTo(arrivalId), eqTo(houseConsignmentIndex), eqTo(houseConsignmentMode))(any())

            verify(mockSessionRepository).set(eqTo(answersAfterCleanup))
          }
        }
      }
    }

    "when max limit reached" - {
      "must redirect to next page" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherItemRoute)
          .withFormUrlEncodedBody(("value", ""))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.houseConsignment.routes.AddAnotherHouseConsignmentController.onPageLoad(arrivalId, houseConsignmentMode).url
      }
    }

    "must return a Bad Request and errors" - {
      "when invalid data is submitted and max limit not reached" in {
        val initialAnswers      = emptyUserAnswers
        val answersAfterCleanup = emptyUserAnswers

        when(mockViewModelProvider.apply(any(), any(), any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        when(mockGoodsReferenceService.removeEmptyItems(any(), any()))
          .thenReturn(answersAfterCleanup)

        setExistingUserAnswers(initialAnswers)

        val request = FakeRequest(POST, addAnotherItemRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form(notMaxedOutViewModel).bind(Map("value" -> ""))

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherItemView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, mrn, arrivalId, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addAnotherItemRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addAnotherItemRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
