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
import matchers.JsonMatchers
import models.{TraderAtDestination, UnloadingPermission}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.UnloadingPermissionService

import java.time.LocalDate
import scala.concurrent.Future

class UnloadingSummaryControllerSpec extends SpecBase with AppWithDefaultMockFixtures with JsonMatchers {

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

  "UnloadingSummary Controller" - {

    "return OK and the correct view for a GET" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(GET, routes.UnloadingSummaryController.onPageLoad(arrivalId).url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      //TODO fix the test with all the other controller tests
      val expectedJson = Json.obj("mrn" -> mrn, "arrivalId" -> arrivalId)

      templateCaptor.getValue mustEqual "unloadingSummary.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
