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

import base.SpecBase
import generated.CountryOfRoutingOfConsignmentType02
import generators.Generators
import models.Index
import models.reference.Country
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.countriesOfRouting.CountryOfRoutingPage
import pages.sections.CountryOfRoutingSection
import services.ReferenceDataService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CountriesOfRoutingTransformerSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private lazy val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  private val transformer = new CountriesOfRoutingTransformer(mockReferenceDataService)

  "must transform data" in {
    val countriesOfRouting = arbitrary[Seq[CountryOfRoutingOfConsignmentType02]].sample.value

    countriesOfRouting.map {
      countryOfRouting =>
        when(mockReferenceDataService.getCountry(eqTo(countryOfRouting.country))(any()))
          .thenReturn(
            Future.successful(Country(code = countryOfRouting.country, description = "describe me"))
          )
    }

    val result = transformer.transform(countriesOfRouting).apply(emptyUserAnswers).futureValue

    countriesOfRouting.zipWithIndex.map {
      case (countryOfRouting, i) =>
        result.getSequenceNumber(CountryOfRoutingSection(Index(i))) mustBe countryOfRouting.sequenceNumber
        result.getValue(CountryOfRoutingPage(Index(i))).code mustBe countryOfRouting.country
        result.getValue(CountryOfRoutingPage(Index(i))).description mustBe "describe me"
    }
  }
}
