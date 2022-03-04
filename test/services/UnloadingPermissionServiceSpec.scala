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

import base.SpecBase
import connectors.UnloadingConnector
import generators.Generators
import models.{MessagesLocation, MessagesSummary, Seals, UnloadingPermission, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnloadingPermissionServiceSpec extends SpecBase with BeforeAndAfterEach with Generators {

  private val mockConnector = mock[UnloadingConnector]

  private val service = new UnloadingPermissionServiceImpl(mockConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  "UnloadingPermissionService" - {

    "getUnloadingPermission" - {

      "must return UnloadingPermission when IE0043 message exists" in {

        val unloadingPermissionMessage = arbitrary[UnloadingPermission].sample.value

        val messagesSummary =
          MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))

        when(mockConnector.getSummary(arrivalId)).thenReturn(Future.successful(Some(messagesSummary)))

        when(mockConnector.getUnloadingPermission(any())(any())).thenReturn(Future.successful(Some(unloadingPermissionMessage)))

        service.getUnloadingPermission(arrivalId).futureValue mustBe a[Some[_]]
      }

      "must return None when IE0043 message is not returned" in {

        val messagesSummary =
          MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))

        when(mockConnector.getSummary(arrivalId)).thenReturn(Future.successful(Some(messagesSummary)))

        when(mockConnector.getUnloadingPermission(any())(any())).thenReturn(Future.successful(None))

        service.getUnloadingPermission(arrivalId).futureValue mustBe None
      }

      "must return None when getSummary returns None" in {

        when(mockConnector.getSummary(arrivalId)).thenReturn(Future.successful(None))

        verify(mockConnector, times(0)).getUnloadingPermission(any())(any())

        service.getUnloadingPermission(arrivalId).futureValue mustBe None
      }

    }

    "convertSeals" - {

      "return the same userAnswers when given an ID with no Seals" in {

        val unloadingPermissionMessage = arbitrary[UnloadingPermission].sample.value

        val messagesSummary =
          MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))

        when(mockConnector.getSummary(arrivalId)).thenReturn(Future.successful(Some(messagesSummary)))

        when(mockConnector.getUnloadingPermission(any())(any()))
          .thenReturn(Future.successful(Some(unloadingPermissionMessage.copy(seals = None))))

        val userAnswers = UserAnswers(arrivalId, mrn, eoriNumber, Json.obj())
        service.convertSeals(userAnswers).futureValue mustBe Some(userAnswers)
      }

      "return updated userAnswers when given an ID with seals" in {

        val unloadingPermissionMessage = arbitrary[UnloadingPermission].sample.value

        val messagesSummary =
          MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))

        when(mockConnector.getSummary(arrivalId)).thenReturn(Future.successful(Some(messagesSummary)))

        when(mockConnector.getUnloadingPermission(any())(any()))
          .thenReturn(Future.successful(Some(unloadingPermissionMessage.copy(seals = Some(Seals(2, Seq("Seals01", "Seals02")))))))
        val userAnswers = UserAnswers(arrivalId, mrn, eoriNumber, Json.obj())
        val userAnswersWithSeals =
          UserAnswers(arrivalId, mrn, eoriNumber, Json.obj("seals" -> Seq("Seals01", "Seals02")), lastUpdated = userAnswers.lastUpdated)
        service.convertSeals(userAnswers).futureValue mustBe Some(userAnswersWithSeals)
      }

      "accept unloadingPermission arg and update user answers if seals exist" in {

        val unloadingPermissionMessage = arbitrary[UnloadingPermission].sample.value

        val userAnswers = UserAnswers(arrivalId, mrn, eoriNumber, Json.obj())
        val userAnswersWithSeals =
          UserAnswers(arrivalId, mrn, eoriNumber, Json.obj("seals" -> Seq("Seals01", "Seals02")), lastUpdated = userAnswers.lastUpdated)
        service.convertSeals(userAnswers, unloadingPermissionMessage.copy(seals = Some(Seals(2, Seq("Seals01", "Seals02"))))) mustBe Some(userAnswersWithSeals)
      }

    }

  }

}
