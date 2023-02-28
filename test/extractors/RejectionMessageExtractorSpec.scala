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

import base.SpecBase
import generators.Generators
import models.ErrorType.IncorrectValue
import models._
import pages._

import java.time.LocalDate

class RejectionMessageExtractorSpec extends SpecBase with Generators {

  private val extractor = new RejectionMessageExtractor()

  private def rejectionMessage(pointer: ErrorPointer, originalValue: Option[String]) = UnloadingRemarksRejectionMessage(
    movementReferenceNumber = mrn,
    rejectionDate = LocalDate.parse("2020-01-01"),
    action = None,
    errors = Seq(FunctionalError(IncorrectValue, pointer, None, originalValue))
  )

  "must populate user answers" - {
    "when gross mass pointer" in {
      val result = extractor.apply(emptyUserAnswers, rejectionMessage(GrossWeightPointer, Some("1000")))
      result.get.getValue(GrossWeightPage) mustBe "1000"
    }

    "when number of items pointer" in {
      val result = extractor.apply(emptyUserAnswers, rejectionMessage(NumberOfItemsPointer, Some("1000")))
      result.get.getValue(TotalNumberOfItemsPage) mustBe 1000
    }

    "when unloading date pointer" in {
      val result = extractor.apply(emptyUserAnswers, rejectionMessage(UnloadingDatePointer, Some("20210101")))
      result.get.getValue(DateGoodsUnloadedPage) mustBe LocalDate.of(2021, 1, 1)
    }

    "when vehicle registration pointer" in {
      val result = extractor.apply(emptyUserAnswers, rejectionMessage(VehicleRegistrationPointer, Some("value")))
      result.get.getValue(VehicleNameRegistrationReferencePage) mustBe "value"
    }

    "when number of packages pointer" in {
      val result = extractor.apply(emptyUserAnswers, rejectionMessage(NumberOfPackagesPointer, Some("1000")))
      result.get.getValue(TotalNumberOfPackagesPage) mustBe 1000
    }
  }

  "must return existing user answers" - {
    "when default pointer" in {
      val userAnswers = emptyUserAnswers
      val result      = extractor.apply(userAnswers, rejectionMessage(DefaultPointer("value"), Some("1000")))
      result.get mustBe userAnswers
    }

    "when original attribute value is None" in {
      val userAnswers = emptyUserAnswers
      val result      = extractor.apply(userAnswers, rejectionMessage(GrossWeightPointer, None))
      result.get mustBe userAnswers
    }

    "when multiple errors" in {
      val rejectionMessage = UnloadingRemarksRejectionMessage(
        movementReferenceNumber = mrn,
        rejectionDate = LocalDate.parse("2020-01-01"),
        action = None,
        errors = Seq(
          FunctionalError(IncorrectValue, GrossWeightPointer, None, Some("1000")),
          FunctionalError(IncorrectValue, NumberOfItemsPointer, None, Some("2000"))
        )
      )

      val userAnswers = emptyUserAnswers
      val result      = extractor.apply(userAnswers, rejectionMessage)
      result.get mustBe userAnswers
    }

    "when number of items pointer but value cannot be parsed as Int" in {
      val userAnswers = emptyUserAnswers
      val result      = extractor.apply(userAnswers, rejectionMessage(NumberOfItemsPointer, Some("invalid")))
      result.get mustBe userAnswers
    }

    "when unloading date pointer but value cannot be parsed as LocalDate" in {
      val userAnswers = emptyUserAnswers
      val result      = extractor.apply(userAnswers, rejectionMessage(UnloadingDatePointer, Some("invalid")))
      result.get mustBe userAnswers
    }

    "when number of packages pointer but value cannot be parsed as Int" in {
      val userAnswers = emptyUserAnswers
      val result      = extractor.apply(userAnswers, rejectionMessage(NumberOfPackagesPointer, Some("invalid")))
      result.get mustBe userAnswers
    }
  }
}
