/*
 * Copyright 2024 HM Revenue & Customs
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

package itbase

import models.{ArrivalId, EoriNumber, MovementReferenceNumber, UserAnswers}
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant

trait ItSpecBase extends AnyFreeSpec with Matchers with ScalaFutures with OptionValues with GuiceOneServerPerSuite with IntegrationPatience {

  val arrivalId: ArrivalId         = ArrivalId("AB123")
  val mrn: MovementReferenceNumber = MovementReferenceNumber("19GB1234567890123")
  val eoriNumber: EoriNumber       = EoriNumber("id")

  def emptyUserAnswers: UserAnswers = UserAnswers(arrivalId, mrn, eoriNumber, Json.obj(), Json.obj(), Instant.now())

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def guiceApplicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure("metrics.enabled" -> false)

  final override def fakeApplication(): Application =
    guiceApplicationBuilder()
      .build()

}
