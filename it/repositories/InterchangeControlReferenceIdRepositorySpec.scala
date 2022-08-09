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
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.DateTimeService
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

class InterchangeControlReferenceIdRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfterEach
    with GuiceOneAppPerSuite
    with IntegrationPatience
    with DefaultPlayMongoRepositorySupport[InterchangeControlReference] {

  private val mockTimeService: DateTimeService = mock[DateTimeService]

  override protected def repository = new InterchangeControlReferenceIdRepository(mongoComponent, mockTimeService)

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[DateTimeService].toInstance(mockTimeService)
    )
    .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockTimeService)
  }

  private val date: String = "20190101"

  "InterchangeControlReferenceIdRepository" - {

    "must generate correct InterchangeControlReference when no record exists within the database" in {

      when(mockTimeService.dateFormatted).thenReturn(date)

      val first = repository.nextInterchangeControlReferenceId().futureValue

      first mustBe InterchangeControlReference(date, 1)

      val second = repository.nextInterchangeControlReferenceId().futureValue

      second mustBe InterchangeControlReference(date, 2)
    }

    "must generate correct InterchangeControlReference when the collection already has a document in the database" in {

      when(mockTimeService.dateFormatted).thenReturn(date)

      insert(InterchangeControlReference(mockTimeService.dateFormatted, 1)).futureValue

      val first  = repository.nextInterchangeControlReferenceId().futureValue
      val second = repository.nextInterchangeControlReferenceId().futureValue

      first.index mustEqual 2
      second.index mustEqual 3
    }
  }
}
