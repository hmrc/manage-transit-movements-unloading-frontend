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
import models.{ArrivalId, EoriNumber, MovementReferenceNumber, UserAnswers}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import services.DateTimeService
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfterEach
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with OptionValues
    with DefaultPlayMongoRepositorySupport[UserAnswers] {

  private val config: FrontendAppConfig        = app.injector.instanceOf[FrontendAppConfig]
  private val dateTimeService: DateTimeService = app.injector.instanceOf[DateTimeService]

  override protected val repository = new SessionRepository(mongoComponent, config, dateTimeService)

  private val userAnswers1 =
    UserAnswers(ArrivalId("0"), MovementReferenceNumber("99IT9876AB88901209").get, EoriNumber("EoriNumber1"), Json.obj("foo" -> "bar"), dateTimeService.now)

  private val userAnswers2 =
    UserAnswers(ArrivalId("1"), MovementReferenceNumber("18GB0000601001EB15").get, EoriNumber("EoriNumber2"), Json.obj("bar" -> "foo"), dateTimeService.now)

  override def beforeEach(): Unit = {
    super.beforeEach()
    insert(userAnswers1).futureValue
    insert(userAnswers2).futureValue
  }

  "SessionRepository" - {

    "get" - {

      "must return UserAnswers when given an ArrivalId and EoriNumber" in {

        val result = repository.get(ArrivalId("0"), EoriNumber("EoriNumber1")).futureValue

        result.value.id mustBe userAnswers1.id
        result.value.eoriNumber mustBe userAnswers1.eoriNumber
        result.value.data mustBe userAnswers1.data
      }

      "must return None when no UserAnswers match ArrivalId" in {

        val result = repository.get(ArrivalId("3"), EoriNumber("EoriNumber1")).futureValue

        result mustBe None
      }

      "must return None when no UserAnswers match EoriNumber" in {

        val result = repository.get(ArrivalId("0"), EoriNumber("InvalidEori")).futureValue

        result mustBe None
      }
    }

    "set" - {

      "must create new document when given valid UserAnswers" in {

        val userAnswers =
          UserAnswers(
            ArrivalId("3"),
            MovementReferenceNumber("18GB0000601001EBD1").get,
            EoriNumber("EoriNumber3"),
            Json.obj("foo" -> "bar"),
            dateTimeService.now
          )

        val setResult = repository.set(userAnswers).futureValue

        val getResult = repository.get(ArrivalId("3"), EoriNumber("EoriNumber3")).futureValue.value

        setResult mustBe true
        getResult.id mustBe userAnswers.id
        getResult.eoriNumber mustBe userAnswers.eoriNumber
        getResult.data mustBe userAnswers.data
      }
    }

    "remove" - {

      "must remove document when given a valid ArrivalId" in {

        repository.get(ArrivalId("0"), EoriNumber("EoriNumber1")).futureValue mustBe defined

        repository.remove(ArrivalId("0")).futureValue

        repository.get(ArrivalId("0"), EoriNumber("EoriNumber1")).futureValue must not be defined
      }
    }
  }

}
