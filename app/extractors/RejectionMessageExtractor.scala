/*
 * Copyright 2023 HM Revenue & Customs
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

package extractors

import models._
import pages._
import play.api.libs.json.Writes
import utils.{Date, IntValue}

import scala.util.{Success, Try}

class RejectionMessageExtractor {

  def apply(userAnswers: UserAnswers, rejectionMessage: UnloadingRemarksRejectionMessage): Try[UserAnswers] =
    rejectionMessage.errors match {
      case error :: Nil =>
        def setValue[T](page: QuestionPage[T])(format: String => Option[T])(implicit writes: Writes[T]): Try[UserAnswers] =
          (for {
            value          <- error.originalAttributeValue
            formattedValue <- format(value)
          } yield userAnswers.set(page, formattedValue)).getOrElse(Success(userAnswers))

        error.pointer match {
          case GrossWeightPointer         => setValue(GrossWeightPage)(Some(_))
          case NumberOfItemsPointer       => setValue(TotalNumberOfItemsPage)(IntValue.getInt)
          case UnloadingDatePointer       => setValue(DateGoodsUnloadedPage)(Date.getDate)
          case VehicleRegistrationPointer => setValue(VehicleIdentificationNumberPage)(Some(_))
          case NumberOfPackagesPointer    => setValue(TotalNumberOfPackagesPage)(Some(_))
          case _: DefaultPointer          => Success(userAnswers)
        }
      case _ => Success(userAnswers)
    }

}
