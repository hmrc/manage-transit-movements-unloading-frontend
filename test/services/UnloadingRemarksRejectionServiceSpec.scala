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

package services

import java.time.LocalDate

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.UnloadingConnector
import models.ErrorType.IncorrectValue
import models.{ArrivalId, DefaultPointer, FunctionalError, MessagesLocation, MessagesSummary, MovementReferenceNumber, UnloadingRemarksRejectionMessage}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import pages.{DateGoodsUnloadedPage, TotalNumberOfItemsPage, VehicleNameRegistrationReferencePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import utils.Date

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

  import UnloadingRemarksRejectionServiceSpec._

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

    "getRejectedValueAsString" - {

      "must return" - {

        "value from UnloadingRemarksRejectionMessage when there is no previously saved answers" in {

          when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
          when(mockConnector.getRejectionMessage(any())(any()))
            .thenReturn(Future.successful(Some(rejectionMessage(Some("some reference")))))

          setNoExistingUserAnswers()

          val unloadingRemarksRejectionService = app.injector.instanceOf[UnloadingRemarksRejectionService]

          val result = unloadingRemarksRejectionService.getRejectedValueAsString(arrivalId, None)(VehicleNameRegistrationReferencePage)
          result.futureValue mustBe Some("some reference")
        }

        "value from UnloadingRemarksRejectionMessage when there is a user answer but not from the required page" in {

          when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
          when(mockConnector.getRejectionMessage(any())(any()))
            .thenReturn(Future.successful(Some(rejectionMessage(Some("some reference")))))

          setNoExistingUserAnswers()

          val unloadingRemarksRejectionService = app.injector.instanceOf[UnloadingRemarksRejectionService]

          val result = unloadingRemarksRejectionService.getRejectedValueAsString(arrivalId, Some(emptyUserAnswers))(VehicleNameRegistrationReferencePage)
          result.futureValue mustBe Some("some reference")
        }

        "None when there is no previously saved answers and UnloadingRemarksRejectionMessage returns 'None'" in {
          val messagesSummary =
            MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))

          when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
          when(mockConnector.getRejectionMessage(any())(any()))
            .thenReturn(Future.successful(None))

          setExistingUserAnswers(emptyUserAnswers)

          val unloadingRemarksRejectionService = app.injector.instanceOf[UnloadingRemarksRejectionService]

          val result = unloadingRemarksRejectionService.getRejectedValueAsString(arrivalId, None)(VehicleNameRegistrationReferencePage)
          result.futureValue mustBe None
        }

        "value from user answers when there is a previously saved user answer" in {
          val originalValue = "some reference"
          val userAnswers   = emptyUserAnswers.set(VehicleNameRegistrationReferencePage, originalValue).get

          setExistingUserAnswers(userAnswers)

          val unloadingRemarksRejectionService = app.injector.instanceOf[UnloadingRemarksRejectionService]

          val result = unloadingRemarksRejectionService.getRejectedValueAsString(arrivalId, Some(userAnswers))(VehicleNameRegistrationReferencePage)
          result.futureValue mustBe Some("some reference")
        }

        "None when there is no previously saved answers and UnloadingRemarksRejectionMessage.originalAttributeValue is 'None'" in {
          val errors           = Seq(FunctionalError(IncorrectValue, DefaultPointer(""), None, None))
          val rejectionMessage = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)
          val messagesSummary =
            MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))

          when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
          when(mockConnector.getRejectionMessage(any())(any()))
            .thenReturn(Future.successful(Some(rejectionMessage)))

          setExistingUserAnswers(emptyUserAnswers)

          val unloadingRemarksRejectionService = app.injector.instanceOf[UnloadingRemarksRejectionService]

          val result = unloadingRemarksRejectionService.getRejectedValueAsString(arrivalId, None)(VehicleNameRegistrationReferencePage)
          result.futureValue mustBe None
        }

      }

    }

    "getRejectedValueAsInt" - {

      "must return" - {

        "value from UnloadingRemarksRejectionMessage when there is no previously saved answers" in {

          when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
          when(mockConnector.getRejectionMessage(any())(any()))
            .thenReturn(Future.successful(Some(rejectionMessage(Some("1000")))))

          setNoExistingUserAnswers()

          val unloadingRemarksRejectionService = app.injector.instanceOf[UnloadingRemarksRejectionService]

          val result = unloadingRemarksRejectionService.getRejectedValueAsInt(arrivalId, None)(TotalNumberOfItemsPage)
          result.futureValue.value mustBe 1000
        }

        "value from user answers when there is a previously saved user answer" in {
          val originalValue = 2000
          val userAnswers   = emptyUserAnswers.set(TotalNumberOfItemsPage, originalValue).get

          setExistingUserAnswers(userAnswers)

          val unloadingRemarksRejectionService = app.injector.instanceOf[UnloadingRemarksRejectionService]

          val result = unloadingRemarksRejectionService.getRejectedValueAsInt(arrivalId, Some(userAnswers))(TotalNumberOfItemsPage)
          result.futureValue.value mustBe 2000
        }
      }
    }

    "getRejectedValueAsDate" - {

      "must return" - {
        "value from UnloadingRemarksRejectionMessage when there is no previously saved answers" in {

          when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
          when(mockConnector.getRejectionMessage(any())(any()))
            .thenReturn(Future.successful(Some(rejectionMessage(Some("20200721")))))

          setNoExistingUserAnswers()

          val unloadingRemarksRejectionService = app.injector.instanceOf[UnloadingRemarksRejectionService]

          val result = unloadingRemarksRejectionService.getRejectedValueAsDate(arrivalId, None)(DateGoodsUnloadedPage)
          result.futureValue.value mustBe LocalDate.parse("2020-07-21")
        }

        "value from user answers when there is a previously saved user answer" in {
          val originalValue: LocalDate = Date.getDate("20200721").get
          val userAnswers              = emptyUserAnswers.set(DateGoodsUnloadedPage, originalValue).get

          setExistingUserAnswers(userAnswers)

          val unloadingRemarksRejectionService = app.injector.instanceOf[UnloadingRemarksRejectionService]

          val result = unloadingRemarksRejectionService.getRejectedValueAsDate(arrivalId, Some(userAnswers))(DateGoodsUnloadedPage)
          result.futureValue.value mustBe LocalDate.parse("2020-07-21")
        }
      }
    }
  }
}

object UnloadingRemarksRejectionServiceSpec {

  val mrn: MovementReferenceNumber = MovementReferenceNumber("19", "GB", "1234567890123")

  val rejectionMessage: Option[String] => UnloadingRemarksRejectionMessage = {
    rejectionReason =>
      val errors = Seq(FunctionalError(IncorrectValue, DefaultPointer(""), None, rejectionReason))
      UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)
  }

  val arrivalId: ArrivalId = ArrivalId(1)

  val messagesSummary: MessagesSummary =
    MessagesSummary(
      arrivalId,
      MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3",
                       Some("/movements/arrivals/1234/messages/4"),
                       Some("/movements/arrivals/1234/messages/5")
      )
    )
}
