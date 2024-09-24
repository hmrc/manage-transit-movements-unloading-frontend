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

import connectors.ReferenceDataConnector
import generated.{AddressType07, AddressType09, ConsigneeType03, ConsigneeType04}
import models.{DynamicAddress, Index, UserAnswers}
import pages.houseConsignment.index.items.{
  ConsigneeAddressPage => ItemConsigneeAddressPage,
  ConsigneeCountryPage => ItemConsigneeCountryPage,
  ConsigneeIdentifierPage => ItemConsigneeIdentifierPage,
  ConsigneeNamePage => ItemConsigneeNamePage
}
import pages.{ConsigneeAddressPage, ConsigneeCountryPage, ConsigneeIdentifierPage, ConsigneeNamePage}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConsigneeTransformer @Inject() (
  referenceDataConnector: ReferenceDataConnector
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(consignee: Option[ConsigneeType04])(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.consignee._

    userAnswers =>
      consignee match {
        case Some(ConsigneeType04(_, _, address)) =>
          for {
            country <- address.map(_.country).lookup(referenceDataConnector.getCountry)
            userAnswers <- {
              val pipeline: UserAnswers => Future[UserAnswers] =
                set(CountryPage, country)

              pipeline(userAnswers)
            }
          } yield userAnswers
        case None =>
          Future.successful(userAnswers)
      }
  }

  def transform(consignee: Option[ConsigneeType04], hcIndex: Index)(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] = userAnswers =>
    consignee match {
      case Some(ConsigneeType04(identificationNumber, name, address)) =>
        val pipeline: UserAnswers => Future[UserAnswers] =
          set(ConsigneeIdentifierPage(hcIndex), identificationNumber) andThen
            set(ConsigneeNamePage(hcIndex), name) andThen
            transformAddress(address, hcIndex)

        pipeline(userAnswers)
      case None =>
        Future.successful(userAnswers)
    }

  private def transformAddress(address: Option[AddressType07], hcIndex: Index)(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] = userAnswers =>
    address match {
      case Some(AddressType07(streetAndNumber, postcode, city, country)) =>
        referenceDataConnector.getCountry(country).flatMap {
          countryVal =>
            val pipeline: UserAnswers => Future[UserAnswers] =
              set(ConsigneeCountryPage(hcIndex), countryVal) andThen
                set(ConsigneeAddressPage(hcIndex), DynamicAddress(streetAndNumber, city, postcode))
            pipeline(userAnswers)
        }

      case None => Future.successful(userAnswers)
    }

  def transform(consignee: Option[ConsigneeType03], hcIndex: Index, itemIndex: Index)(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] = userAnswers =>
    consignee match {
      case Some(ConsigneeType03(identificationNumber, name, address)) =>
        val pipeline: UserAnswers => Future[UserAnswers] =
          set(ItemConsigneeIdentifierPage(hcIndex, itemIndex), identificationNumber) andThen
            set(ItemConsigneeNamePage(hcIndex, itemIndex), name) andThen
            transformItemConsigneeAddress(address, hcIndex, itemIndex)

        pipeline(userAnswers)
      case None =>
        Future.successful(userAnswers)
    }

  private def transformItemConsigneeAddress(address: Option[AddressType09], hcIndex: Index, itemIndex: Index)(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] = userAnswers =>
    address match {
      case Some(AddressType09(streetAndNumber, postcode, city, country)) =>
        referenceDataConnector.getCountry(country).flatMap {
          countryVal =>
            val pipeline: UserAnswers => Future[UserAnswers] =
              set(ItemConsigneeCountryPage(hcIndex, itemIndex), countryVal) andThen
                set(ItemConsigneeAddressPage(hcIndex, itemIndex), DynamicAddress(streetAndNumber, city, postcode))
            pipeline(userAnswers)
        }

      case None => Future.successful(userAnswers)
    }
}
