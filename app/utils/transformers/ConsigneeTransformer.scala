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

import generated.{AddressType07, ConsigneeType04}
import models.{Address, Index, UserAnswers}
import pages.{ConsigneeAddressPage, ConsigneeIdentifierPage, ConsigneeNamePage}
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConsigneeTransformer @Inject() (
  referenceDataConnector: ReferenceDataService
)(implicit ec: ExecutionContext)
    extends PageTransformer {

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
        referenceDataConnector
          .getCountryByCode(country)
          .flatMap(
            country => userAnswers.set(ConsigneeAddressPage(hcIndex), Address(streetAndNumber, city, postcode, country)).asFuture
          )
      case None =>
        Future.successful(userAnswers)
    }
}
