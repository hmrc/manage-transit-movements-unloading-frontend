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

package views

import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewModels.CheckYourAnswersViewModel
import viewModels.sections.Section
import views.behaviours.SummaryListViewBehaviours
import views.html.CheckYourAnswersView

class CheckYourAnswersViewSpec extends SummaryListViewBehaviours with Generators {

  override val prefix: String = "checkYourAnswers"

  lazy val sections: Seq[Section] = arbitrary[List[Section]].sample.value

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[CheckYourAnswersView].apply(mrn, arrivalId, checkYourAnswersViewModel)(fakeRequest, messages)

  val checkYourAnswersViewModel: CheckYourAnswersViewModel = new CheckYourAnswersViewModel(sections)

  override def summaryLists: Seq[SummaryList] = sections.map(
    section => SummaryList(section.rows)
  )

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(mrn.toString)

  behave like pageWithHeading()

  behave like pageWithSummaryLists()

  behave like pageWithFormAction(controllers.routes.CheckYourAnswersController.onSubmit(arrivalId).url)

  behave like pageWithSubmitButton("Confirm and send")

  "must render section titles when rows are non-empty" - {
    sections.foreach(_.sectionTitle.map {
      sectionTitle =>
        behave like pageWithContent("h2", sectionTitle)
    })
  }

}
