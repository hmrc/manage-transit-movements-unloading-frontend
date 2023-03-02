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

package controllers.actions

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.UnloadingConnector
import controllers.routes
import models.requests.IdentifierRequest
import models.response.ResponseArrival
import models.{ArrivalStatus, EoriNumber}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Results._
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ArrivalStatusActionSpec extends SpecBase with BeforeAndAfterEach with AppWithDefaultMockFixtures {

  val mockConnector: UnloadingConnector = mock[UnloadingConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockConnector)
  }

  private def fakeOkResult[A]: A => Future[Result] =
    _ => Future.successful(Ok("fake ok result value"))

  val validStatus = Seq(
    ArrivalStatus.UnloadingPermission,
    ArrivalStatus.UnloadingRemarksRejected,
    ArrivalStatus.UnloadingRemarksSubmittedNegativeAcknowledgement
  )

  "a check cancellation status action" - {
    "will get a 200 and will load the correct page when the arrival status is valid" in {

      validStatus.map {
        validArrivalStatus =>
          val mockArrivalResponse: ResponseArrival =
            ResponseArrival(
              arrivalId,
              validArrivalStatus
            )

          when(mockConnector.getArrival(any())(any())).thenReturn(Future.successful(Some(mockArrivalResponse)))

          val checkArrivalStatusProvider = (new CheckArrivalStatusProvider(mockConnector)(implicitly))(arrivalId)

          val testRequest = IdentifierRequest(FakeRequest(GET, "/"), EoriNumber("eori"))

          val result: Future[Result] = checkArrivalStatusProvider.invokeBlock(testRequest, fakeOkResult)

          status(result) mustEqual OK
          contentAsString(result) mustEqual "fake ok result value"
      }
    }
  }

  "will get a 400 and will load the cannot cancel page when arrival status is not valid" in {
    val mockArrivalResponse: ResponseArrival =
      ResponseArrival(
        arrivalId,
        ArrivalStatus.OtherStatus
      )
    when(mockConnector.getArrival(any())(any())).thenReturn(Future.successful(Some(mockArrivalResponse)))

    val checkArrivalStatusProvider = (new CheckArrivalStatusProvider(mockConnector)(implicitly))(arrivalId)
    val testRequest                = IdentifierRequest(FakeRequest(GET, "/"), EoriNumber("eori"))
    val result: Future[Result]     = checkArrivalStatusProvider.invokeBlock(testRequest, fakeOkResult)

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual routes.CannotSendUnloadingRemarksController.badRequest().url
  }

  "will get a 404 and will load the departure not found page when the departure record is not found" in {

    when(mockConnector.getArrival(any())(any())).thenReturn(Future.successful(None))

    val checkArrivalStatusProvider = (new CheckArrivalStatusProvider(mockConnector)(implicitly))(arrivalId)
    val testRequest                = IdentifierRequest(FakeRequest(GET, "/"), EoriNumber("eori"))
    val result: Future[Result]     = checkArrivalStatusProvider.invokeBlock(testRequest, fakeOkResult)

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual routes.CannotSendUnloadingRemarksController.notFound().url
  }

}
