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

package repositories

import models.messages.InterchangeControlReference
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.running
import reactivemongo.play.json.collection.Helpers.idWrites
import reactivemongo.play.json.collection.JSONCollection
import services.DateTimeService
import services.mocks.MockDateTimeService

import scala.concurrent.ExecutionContext.Implicits.global

class InterchangeControlReferenceIdRepositorySpec
  extends AnyFreeSpec
    with Matchers
    with MongoSuite
    with ScalaFutures
    with BeforeAndAfterEach
    with IntegrationPatience
    with MockDateTimeService {

  private val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder()
    .overrides(
      bind[DateTimeService].toInstance(mockTimeService)
    )

  override def beforeEach(): Unit = {
    super.beforeEach()
    database.flatMap(_.drop()).futureValue
  }

  "InterchangeControlReferenceIdRepository" - {

    "must generate correct InterchangeControlReference when no record exists within the database" in {

      val app = appBuilder.build()

      running(app) {
        mockDateFormatted("20190101")

        val service: InterchangeControlReferenceIdRepository = app.injector.instanceOf[InterchangeControlReferenceIdRepository]

        val first = service.nextInterchangeControlReferenceId().futureValue

        first mustBe InterchangeControlReference("20190101", 1)

        val second = service.nextInterchangeControlReferenceId().futureValue

        second mustBe InterchangeControlReference("20190101", 2)
      }
    }

    "must generate correct InterchangeControlReference when the collection already has a document in the database" in {

      mockDateFormatted("20190101")

      database.flatMap {
        db =>
          db.collection[JSONCollection]("interchange-control-reference-ids")
            .insert(ordered = false)
            .one(
              Json.obj(
                "_id"        -> mockTimeService.dateFormatted,
                "last-index" -> 1
              ))
      }.futureValue

      val app = appBuilder.build()

      running(app) {
        val service: InterchangeControlReferenceIdRepository = app.injector.instanceOf[InterchangeControlReferenceIdRepository]

        val first = service.nextInterchangeControlReferenceId().futureValue
        val second = service.nextInterchangeControlReferenceId().futureValue

        first.index mustEqual 2
        second.index mustEqual 3
      }
    }
  }

}
