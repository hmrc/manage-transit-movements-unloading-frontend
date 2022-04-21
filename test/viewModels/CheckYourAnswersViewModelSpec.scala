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
import models.reference.Country
import pages._
import queries.GoodsItemsQuery
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

import java.time.LocalDate

class CheckYourAnswersViewModelSpec extends SpecBase {

  "CheckYourAnswersViewModel" - {

    "contain date goods unloaded" in {
      val date                     = LocalDate.of(2020: Int, 3: Int, 12: Int)
      val userAnswers: UserAnswers = emptyUserAnswers.setValue(DateGoodsUnloadedPage, date)
      val sections                 = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections.head.rows.head.value.content mustBe Text("12 March 2020")
    }

    "contain vehicle registration details" in {
      val userAnswers = emptyUserAnswers.setValue(VehicleNameRegistrationReferencePage, "vehicle reference")
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).rows.head.value.content mustBe Text("vehicle reference")
      sections(2).rows.head.actions.isEmpty mustBe false
    }

    "contain transport country details" in {
      val userAnswers = emptyUserAnswers.setValue(VehicleRegistrationCountryPage, Country("GB", "United Kingdom"))
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).rows.head.value.content mustBe Text("United Kingdom")
    }

    "contain gross mass details" in {
      val userAnswers = emptyUserAnswers.setValue(GrossMassAmountPage, "500")
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).rows.head.value.content mustBe Text("500")
    }

    "contain number of items details" in {
      val userAnswers = emptyUserAnswers.setValue(TotalNumberOfItemsPage, 10)
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).rows.head.value.content mustBe Text("10")
    }

    "contain number of packages details" in {
      val userAnswers = emptyUserAnswers.setValue(TotalNumberOfPackagesPage, 11)
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).rows.head.value.content mustBe Text("11")
    }

    "contain item details" in {
      val userAnswers = emptyUserAnswers.setValue(GoodsItemsQuery, Seq("Flowers"))
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).rows.head.value.content mustBe Text("Flowers")
    }

    "contain comments details" in {
      val userAnswers = emptyUserAnswers.setValue(ChangesToReportPage, "Test comment")
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).rows.head.value.content mustBe Text("Test comment")
    }
  }
}
