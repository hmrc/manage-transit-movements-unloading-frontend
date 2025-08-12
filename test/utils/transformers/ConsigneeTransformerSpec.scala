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
import generated.{AddressType14, ConsigneeType01, ConsigneeType05}
import generators.Generators
import models.reference.Country
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verifyNoInteractions, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.{
  ConsigneeAddressPage as ItemConsigneeAddressPage,
  ConsigneeCountryPage as ItemConsigneeCountryPage,
  ConsigneeIdentifierPage as ItemConsigneeIdentifierPage,
  ConsigneeNamePage as ItemConsigneeNamePage
}
import pages.{ConsigneeAddressPage, ConsigneeCountryPage, ConsigneeIdentifierPage, ConsigneeNamePage}
import services.ReferenceDataService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConsigneeTransformerSpec extends SpecBase with BeforeAndAfterEach with ScalaCheckPropertyChecks with Generators {

  private lazy val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  private val transformer = new ConsigneeTransformer(mockReferenceDataService)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
  }

  "must transform data" - {

    val country = Country("GB", "United Kingdom")

    "at consignment level" - {
      "when consignee defined" in {
        forAll(arbitrary[ConsigneeType05], arbitrary[AddressType14], arbitrary[Country]) {
          (consignor, address, country) =>
            beforeEach()

            val input = consignor.copy(Address = Some(address))

            when(mockReferenceDataService.getCountry(eqTo(address.country))(any()))
              .thenReturn(Future.successful(country))

            val result = transformer.transform(Some(input)).apply(emptyUserAnswers).futureValue

            result.getValue(pages.consignee.CountryPage) mustEqual country
        }
      }

      "when consignee undefined" in {
        val result = transformer.transform(None).apply(emptyUserAnswers).futureValue

        result.get(pages.consignee.CountryPage) must not be defined
        verifyNoInteractions(mockReferenceDataService)
      }
    }

    "at house consignment level" - {

      "when consignee defined" in {
        when(mockReferenceDataService.getCountry(any())(any()))
          .thenReturn(Future.successful(country))

        forAll(arbitrary[ConsigneeType05]) {
          consignee =>
            val result = transformer.transform(Some(consignee), hcIndex).apply(emptyUserAnswers).futureValue

            result.get(ConsigneeIdentifierPage(hcIndex)) mustEqual consignee.identificationNumber
            result.get(ConsigneeNamePage(hcIndex)) mustEqual consignee.name
            result
              .get(ConsigneeCountryPage(hcIndex))
              .map(
                countryResult => countryResult.description mustEqual country.description
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

    "at item level" - {

      "when consignee defined" in {
        when(mockReferenceDataService.getCountry(any())(any()))
          .thenReturn(Future.successful(country))

        forAll(arbitrary[ConsigneeType01]) {
          consignee =>
            val result = transformer.transform(Some(consignee), hcIndex, itemIndex).apply(emptyUserAnswers).futureValue

            result.get(ItemConsigneeIdentifierPage(hcIndex, itemIndex)) mustEqual consignee.identificationNumber
            result.get(ItemConsigneeNamePage(hcIndex, itemIndex)) mustEqual consignee.name
            result
              .get(ItemConsigneeCountryPage(hcIndex, itemIndex))
              .map(
                countryResult => countryResult.description mustEqual country.description
              )
        }
      }

      "when consignee undefined" in {
        val result = transformer.transform(None, hcIndex, itemIndex).apply(emptyUserAnswers).futureValue

        result.get(ItemConsigneeIdentifierPage(hcIndex, itemIndex)) must not be defined
        result.get(ItemConsigneeNamePage(hcIndex, itemIndex)) must not be defined
        result.get(ItemConsigneeAddressPage(hcIndex, itemIndex)) must not be defined
      }
    }
  }
}
