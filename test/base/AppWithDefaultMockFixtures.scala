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

package base

import controllers.actions._
import models.P5._
import models.requests.IdentifierRequest
import models.{MovementReferenceNumber, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.{GuiceFakeApplicationFactory, GuiceOneAppPerSuite}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{ActionFilter, Call, Result}
import repositories.SessionRepository
import services.P5.UnloadingPermissionMessageService

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

trait AppWithDefaultMockFixtures extends BeforeAndAfterEach with GuiceOneAppPerSuite with GuiceFakeApplicationFactory with MockitoSugar {
  self: TestSuite =>

  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
    reset(mockDataRetrievalActionProvider)
    reset(mockUnloadingPermissionMessageService)

    when(mockUnloadingPermissionMessageService.getUnloadingPermissionMessage(any(), any())(any(), any()))
      .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), ArrivalMessageType.UnloadingPermission, "foo/bar"))))

    when(mockUnloadingPermissionMessageService.getUnloadingPermission(any(), any())(any(), any())).thenReturn(
      Future.successful(
        Some(
          IE043Data(
            MessageData(
              LocalDateTime.now(),
              TransitOperation = TransitOperation(MovementReferenceNumber("99IT9876AB88901209").get),
              TraderAtDestination = TraderAtDestination("identificationNumber"),
              Consignment = Consignment(None, None, List.empty),
              CustomsOfficeOfDestinationActual = CustomsOfficeOfDestinationActual("referenceNumber")
            )
          )
        )
      )
    )

  }

  final val mockSessionRepository: SessionRepository = mock[SessionRepository]
  final val mockDataRetrievalActionProvider          = mock[DataRetrievalActionProvider]
  final val mockUnloadingPermissionMessageService    = mock[UnloadingPermissionMessageService]
  final val mockUnloadingPermissionActionProvider    = mock[UnloadingPermissionActionProvider]

  final override def fakeApplication(): Application =
    guiceApplicationBuilder()
      .build()

  protected def setExistingUserAnswers(answers: UserAnswers): Unit =
    when(mockDataRetrievalActionProvider.apply(any())) thenReturn new FakeDataRetrievalAction(Some(answers))

  protected def setNoExistingUserAnswers(): Unit =
    when(mockDataRetrievalActionProvider.apply(any())) thenReturn new FakeDataRetrievalAction(None)

  protected def checkArrivalStatus(): Unit = {
    val fakeCheckArrivalStatusAction = new ActionFilter[IdentifierRequest] {
      override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] =
        Future.successful(None)

      override protected def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.global
    }

  }

  protected val onwardRoute: Call = Call("GET", "/foo")

  protected val fakeNavigator: Navigator = new FakeNavigator(onwardRoute)

  def guiceApplicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[UnloadingPermissionActionProvider].toInstance(mockUnloadingPermissionActionProvider),
        bind[UnloadingPermissionMessageService].toInstance(mockUnloadingPermissionMessageService),
        bind[DataRetrievalActionProvider].toInstance(mockDataRetrievalActionProvider),
        bind[Navigator].toInstance(fakeNavigator)
      )
}
