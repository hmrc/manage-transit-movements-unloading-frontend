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

import generated.AddressType18
import models.{Index, UserAnswers}
import pages.incident.location.address.{AddressCityPage, AddressPostcodePage, AddressStreetAndNumberPage}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IncidentLocationAddressTransformer @Inject() (implicit ec: ExecutionContext) extends PageTransformer {

  def transform(address: Option[AddressType18], incidentIndex: Index)(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    address match {
      case Some(AddressType18(streetAndNumber, postcode, city)) =>
        lazy val pipeline: UserAnswers => Future[UserAnswers] =
          set(AddressStreetAndNumberPage(incidentIndex), streetAndNumber) andThen
            set(AddressPostcodePage(incidentIndex), postcode) andThen
            set(AddressCityPage(incidentIndex), city)

        pipeline(userAnswers)

      case None => Future.successful(userAnswers)
    }
}
