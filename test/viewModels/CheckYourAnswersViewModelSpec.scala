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
import models.reference.Country
import models.{Seal, UserAnswers}
import pages._
import queries.{GoodsItemsQuery, SealsQuery}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}

import java.time.LocalDate

class CheckYourAnswersViewModelSpec extends SpecBase {

  "CheckYourAnswersViewModel" - {

    "contain date goods unloaded" in {
      val date                     = LocalDate.of(2020: Int, 3: Int, 12: Int)
      val userAnswers: UserAnswers = emptyUserAnswers.setValue(DateGoodsUnloadedPage, date)
      val sections                 = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections.head.sectionTitle mustNot be(defined)
      sections.head.rows.head.value.content mustBe Text("12 March 2020")
    }

    "contain seals" in {
      val userAnswers: UserAnswers = emptyUserAnswers
        .setValue(SealsQuery,
                  Seq(
                    Seal("Seal 1", removable = false),
                    Seal("Seal 2", removable = true)
                  )
        )
      val sections = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(1).sectionTitle.get mustBe "Check official customs seals"
      sections(1).rows.head.value.content mustBe HtmlContent("Seal 1<br>Seal 2")
    }

    "contain can seals be read" in {
      val userAnswers: UserAnswers = emptyUserAnswers.setValue(CanSealsBeReadPage, true)
      val sections                 = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(1).sectionTitle.get mustBe "Check official customs seals"
      sections(1).rows.head.value.content mustBe Text("Yes")
    }

    "contain are any seals broken" in {
      val userAnswers: UserAnswers = emptyUserAnswers.setValue(CanSealsBeReadPage, false)
      val sections                 = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(1).sectionTitle.get mustBe "Check official customs seals"
      sections(1).rows.head.value.content mustBe Text("No")
    }

    "contain vehicle registration details" in {
      val userAnswers = emptyUserAnswers.setValue(VehicleIdentificationNumberPage, "vehicle reference")
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).sectionTitle.get mustBe "What you found when unloading"
      sections(2).rows.head.value.content mustBe Text("vehicle reference")
    }

    "contain transport country details" in {
      val userAnswers = emptyUserAnswers.setValue(VehicleRegistrationCountryPage, Country("GB", "United Kingdom"))
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).sectionTitle.get mustBe "What you found when unloading"
      sections(2).rows.head.value.content mustBe Text("United Kingdom")
    }

    "contain gross mass details" in {
      val userAnswers = emptyUserAnswers.setValue(GrossMassAmountPage, "500")
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).sectionTitle.get mustBe "What you found when unloading"
      sections(2).rows.head.value.content mustBe Text("500")
    }

    "contain number of items details" in {
      val userAnswers = emptyUserAnswers.setValue(TotalNumberOfItemsPage, 10)
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).sectionTitle.get mustBe "What you found when unloading"
      sections(2).rows.head.value.content mustBe Text("10")
    }

    "contain number of packages details" in {
      val userAnswers = emptyUserAnswers.setValue(TotalNumberOfPackagesPage, 11)
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).sectionTitle.get mustBe "What you found when unloading"
      sections(2).rows.head.value.content mustBe Text("11")
    }

    "contain item details" in {
      val userAnswers = emptyUserAnswers.setValue(GoodsItemsQuery, Seq("Flowers"))
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).sectionTitle.get mustBe "What you found when unloading"
      sections(2).rows.head.value.content mustBe Text("Flowers")
    }

    "contain comments details" in {
      val userAnswers = emptyUserAnswers.setValue(ChangesToReportPage, "Test comment")
      val sections    = new CheckYourAnswersViewModel()(userAnswers)

      sections.length mustBe 3
      sections(2).sectionTitle.get mustBe "What you found when unloading"
      sections(2).rows.head.value.content mustBe Text("Test comment")
    }
  }
}
