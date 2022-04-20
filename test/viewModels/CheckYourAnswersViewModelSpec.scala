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

import base.SpecBase
import models.UserAnswers
import pages._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

import java.time.LocalDate

class CheckYourAnswersViewModelSpec extends SpecBase {

  "CheckYourAnswersViewModel" - {

    "contain date goods unloaded" in {

      val date                     = LocalDate.of(2020: Int, 3: Int, 12: Int)
      val userAnswers: UserAnswers = emptyUserAnswers.set(DateGoodsUnloadedPage, date).success.value
      val sections                 = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections.head.rows.head.value.content mustBe Text("12 March 2020")
    }
    "contain vehicle registration details with new user answers" in {
      val userAnswers = emptyUserAnswers.set(VehicleNameRegistrationReferencePage, "vehicle reference").success.value
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).rows.head.value.content mustBe Text("vehicle reference")
      sections(2).rows.head.actions.isEmpty mustBe false
    }
    "contain transport country details from unloading permission" in {
      val sections = new CheckYourAnswersViewModel()(emptyUserAnswers)

      sections.length mustBe 3
      sections(2).rows(1).value.content mustBe Text("United Kingdom")
    }
    "contain gross mass amount details from unloading permission" in {
      val sections = new CheckYourAnswersViewModel()(emptyUserAnswers)

      sections.length mustBe 3
      sections(2).rows.head.value.content mustBe Text("1000")
      sections(2).rows.head.actions.isEmpty mustBe false
    }
    "contain gross mass details" in {
      val userAnswers = emptyUserAnswers.set(GrossMassAmountPage, "500").success.value
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).rows.head.value.content mustBe Text("500")
      sections(2).rows.head.actions.isEmpty mustBe false
    }
    "contain number of items details" in {
      val userAnswers = emptyUserAnswers.set(TotalNumberOfItemsPage, 10).success.value
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).rows(1).value.content mustBe Text("10")
      sections(2).rows.head.actions.isEmpty mustBe false
    }
    "contain number of items details with details from unloading permission" in {
      val sections = new CheckYourAnswersViewModel()(emptyUserAnswers)

      sections.length mustBe 3
      sections(2).rows(1).value.content mustBe Text("1")
      sections(2).rows.head.actions.isEmpty mustBe false
    }
    "contain number of packages details with details from unloading permission" in {
      val sections = new CheckYourAnswersViewModel()(emptyUserAnswers)

      sections.length mustBe 3
      sections(2).rows(2).value.content mustBe Text("1")
      sections(2).rows.head.actions.isEmpty mustBe false
    }
    "contain number of packages details" in {
      val userAnswers = emptyUserAnswers.set(TotalNumberOfPackagesPage, 11).success.value
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).rows(2).value.content mustBe Text("11")
      sections(2).rows.head.actions.isEmpty mustBe false
    }
    "contain item details" in {
      val userAnswers = emptyUserAnswers
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).rows(3).value.content mustBe Text("Flowers")
    }
    "contain comments details" in {
      val userAnswers = emptyUserAnswers.set(ChangesToReportPage, "Test comment").success.value
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).rows(4).value.content mustBe Text("Test comment")
    }
  }
}
