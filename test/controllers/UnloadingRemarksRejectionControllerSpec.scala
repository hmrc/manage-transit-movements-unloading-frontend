/*
 * Copyright 2022 HM Revenue & Customs
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
import extractors.RejectionMessageExtractor
import generators.Generators
import models.ErrorType.IncorrectValue
import models.{DefaultPointer, ErrorPointer, FunctionalError, NumberOfPackagesPointer, UnloadingRemarksRejectionMessage, VehicleRegistrationPointer}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UnloadingRemarksRejectionService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.UnloadingRemarksRejectionViewModel
import viewModels.sections.Section
import views.html.{UnloadingRemarksMultipleErrorsRejectionView, UnloadingRemarksRejectionView}

import java.time.LocalDate
import scala.concurrent.Future
import scala.util.{Failure, Success}

class UnloadingRemarksRejectionControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val mockUnloadingRemarksRejectionService = mock[UnloadingRemarksRejectionService]
  private val mockViewModel                        = mock[UnloadingRemarksRejectionViewModel]
  private val mockExtractor                        = mock[RejectionMessageExtractor]

  private def functionalError(pointer: ErrorPointer) = FunctionalError(IncorrectValue, pointer, Some("R206"), Some("origValue"))
  private val vehicleFunctionalError                 = functionalError(VehicleRegistrationPointer)
  private val packagesFunctionalError                = functionalError(NumberOfPackagesPointer)
  private val defaultFunctionalError                 = functionalError(DefaultPointer("error here"))

  private def rejectionMessageWithErrors(errors: Seq[FunctionalError]) = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)

  private val sampleRow = arbitrary[SummaryListRow].sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUnloadingRemarksRejectionService, mockViewModel, mockExtractor)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingRemarksRejectionService].toInstance(mockUnloadingRemarksRejectionService))
      .overrides(bind[UnloadingRemarksRejectionViewModel].toInstance(mockViewModel))
      .overrides(bind[RejectionMessageExtractor].toInstance(mockExtractor))

  "UnloadingRemarksRejection Controller" - {

    "return OK and the single error rejection view when unloading a single rejection" in {
      val errors: Seq[FunctionalError] = Seq(packagesFunctionalError)

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(rejectionMessageWithErrors(errors))))

      val userAnswers = emptyUserAnswers

      when(mockExtractor.apply(any(), any()))
        .thenReturn(Success(userAnswers))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      when(mockViewModel.apply(any())(any()))
        .thenReturn(Some(sampleRow))

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      val view = injector.instanceOf[UnloadingRemarksRejectionView]

      val expectedSection: Section = Section(Seq(sampleRow))

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(arrivalId, expectedSection)(request, messages).toString

      verify(mockViewModel).apply(eqTo(userAnswers))(any())
      verify(mockSessionRepository).set(eqTo(userAnswers))
    }

    "return OK and the multiple error rejection view for a GET when unloading rejection message returns Some" in {
      val errors = Seq(packagesFunctionalError, vehicleFunctionalError)

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(rejectionMessageWithErrors(errors))))

      when(mockExtractor.apply(any(), any()))
        .thenReturn(Success(emptyUserAnswers))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value
      val view   = injector.instanceOf[UnloadingRemarksMultipleErrorsRejectionView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(arrivalId, errors)(request, messages).toString
    }

    "return OK and the multiple error rejection view when unloading rejection message has an unrecognised pointer" in {
      val errors = Seq(defaultFunctionalError)

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(rejectionMessageWithErrors(errors))))

      when(mockExtractor.apply(any(), any()))
        .thenReturn(Success(emptyUserAnswers))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      when(mockViewModel.apply(any())(any()))
        .thenReturn(None)

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value
      val view   = injector.instanceOf[UnloadingRemarksMultipleErrorsRejectionView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(arrivalId, errors)(request, messages).toString
    }

    "return OK and the multiple error rejection view when unloading rejection message has multiple unrecognised pointers" in {
      val errors = Seq(defaultFunctionalError, defaultFunctionalError)

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(rejectionMessageWithErrors(errors))))

      when(mockExtractor.apply(any(), any()))
        .thenReturn(Success(emptyUserAnswers))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      when(mockViewModel.apply(any())(any()))
        .thenReturn(None)

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value
      val view   = injector.instanceOf[UnloadingRemarksMultipleErrorsRejectionView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(arrivalId, errors)(request, messages).toString
    }

    "return OK and the multiple error rejection view when we have an unknown error with no original value" in {
      val errors = Seq(defaultFunctionalError.copy(originalAttributeValue = None))

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(rejectionMessageWithErrors(errors))))

      when(mockExtractor.apply(any(), any()))
        .thenReturn(Success(emptyUserAnswers))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      when(mockViewModel.apply(any())(any()))
        .thenReturn(None)

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value
      val view   = injector.instanceOf[UnloadingRemarksMultipleErrorsRejectionView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(arrivalId, errors)(request, messages).toString
    }

    "render 'Technical difficulties' page when a known error pointer has no original value" in {
      val errors = Seq(packagesFunctionalError.copy(originalAttributeValue = None))

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(rejectionMessageWithErrors(errors))))

      when(mockExtractor.apply(any(), any()))
        .thenReturn(Success(emptyUserAnswers))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      when(mockViewModel.apply(any())(any()))
        .thenReturn(None)

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url
    }

    "render 'Technical difficulties' page when unloading rejection message has no errors" in {
      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, Seq.empty))))

      when(mockExtractor.apply(any(), any()))
        .thenReturn(Success(emptyUserAnswers))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url
    }

    "render the 'Technical difficulties' page when unloading rejection message returns a None" in {
      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url

      verify(mockUnloadingRemarksRejectionService).unloadingRemarksRejectionMessage(eqTo(arrivalId))(any())
    }

    "render the 'Technical difficulties' page when the extractor fails" in {
      val errors: Seq[FunctionalError] = Seq(packagesFunctionalError)

      val rejectionMessage = rejectionMessageWithErrors(errors)

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(rejectionMessageWithErrors(errors))))

      when(mockExtractor.apply(any(), any()))
        .thenReturn(Failure(new Throwable("")))

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url

      verify(mockExtractor).apply(any(), eqTo(rejectionMessage))
    }
  }
}
