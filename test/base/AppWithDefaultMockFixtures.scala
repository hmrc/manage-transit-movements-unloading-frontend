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
import models.P5.ArrivalMessageType.UnloadingPermission
import models.P5._
import models.UserAnswers
import navigation.houseConsignment.index.items.{HouseConsignmentItemNavigator, DocumentNavigator => ItemDocumentNavigator}
import navigation._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.{GuiceFakeApplicationFactory, GuiceOneAppPerSuite}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import repositories.SessionRepository
import services.P5.UnloadingPermissionMessageService

import java.time.LocalDateTime
import scala.concurrent.Future

trait AppWithDefaultMockFixtures extends BeforeAndAfterEach with GuiceOneAppPerSuite with GuiceFakeApplicationFactory with MockitoSugar {
  self: TestSuite =>

  final val mockSessionRepository: SessionRepository                            = mock[SessionRepository]
  final val mockDataRetrievalActionProvider                                     = mock[DataRetrievalActionProvider]
  final val mockUnloadingPermissionMessageService                               = mock[UnloadingPermissionMessageService]
  final val mockUnloadingPermissionActionProvider                               = mock[UnloadingPermissionActionProvider]
  protected val onwardRoute: Call                                               = Call("GET", "/foo")
  protected val fakeNavigator: Navigator                                        = new FakeNavigator(onwardRoute)
  protected val fakeDocumentNavigator: DocumentNavigator                        = new FakeDocumentNavigator(onwardRoute)
  protected val fakeItemDocumentNavigator: ItemDocumentNavigator                = new FakeItemDocumentNavigator(onwardRoute)
  protected val fakeHouseConsignmentIteNavigator: HouseConsignmentItemNavigator = new FakeHouseConsignmentItemNavigator(onwardRoute)

  final override def fakeApplication(): Application =
    guiceApplicationBuilder()
      .build()

  def guiceApplicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[UnloadingPermissionActionProvider].toInstance(mockUnloadingPermissionActionProvider),
        bind[UnloadingPermissionMessageService].toInstance(mockUnloadingPermissionMessageService),
        bind[DataRetrievalActionProvider].toInstance(mockDataRetrievalActionProvider),
        bind[Navigator].toInstance(fakeNavigator),
        bind[DocumentNavigator].toInstance(fakeDocumentNavigator),
        bind[ItemDocumentNavigator].toInstance(fakeItemDocumentNavigator),
        bind[HouseConsignmentItemNavigator].toInstance(fakeHouseConsignmentIteNavigator)
      )

  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
    reset(mockDataRetrievalActionProvider)
    reset(mockUnloadingPermissionMessageService)

    when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
      .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))

    when(mockUnloadingPermissionMessageService.getUnloadingPermissionMessage(any())(any(), any()))
      .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), ArrivalMessageType.UnloadingPermission, "foo/bar"))))

  }

  protected def setExistingUserAnswers(answers: UserAnswers): Unit =
    when(mockDataRetrievalActionProvider.apply(any())) thenReturn new FakeDataRetrievalAction(Some(answers))

  protected def setNoExistingUserAnswers(): Unit =
    when(mockDataRetrievalActionProvider.apply(any())) thenReturn new FakeDataRetrievalAction(None)

  // TODO - delete?
  protected def checkArrivalStatus(): Unit = ()
}
