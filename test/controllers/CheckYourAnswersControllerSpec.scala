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

import audit.services.AuditEventSubmissionService
import base.{AppWithDefaultMockFixtures, SpecBase}
import cats.data.NonEmptyList
import models.{TraderAtDestination, UnloadingPermission}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.http.Status.ACCEPTED
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{UnloadingPermissionService, UnloadingRemarksService}
import views.html.CheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  val unloadingPermission: UnloadingPermission = UnloadingPermission(
    movementReferenceNumber = "19IT02110010007827",
    transportIdentity = None,
    transportCountry = None,
    grossMass = "1000",
    numberOfItems = 1,
    numberOfPackages = Some(1),
    traderAtDestination = TraderAtDestination("eori", "name", "streetAndNumber", "postcode", "city", "countryCode"),
    presentationOffice = "GB000060",
    seals = None,
    goodsItems = NonEmptyList(goodsItemMandatory, Nil),
    dateOfPreparation = LocalDate.now()
  )

  val mockUnloadingPermissionService: UnloadingPermissionService   = mock[UnloadingPermissionService]
  val mockUnloadingRemarksService: UnloadingRemarksService         = mock[UnloadingRemarksService]
  val mockAuditEventSubmissionService: AuditEventSubmissionService = mock[AuditEventSubmissionService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUnloadingPermissionService)
    reset(mockUnloadingRemarksService)
    reset(mockAuditEventSubmissionService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[UnloadingPermissionService].toInstance(mockUnloadingPermissionService),
        bind[UnloadingRemarksService].toInstance(mockUnloadingRemarksService),
        bind[AuditEventSubmissionService].toInstance(mockAuditEventSubmissionService)
      )

  "Check Your Answers Controller" - {

    "onPageLoad must" - {

      "return OK and the correct view for a GET" in {
        checkArrivalStatus()
        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(arrivalId).url)

        val result = route(app, request).value

        val view = injector.instanceOf[CheckYourAnswersView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(mrn, arrivalId, Nil)(request, messages).toString
      }

      "redirect to Session Expired for a GET if no existing data is found" in {
        checkArrivalStatus()
        setNoExistingUserAnswers()

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(arrivalId).url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
      }

      "return BAD REQUEST when unloading permission does not exist" in {
        checkArrivalStatus()
        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(None))

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(arrivalId).url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.ErrorController.badRequest().url
      }

    }

    "onSubmit must" - {

      "redirect to Confirmation on valid submission" in {
        checkArrivalStatus()
        setExistingUserAnswers(emptyUserAnswers)

        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

        when(mockUnloadingRemarksService.submit(any(), any(), any())(any())).thenReturn(Future.successful(Some(ACCEPTED)))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(arrivalId).url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.ConfirmationController.onPageLoad(arrivalId).url
      }

      "render the Bad Request page on failed submission (invalid response code)" in {
        checkArrivalStatus()

        setExistingUserAnswers(emptyUserAnswers)

        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

        when(mockUnloadingRemarksService.submit(any(), any(), any())(any())).thenReturn(Future.successful(Some(BAD_REQUEST)))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(arrivalId).url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.ErrorController.badRequest().url
      }

      "return Bad Request when backend returns 401" in {
        checkArrivalStatus()
        setExistingUserAnswers(emptyUserAnswers)

        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

        when(mockUnloadingRemarksService.submit(any(), any(), any())(any())).thenReturn(Future.successful(Some(UNAUTHORIZED)))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(arrivalId).url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.ErrorController.badRequest().url
      }

      "return Technical Difficulties page on internal failure" in {
        checkArrivalStatus()
        setExistingUserAnswers(emptyUserAnswers)

        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

        when(mockUnloadingRemarksService.submit(any(), any(), any())(any())).thenReturn(Future.successful(None))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(arrivalId).url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url
      }

      "return Technical Difficulties page when UnloadingPermission can't be retrieved" in {
        checkArrivalStatus()
        setExistingUserAnswers(emptyUserAnswers)

        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(arrivalId).url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url
      }
    }
  }
}
