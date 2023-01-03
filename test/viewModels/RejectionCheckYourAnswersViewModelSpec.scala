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

package viewModels

import base.SpecBase
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

import java.time.LocalDate

class RejectionCheckYourAnswersViewModelSpec extends AnyFreeSpec with Matchers with SpecBase {

  "RejectionCheckYourAnswersViewModel" - {

    "display vehicle name reference on check your answers" in {
      val userAnswers = emptyUserAnswers.setValue(VehicleNameRegistrationReferencePage, "reference")
      val sections    = new RejectionCheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 1
      sections.head.rows.head.value.content mustBe Text("reference")
      sections.head.rows.head.actions.nonEmpty mustBe true
    }

    "display gross mass amount on check your answers" in {
      val userAnswers = emptyUserAnswers.setValue(GrossMassAmountPage, "10000")
      val sections    = new RejectionCheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 1
      sections.head.rows.head.value.content mustBe Text("10000")
      sections.head.rows.head.actions.nonEmpty mustBe true
    }

    //noinspection ScalaStyle
    "display total number of packages on check your answers" in {
      val userAnswers = emptyUserAnswers.setValue(TotalNumberOfPackagesPage, 100)
      val sections    = new RejectionCheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 1
      sections.head.rows.head.value.content mustBe Text("100")
      sections.head.rows.head.actions.nonEmpty mustBe true
    }

    "display total number of items on check your answers" in {
      val userAnswers = emptyUserAnswers.setValue(TotalNumberOfItemsPage, 100)
      val sections    = new RejectionCheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 1
      sections.head.rows.head.value.content mustBe Text("100")
      sections.head.rows.head.actions.nonEmpty mustBe true
    }

    "display date goods unloaded on check your answers" in {
      val date        = LocalDate.parse("2020-07-21")
      val userAnswers = emptyUserAnswers.setValue(DateGoodsUnloadedPage, date)
      val sections    = new RejectionCheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 1
      sections.head.rows.head.value.content mustBe Text("21 July 2020")
      sections.head.rows.head.actions.nonEmpty mustBe true
    }
  }

}
