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
import generated.*
import generators.Generators
import models.DynamicAddress
import models.reference.Country
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verifyNoInteractions, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import services.ReferenceDataService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConsignorTransformerSpec extends SpecBase with BeforeAndAfterEach with ScalaCheckPropertyChecks with Generators {

  private lazy val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  private val transformer = new ConsignorTransformer(mockReferenceDataService)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
  }

  "must transform data" - {
    "at consignment level" - {
      import pages.consignor.*

      "when consignor defined" - {
        "when address defined" in {
          forAll(arbitrary[ConsignorType04], arbitrary[AddressType14], arbitrary[Country]) {
            (consignor, address, country) =>
              beforeEach()

              val input = consignor.copy(Address = Some(address))

              when(mockReferenceDataService.getCountry(eqTo(address.country))(any()))
                .thenReturn(Future.successful(country))

              val result = transformer.transform(Some(input)).apply(emptyUserAnswers).futureValue

              result.getValue(CountryPage) mustEqual country
          }
        }

        "when address undefined" in {
          forAll(arbitrary[ConsignorType04]) {
            consignor =>
              beforeEach()

              val input = consignor.copy(Address = None)

              val result = transformer.transform(Some(input)).apply(emptyUserAnswers).futureValue

              result.get(CountryPage) must not be defined

              verifyNoInteractions(mockReferenceDataService)
          }
        }
      }

      "when consignee undefined" in {
        val result = transformer.transform(None).apply(emptyUserAnswers).futureValue

        result.get(CountryPage) must not be defined

        verifyNoInteractions(mockReferenceDataService)
      }
    }

    "at house consignment level" - {
      import pages.{ConsignorAddressPage, ConsignorIdentifierPage, ConsignorNamePage}

      "when consignor defined" in {
        forAll(arbitrary[ConsignorType05], arbitrary[AddressType14], arbitrary[Country]) {
          (consignor, address, country) =>
            beforeEach()

            val input = consignor.copy(Address = Some(address))

            when(mockReferenceDataService.getCountry(eqTo(address.country))(any()))
              .thenReturn(Future.successful(country))

            val result = transformer.transform(Some(input), hcIndex).apply(emptyUserAnswers).futureValue

            result.get(ConsignorIdentifierPage(hcIndex)) mustEqual consignor.identificationNumber
            result.get(ConsignorNamePage(hcIndex)) mustEqual consignor.name
            result.get(ConsignorAddressPage(hcIndex)).value mustEqual DynamicAddress(address.streetAndNumber, address.city, address.postcode)
        }
      }

      "when consignee undefined" in {
        val result = transformer.transform(None, hcIndex).apply(emptyUserAnswers).futureValue

        result.get(ConsignorIdentifierPage(hcIndex)) must not be defined
        result.get(ConsignorNamePage(hcIndex)) must not be defined
        result.get(ConsignorAddressPage(hcIndex)) must not be defined
      }
    }
  }
}
