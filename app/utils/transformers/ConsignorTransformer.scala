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

import generated.*
import models.{DynamicAddress, Index, UserAnswers}
import pages.houseConsignment.consignor.CountryPage as HouseCountryPage
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConsignorTransformer @Inject() (
  referenceDataService: ReferenceDataService
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(consignor: Option[ConsignorType04])(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.consignor.*
    consignor match {
      case Some(ConsignorType04(_, _, address)) =>
        set(CountryPage, address.map(_.country), referenceDataService.getCountry)
      case None =>
        Future.successful
    }
  }

  def transform(consignor: Option[ConsignorType05], hcIndex: Index)(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.{ConsignorIdentifierPage, ConsignorNamePage}
    consignor match {
      case Some(ConsignorType05(identificationNumber, name, address)) =>
        transformAddress(address, hcIndex) andThen
          set(ConsignorIdentifierPage(hcIndex), identificationNumber) andThen
          set(ConsignorNamePage(hcIndex), name)
      case None =>
        Future.successful
    }
  }

  private def transformAddress(address: Option[AddressType14], hcIndex: Index)(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] = {
    import pages.ConsignorAddressPage
    address match {
      case Some(AddressType14(streetAndNumber, postcode, city, country)) =>
        set(HouseCountryPage(hcIndex), country, referenceDataService.getCountry) andThen
          set(ConsignorAddressPage(hcIndex), DynamicAddress(streetAndNumber, city, postcode))
      case None =>
        Future.successful
    }
  }
}
