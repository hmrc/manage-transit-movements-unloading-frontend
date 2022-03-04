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

package base

import controllers.actions._
import models.UserAnswers
import models.requests.IdentifierRequest
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.{GuiceFakeApplicationFactory, GuiceOneAppPerSuite}
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{ActionFilter, Call, Result}
import play.api.test.Helpers
import repositories.SessionRepository
import uk.gov.hmrc.nunjucks.NunjucksRenderer

import scala.concurrent.{ExecutionContext, Future}

trait AppWithDefaultMockFixtures extends BeforeAndAfterEach with GuiceOneAppPerSuite with GuiceFakeApplicationFactory with MockitoSugar {
  self: TestSuite =>

  override def beforeEach(): Unit =
    Mockito.reset(
      mockRenderer,
      mockSessionRepository,
      mockDataRetrievalActionProvider,
      mockCheckArrivalStatusProvider
    )

  final val mockRenderer: NunjucksRenderer                             = mock[NunjucksRenderer]
  final val mockSessionRepository: SessionRepository                   = mock[SessionRepository]
  final val mockDataRetrievalActionProvider                            = mock[DataRetrievalActionProvider]
  final val mockCheckArrivalStatusProvider: CheckArrivalStatusProvider = mock[CheckArrivalStatusProvider]

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

    when(mockCheckArrivalStatusProvider.apply(any())).thenReturn(fakeCheckArrivalStatusAction)
  }

  protected val onwardRoute: Call = Call("GET", "/foo")

  protected val fakeNavigator: Navigator = new FakeNavigator(onwardRoute)

  def guiceApplicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[NunjucksRenderer].toInstance(mockRenderer),
        bind[CheckArrivalStatusProvider].toInstance(mockCheckArrivalStatusProvider),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[DataRetrievalActionProvider].toInstance(mockDataRetrievalActionProvider),
        bind[MessagesApi].toInstance(Helpers.stubMessagesApi()),
        bind[Navigator].toInstance(fakeNavigator)
      )
}
