/*
 * Copyright 2022 HM Revenue & Customs
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

package viewModels

import java.time.LocalDate

import base.SpecBase
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.{DateGoodsUnloadedPage, GrossMassAmountPage, TotalNumberOfItemsPage, TotalNumberOfPackagesPage, VehicleNameRegistrationReferencePage}
import uk.gov.hmrc.viewmodels.Text.Literal

class RejectionCheckYourAnswersViewModelSpec extends AnyFreeSpec with Matchers with SpecBase {

  "RejectionCheckYourAnswersViewModel" - {

    "display vehicle name reference on check your answers" in {
      val userAnswers = emptyUserAnswers.set(VehicleNameRegistrationReferencePage, "reference").success.value
      val data        = RejectionCheckYourAnswersViewModel(userAnswers)

      data.sections.length mustBe 1
      data.sections.head.rows.head.value.content mustBe Literal("reference")
      data.sections.head.rows.head.actions.nonEmpty mustBe true
    }

    "display gross mass amount on check your answers" in {
      val userAnswers = emptyUserAnswers.set(GrossMassAmountPage, "10000").success.value
      val data        = RejectionCheckYourAnswersViewModel(userAnswers)

      data.sections.length mustBe 1
      data.sections.head.rows.head.value.content mustBe Literal("10000")
      data.sections.head.rows.head.actions.nonEmpty mustBe true
    }

    //noinspection ScalaStyle
    "display total number of packages on check your answers" in {
      val userAnswers = emptyUserAnswers.set(TotalNumberOfPackagesPage, 100).success.value
      val data        = RejectionCheckYourAnswersViewModel(userAnswers)

      data.sections.length mustBe 1
      data.sections.head.rows.head.value.content mustBe Literal("100")
      data.sections.head.rows.head.actions.nonEmpty mustBe true
    }

    "display total number of items on check your answers" in {
      val userAnswers = emptyUserAnswers.set(TotalNumberOfItemsPage, 100).success.value
      val data        = RejectionCheckYourAnswersViewModel(userAnswers)

      data.sections.length mustBe 1
      data.sections.head.rows.head.value.content mustBe Literal("100")
      data.sections.head.rows.head.actions.nonEmpty mustBe true
    }

    "display date goods unloaded on check your answers" in {
      val date        = LocalDate.parse("2020-07-21")
      val userAnswers = emptyUserAnswers.set(DateGoodsUnloadedPage, date).success.value
      val data        = RejectionCheckYourAnswersViewModel(userAnswers)

      data.sections.length mustBe 1
      data.sections.head.rows.head.value.content mustBe Literal("21 July 2020")
      data.sections.head.rows.head.actions.nonEmpty mustBe true
    }
  }

}
