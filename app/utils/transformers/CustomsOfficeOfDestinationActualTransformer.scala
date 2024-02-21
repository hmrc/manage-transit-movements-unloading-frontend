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

import generated.CustomsOfficeOfDestinationActualType03
import models.UserAnswers
import models.reference.CustomsOfficeOfDestinationActual
import pages.CustomsOfficeOfDestinationActualPage
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsOfficeOfDestinationActualTransformer @Inject() (referenceDataService: ReferenceDataService)(implicit ec: ExecutionContext)
    extends PageTransformer {

  private case class TempCusCode(referenceNumber: String, description: String)

  def transform(customsOfficeOfDestination: CustomsOfficeOfDestinationActualType03)(implicit
    hc: HeaderCarrier
  ): Future[UserAnswers] => Future[UserAnswers] = userAnswers => {

    lazy val referenceDataLookup =
      referenceDataService.getCustomsOfficeByCode(customsOfficeOfDestination.referenceNumber).map {
        customsOffice =>
          TempCusCode(
            customsOfficeOfDestination.referenceNumber,
            customsOffice.map(_.name).getOrElse(customsOfficeOfDestination.referenceNumber)
          )
      }

    referenceDataLookup flatMap {
      case TempCusCode(referenceNumber, description) =>
        val pipeline: UserAnswers => Future[UserAnswers] =
          set(CustomsOfficeOfDestinationActualPage(), CustomsOfficeOfDestinationActual(referenceNumber, description))

        pipeline(userAnswers)
    }
  }
}
