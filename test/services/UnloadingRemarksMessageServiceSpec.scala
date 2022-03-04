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

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.UnloadingConnector
import generators.MessagesModelGenerators
import models.messages.UnloadingRemarksRequest
import models.{MessagesLocation, MessagesSummary}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.inject.bind
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnloadingRemarksMessageServiceSpec extends SpecBase with AppWithDefaultMockFixtures with MessagesModelGenerators {

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

  "UnloadingRemarksMessageService" - {
    "must return UnloadingRemarksMessage for the input arrivalId" in {

      val unloadingRemarksRequest = arbitrary[UnloadingRemarksRequest].sample.value
      val messagesSummary =
        MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))

      when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
      when(mockConnector.getUnloadingRemarksMessage(any())(any()))
        .thenReturn(Future.successful(Some(unloadingRemarksRequest)))

      setExistingUserAnswers(emptyUserAnswers)

      val unloadingRemarksMessageService = app.injector.instanceOf[UnloadingRemarksMessageService]

      unloadingRemarksMessageService.unloadingRemarksMessage(arrivalId).futureValue.value mustBe unloadingRemarksRequest
    }

    "must return None when getSummary call fails to get MessagesSummary" in {

      when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(None))

      setExistingUserAnswers(emptyUserAnswers)

      val unloadingRemarksMessage = app.injector.instanceOf[UnloadingRemarksMessageService]

      unloadingRemarksMessage.unloadingRemarksMessage(arrivalId).futureValue mustBe None
    }
  }
}
