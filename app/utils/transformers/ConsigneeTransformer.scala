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

import generated.{AddressType01, AddressType14, ConsigneeType01, ConsigneeType05}
import models.{DynamicAddress, Index, UserAnswers}
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConsigneeTransformer @Inject() (
  referenceDataService: ReferenceDataService
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(consignee: Option[ConsigneeType05])(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    consignee.mapWithSets {
      case ConsigneeType05(_, _, address) =>
        transformAddress(address)
    }

  private def transformAddress(address: Option[AddressType14])(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.consignee.CountryPage
    address.mapWithSets {
      case AddressType14(_, _, _, country) =>
        set(CountryPage, country, referenceDataService.getCountry)
    }
  }

  def transform(consignee: Option[ConsigneeType05], hcIndex: Index)(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] = {
    import pages.{ConsigneeIdentifierPage, ConsigneeNamePage}
    consignee.mapWithSets {
      case ConsigneeType05(identificationNumber, name, address) =>
        set(ConsigneeIdentifierPage(hcIndex), identificationNumber) andThen
          set(ConsigneeNamePage(hcIndex), name) andThen
          transformAddress(address, hcIndex)
    }
  }

  private def transformAddress(address: Option[AddressType14], hcIndex: Index)(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] = {
    import pages.{ConsigneeAddressPage, ConsigneeCountryPage}
    address.mapWithSets {
      case AddressType14(streetAndNumber, postcode, city, country) =>
        set(ConsigneeCountryPage(hcIndex), country, referenceDataService.getCountry) andThen
          set(ConsigneeAddressPage(hcIndex), DynamicAddress(streetAndNumber, city, postcode))
    }
  }

  def transform(consignee: Option[ConsigneeType01], hcIndex: Index, itemIndex: Index)(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] = {
    import pages.houseConsignment.index.items.*
    consignee.mapWithSets {
      case ConsigneeType01(identificationNumber, name, address) =>
        set(ConsigneeIdentifierPage(hcIndex, itemIndex), identificationNumber) andThen
          set(ConsigneeNamePage(hcIndex, itemIndex), name) andThen
          transformAddress(address, hcIndex, itemIndex)
    }
  }

  private def transformAddress(address: Option[AddressType01], hcIndex: Index, itemIndex: Index)(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] =
    import pages.houseConsignment.index.items.*
    address.mapWithSets {
      case AddressType01(streetAndNumber, postcode, city, country) =>
        set(ConsigneeCountryPage(hcIndex, itemIndex), country, referenceDataService.getCountry) andThen
          set(ConsigneeAddressPage(hcIndex, itemIndex), DynamicAddress(streetAndNumber, city, postcode))
    }
}
