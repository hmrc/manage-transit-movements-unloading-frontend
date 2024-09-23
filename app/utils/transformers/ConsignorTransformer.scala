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
import generated._
import models.{DynamicAddress, Index, UserAnswers}
import pages.houseConsignment.consignor.{CountryPage => HouseCountryPage}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConsignorTransformer @Inject() (
  referenceDataConnector: ReferenceDataConnector
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(consignor: Option[ConsignorType05])(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.consignor._

    userAnswers =>
      consignor match {
        case Some(ConsignorType05(_, _, address)) =>
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

  def transform(consignor: Option[ConsignorType06], hcIndex: Index)(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.{ConsignorIdentifierPage, ConsignorNamePage}

    userAnswers =>
      consignor match {
        case Some(ConsignorType06(identificationNumber, name, address)) =>
          for {
            userAnswers <- {
              val pipeline: UserAnswers => Future[UserAnswers] =
                transformAddress(address, hcIndex) andThen
                  set(ConsignorIdentifierPage(hcIndex), identificationNumber) andThen
                  set(ConsignorNamePage(hcIndex), name)

              pipeline(userAnswers)
            }
          } yield userAnswers

        case None =>
          Future.successful(userAnswers)
      }
  }

  private def transformAddress(address: Option[AddressType07], hcIndex: Index)(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] = userAnswers => {
    import pages.ConsignorAddressPage

    address match {

      case Some(AddressType07(streetAndNumber, postcode, city, country)) =>
        referenceDataConnector.getCountry(country).flatMap {
          countryVal =>
            val pipeline: UserAnswers => Future[UserAnswers] =
              set(HouseCountryPage(hcIndex), countryVal) andThen
                set(ConsignorAddressPage(hcIndex), DynamicAddress(streetAndNumber, city, postcode))
            pipeline(userAnswers)
        }

      case None => Future.successful(userAnswers)
    }
  }
}
