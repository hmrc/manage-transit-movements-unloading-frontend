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
import extractors.UnloadingPermissionExtractor
import generators.{MessagesModelGenerators, ViewModelGenerators}
import models.ErrorType.{IncorrectValue, NotSupportedPosition}
import models.{DefaultPointer, FunctionalError, NumberOfPackagesPointer, UnloadingPermission, UnloadingRemarksRejectionMessage, VehicleRegistrationPointer}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{UnloadingPermissionService, UnloadingRemarksRejectionService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.UnloadingRemarksRejectionViewModel
import viewModels.sections.Section
import views.html.{UnloadingRemarksMultipleErrorsRejectionView, UnloadingRemarksRejectionView}

import java.time.LocalDate
import scala.concurrent.Future
import scala.util.{Failure, Success}

class UnloadingRemarksRejectionControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MessagesModelGenerators with ViewModelGenerators {

  private val mockUnloadingRemarksRejectionService = mock[UnloadingRemarksRejectionService]
  private val mockViewModel                        = mock[UnloadingRemarksRejectionViewModel]
  private val mockUnloadingPermissionService       = mock[UnloadingPermissionService]
  private val mockExtractor                        = mock[UnloadingPermissionExtractor]

  private val originalVehicleValue    = "origValue"
  private val vehicleFunctionalError  = FunctionalError(IncorrectValue, VehicleRegistrationPointer, Some("R206"), Some(originalVehicleValue))
  private val originalPackagesValue   = "origValue"
  private val packagesFunctionalError = FunctionalError(IncorrectValue, NumberOfPackagesPointer, Some("R206"), Some(originalPackagesValue))
  private val defaultFunctionalError  = FunctionalError(NotSupportedPosition, DefaultPointer("error here"), Some("R206"), Some(originalPackagesValue))

  private val sampleUnloadingPermission = arbitrary[UnloadingPermission].sample.value
  private val sampleRow                 = arbitrary[SummaryListRow].sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUnloadingRemarksRejectionService, mockViewModel, mockUnloadingPermissionService, mockExtractor)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingRemarksRejectionService].toInstance(mockUnloadingRemarksRejectionService))
      .overrides(bind[UnloadingRemarksRejectionViewModel].toInstance(mockViewModel))
      .overrides(bind[UnloadingPermissionService].toInstance(mockUnloadingPermissionService))
      .overrides(bind[UnloadingPermissionExtractor].toInstance(mockExtractor))

  "UnloadingRemarksRejection Controller" - {

    "return OK and the single error rejection view when unloading a single rejection" in {
      val errors: Seq[FunctionalError] = Seq(packagesFunctionalError)

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors))))

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
        .thenReturn(Future.successful(Some(sampleUnloadingPermission)))

      when(mockExtractor.apply(any(), any())(any(), any()))
        .thenReturn(Future.successful(Success(emptyUserAnswers)))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      when(mockViewModel.apply(any(), any())(any()))
        .thenReturn(Some(sampleRow))

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      val view = injector.instanceOf[UnloadingRemarksRejectionView]

      val expectedSection: Section = Section(Seq(sampleRow))

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(arrivalId, expectedSection)(request, messages).toString
    }

    "return OK and the multiple error rejection view for a GET when unloading rejection message returns Some" in {
      val errors = Seq(packagesFunctionalError, vehicleFunctionalError)

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors))))

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
        .thenReturn(Future.successful(Some(sampleUnloadingPermission)))

      when(mockExtractor.apply(any(), any())(any(), any()))
        .thenReturn(Future.successful(Success(emptyUserAnswers)))

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
        .thenReturn(Future.successful(Some(UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors))))

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
        .thenReturn(Future.successful(Some(sampleUnloadingPermission)))

      when(mockExtractor.apply(any(), any())(any(), any()))
        .thenReturn(Future.successful(Success(emptyUserAnswers)))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      when(mockViewModel.apply(any(), any())(any()))
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
        .thenReturn(Future.successful(Some(UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors))))

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
        .thenReturn(Future.successful(Some(sampleUnloadingPermission)))

      when(mockExtractor.apply(any(), any())(any(), any()))
        .thenReturn(Future.successful(Success(emptyUserAnswers)))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      when(mockViewModel.apply(any(), any())(any()))
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
        .thenReturn(Future.successful(Some(UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors))))

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
        .thenReturn(Future.successful(Some(sampleUnloadingPermission)))

      when(mockExtractor.apply(any(), any())(any(), any()))
        .thenReturn(Future.successful(Success(emptyUserAnswers)))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      when(mockViewModel.apply(any(), any())(any()))
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
        .thenReturn(Future.successful(Some(UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors))))

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
        .thenReturn(Future.successful(Some(sampleUnloadingPermission)))

      when(mockExtractor.apply(any(), any())(any(), any()))
        .thenReturn(Future.successful(Success(emptyUserAnswers)))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      when(mockViewModel.apply(any(), any())(any()))
        .thenReturn(None)

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url
    }

    "render 'Technical difficulties' page when unloading rejection message has no errors" in {
      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, Seq.empty))))

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
        .thenReturn(Future.successful(Some(sampleUnloadingPermission)))

      when(mockExtractor.apply(any(), any())(any(), any()))
        .thenReturn(Future.successful(Success(emptyUserAnswers)))

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

    "render the 'Technical difficulties' page when unloading permission returns a None" in {
      val errors: Seq[FunctionalError] = Seq(packagesFunctionalError)

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors))))

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url

      verify(mockUnloadingPermissionService).getUnloadingPermission(eqTo(arrivalId))(any(), any())
    }

    "render the 'Technical difficulties' page when the extractor fails" in {
      val errors: Seq[FunctionalError] = Seq(packagesFunctionalError)

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors))))

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
        .thenReturn(Future.successful(Some(sampleUnloadingPermission)))

      when(mockExtractor.apply(any(), any())(any(), any()))
        .thenReturn(Future.successful(Failure(new Throwable(""))))

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url

      verify(mockExtractor).apply(any(), eqTo(sampleUnloadingPermission))(any(), any())
    }
  }
}
