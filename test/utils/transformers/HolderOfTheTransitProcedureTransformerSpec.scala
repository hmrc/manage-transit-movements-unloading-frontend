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
import generated.{AddressType10, HolderOfTheTransitProcedureType06}
import generators.Generators
import models.reference.Country
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.holderOfTheTransitProcedure.CountryPage
import services.ReferenceDataService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HolderOfTheTransitProcedureTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val refDataService = mock[ReferenceDataService]
  private val transformer    = new HolderOfTheTransitProcedureTransformer(refDataService)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(refDataService)
  }

  val hotP = HolderOfTheTransitProcedureType06(Some("identificationNumber"),
                                               Some("TIRHolderIdentificationNumber"),
                                               "name",
                                               AddressType10("streetAndNumber", Some("postcode"), "city", "GB")
  )

  "must update country page when it is in ref data" in {
    val country = Country("GB", "Great Britain")
    when(refDataService.getCountryByCode("GB")).thenReturn(Future(country))

    val result = transformer.transform(Some(hotP)).apply(emptyUserAnswers).futureValue
    result.getValue(CountryPage) mustBe country
  }

  "return failure if ref data call fails" in {
    when(refDataService.getCountryByCode("GB")).thenReturn(Future.failed(new RuntimeException("test failure")))

    val result = transformer.transform(Some(hotP)).apply(emptyUserAnswers)
    whenReady(result.failed) {
      t =>
        t.getMessage mustBe "test failure"
    }
  }
}
