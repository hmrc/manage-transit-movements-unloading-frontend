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
import cats.data.NonEmptyList
import models.{EoriNumber, Seals, TraderAtDestination, UnloadingPermission, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, JsString, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UnloadingPermissionService

import java.time.LocalDate
import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

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

  private val mockUnloadingPermissionService = mock[UnloadingPermissionService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUnloadingPermissionService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingPermissionService].toInstance(mockUnloadingPermissionService))

  private val nextPage = routes.UnloadingGuidanceController.onPageLoad(arrivalId).url

  "Index Controller" - {
    "must redirect to onward route for a GET when there are no UserAnswers and prepopulate data" in {
      checkArrivalStatus()
      val seals                        = Seals(1, Seq("Seal1", "Seal2"))
      val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(seals))

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
        .thenReturn(Future.successful(Some(unloadingPermissionWithSeals)))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      setNoExistingUserAnswers()

      val request                = FakeRequest(GET, routes.IndexController.onPageLoad(arrivalId).url)
      val result: Future[Result] = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual nextPage

      verify(mockSessionRepository).set(userAnswersCaptor.capture())

      userAnswersCaptor.getValue.mrn.toString mustBe unloadingPermission.movementReferenceNumber
      userAnswersCaptor.getValue.id mustBe arrivalId
      userAnswersCaptor.getValue.eoriNumber mustBe EoriNumber("id")
      userAnswersCaptor.getValue.prepopulateData mustBe Json.obj("seals" -> JsArray(Seq(JsString("Seal1"), JsString("Seal2"))))
    }

    "must redirect to onward route for a when there are UserAnswers" in {
      checkArrivalStatus()
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(arrivalId).url)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual nextPage
    }

    "must redirect to session expired when no response for arrivalId" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
        .thenReturn(Future.successful(None))

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(arrivalId).url)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to session expired when bad mrn received" in {
      checkArrivalStatus()
      val badUnloadingPermission = UnloadingPermission(
        movementReferenceNumber = "",
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

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
        .thenReturn(Future.successful(Some(badUnloadingPermission)))

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(arrivalId).url)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
