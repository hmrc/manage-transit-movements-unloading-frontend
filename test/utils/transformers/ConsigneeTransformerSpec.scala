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
import generated.ConsigneeType04
import generators.Generators
import models.reference.Country
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{ConsigneeAddressPage, ConsigneeCountryPage, ConsigneeIdentifierPage, ConsigneeNamePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService

import scala.concurrent.Future

class ConsigneeTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[ConsigneeTransformer]

  private lazy val mockRefDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ReferenceDataService].toInstance(mockRefDataService)
      )

  "must transform data" - {

    val country = Country("GB", "United Kingdom")

    when(mockRefDataService.getCountryByCode(any())(any(), any()))
      .thenReturn(Future.successful(country))

    "when consignee defined" in {
      forAll(arbitrary[ConsigneeType04]) {
        consignee =>
          val result = transformer.transform(Some(consignee), hcIndex).apply(emptyUserAnswers).futureValue

          result.get(ConsigneeIdentifierPage(hcIndex)) mustBe consignee.identificationNumber
          result.get(ConsigneeNamePage(hcIndex)) mustBe consignee.name
          result
            .get(ConsigneeCountryPage(hcIndex))
            .map(
              countryResult => countryResult.description mustBe country.description
            )
      }
    }

    "when consignee undefined" in {
      val result = transformer.transform(None, hcIndex).apply(emptyUserAnswers).futureValue

      result.get(ConsigneeIdentifierPage(hcIndex)) must not be defined
      result.get(ConsigneeNamePage(hcIndex)) must not be defined
      result.get(ConsigneeAddressPage(hcIndex)) must not be defined
    }
  }
}
