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

package utils.transformers

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.AddressType18
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.incident.location.address._
import play.api.inject.guice.GuiceApplicationBuilder

class IncidentLocationAddressTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[IncidentLocationAddressTransformer]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  override def beforeEach(): Unit =
    super.beforeEach()

  "must transform data" - {
    "when location address defined" in {
      forAll(arbitrary[AddressType18]) {
        addressType18 =>
          beforeEach()

          val result = transformer.transform(Some(addressType18), index).apply(emptyUserAnswers).futureValue

          result.getValue(AddressStreetAndNumberPage(index)) mustBe addressType18.streetAndNumber
          result.get(AddressPostcodePage(index)) mustBe addressType18.postcode
          result.getValue(AddressCityPage(index)) mustBe addressType18.city
      }
    }

    "when location address undefined" in {
      val result = transformer.transform(None, index).apply(emptyUserAnswers).futureValue

      result.get(AddressStreetAndNumberPage(index)) must not be defined
      result.get(AddressPostcodePage(index)) must not be defined
      result.get(AddressCityPage(index)) must not be defined
    }
  }
}
