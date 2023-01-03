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

package services

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.UnloadingConnector
import models.ErrorType.IncorrectValue
import models.{DefaultPointer, FunctionalError, MessagesLocation, MessagesSummary, UnloadingRemarksRejectionMessage}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

class UnloadingRemarksRejectionServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val mockConnector = mock[UnloadingConnector]

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingConnector].toInstance(mockConnector))

  private val messagesSummary: MessagesSummary =
    MessagesSummary(
      arrivalId,
      MessagesLocation(
        s"/movements/arrivals/${arrivalId.value}/messages/3",
        Some("/movements/arrivals/1234/messages/4"),
        Some("/movements/arrivals/1234/messages/5")
      )
    )

  "UnloadingRemarksRejectionService" - {

    "unloadingRemarksRejectionMessage" - {

      "must return " - {

        "UnloadingRemarksRejectionMessage" in {
          val errors              = Seq(FunctionalError(IncorrectValue, DefaultPointer(""), None, None))
          val notificationMessage = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)

          when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
          when(mockConnector.getRejectionMessage(any())(any()))
            .thenReturn(Future.successful(Some(notificationMessage)))

          setExistingUserAnswers(emptyUserAnswers)

          val unloadingRemarksRejectionService = app.injector.instanceOf[UnloadingRemarksRejectionService]

          unloadingRemarksRejectionService.unloadingRemarksRejectionMessage(arrivalId).futureValue mustBe Some(notificationMessage)
        }

        "None when getSummary fails to get rejection message" in {
          val messagesSummary =
            MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", None))
          when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))

          setExistingUserAnswers(emptyUserAnswers)

          val unloadingRemarksRejectionService = app.injector.instanceOf[UnloadingRemarksRejectionService]

          unloadingRemarksRejectionService.unloadingRemarksRejectionMessage(arrivalId).futureValue mustBe None
        }

        "None when getSummary call fails to get MessagesSummary" in {

          when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(None))

          setExistingUserAnswers(emptyUserAnswers)

          val unloadingRemarksRejectionService = app.injector.instanceOf[UnloadingRemarksRejectionService]

          unloadingRemarksRejectionService.unloadingRemarksRejectionMessage(arrivalId).futureValue mustBe None
        }
      }
    }
  }
}
