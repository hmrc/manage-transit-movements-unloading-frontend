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

package repositories

import config.FrontendAppConfig
import generated._
import itbase.ItSpecBase
import models.{ArrivalId, EoriNumber, MovementReferenceNumber, SensitiveFormats, UserAnswers}
import play.api.libs.json.Json
import scalaxb.XMLCalendar
import services.DateTimeService
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositorySpec extends ItSpecBase with DefaultPlayMongoRepositorySupport[UserAnswers] {

  private val config: FrontendAppConfig        = app.injector.instanceOf[FrontendAppConfig]
  private val dateTimeService: DateTimeService = app.injector.instanceOf[DateTimeService]

  implicit private val sensitiveFormats: SensitiveFormats = app.injector.instanceOf[SensitiveFormats]

  override protected val repository: SessionRepository = new SessionRepository(mongoComponent, config, dateTimeService)

  private val ie043: CC043CType = CC043CType(
    messageSequence1 = MESSAGESequence(
      messageSender = "",
      messageRecipient = "",
      preparationDateAndTime = XMLCalendar("2022-02-03T08:45:00.000000"),
      messageIdentification = "",
      messageType = CC043C,
      correlationIdentifier = None
    ),
    TransitOperation = TransitOperationType14(
      MRN = "MRN",
      declarationType = None,
      declarationAcceptanceDate = None,
      security = "0",
      reducedDatasetIndicator = Number0
    ),
    CustomsOfficeOfDestinationActual = CustomsOfficeOfDestinationActualType03(
      referenceNumber = "cooda"
    ),
    HolderOfTheTransitProcedure = None,
    TraderAtDestination = TraderAtDestinationType03(
      identificationNumber = "tad"
    ),
    CTLControl = None,
    Consignment = None,
    attributes = Map.empty
  )

  private val userAnswers1 = UserAnswers(
    id = ArrivalId("0"),
    mrn = MovementReferenceNumber("99IT9876AB889012096"),
    eoriNumber = EoriNumber("EoriNumber1"),
    ie043Data = ie043,
    data = Json.obj("bar" -> "foo"),
    lastUpdated = dateTimeService.now
  )

  private val userAnswers2 = UserAnswers(
    id = ArrivalId("1"),
    mrn = MovementReferenceNumber("18GB0000601001EB15"),
    eoriNumber = EoriNumber("EoriNumber2"),
    ie043Data = ie043,
    data = Json.obj("foo" -> "bar"),
    lastUpdated = dateTimeService.now
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    insert(userAnswers1).futureValue
    insert(userAnswers2).futureValue
  }

  "SessionRepository" - {

    "get" - {

      "must return UserAnswers when given an ArrivalId and EoriNumber" in {

        val result = repository.get(ArrivalId("0"), EoriNumber("EoriNumber1")).futureValue

        result.value.id `mustBe` userAnswers1.id
        result.value.eoriNumber `mustBe` userAnswers1.eoriNumber
        result.value.ie043Data `mustBe` userAnswers1.ie043Data
        result.value.data `mustBe` userAnswers1.data
      }

      "must return None when no UserAnswers match ArrivalId" in {

        val result = repository.get(ArrivalId("3"), EoriNumber("EoriNumber1")).futureValue

        result `mustBe` None
      }

      "must return None when no UserAnswers match EoriNumber" in {

        val result = repository.get(ArrivalId("0"), EoriNumber("InvalidEori")).futureValue

        result `mustBe` None
      }
    }

    "set" - {

      "must create new document when given valid UserAnswers" in {

        val userAnswers = UserAnswers(
          id = ArrivalId("3"),
          mrn = MovementReferenceNumber("18GB0000601001EBD1"),
          eoriNumber = EoriNumber("EoriNumber3"),
          ie043Data = ie043,
          data = Json.obj("bar" -> "foo"),
          lastUpdated = dateTimeService.now
        )

        val setResult = repository.set(userAnswers).futureValue

        val getResult = repository.get(ArrivalId("3"), EoriNumber("EoriNumber3")).futureValue.value

        setResult `mustBe` true
        getResult.id `mustBe` userAnswers.id
        getResult.eoriNumber `mustBe` userAnswers.eoriNumber
        getResult.ie043Data `mustBe` userAnswers.ie043Data
        getResult.data `mustBe` userAnswers.data
      }
    }

    "remove" - {

      "must remove document when given a valid ArrivalId" in {

        repository.get(ArrivalId("0"), EoriNumber("EoriNumber1")).futureValue `mustBe` defined

        repository.remove(ArrivalId("0")).futureValue

        repository.get(ArrivalId("0"), EoriNumber("EoriNumber1")).futureValue `must` not `be` defined
      }
    }
  }

}
