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
import play.twirl.api.HtmlFormat
import viewModels.CheckYourAnswersViewModel
import viewModels.sections.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import views.html.CheckYourAnswersView

class CheckYourAnswersViewSpec extends CheckYourAnswersViewBehaviours with Generators {

  override val prefix: String = "checkYourAnswers"

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable =
    injector.instanceOf[CheckYourAnswersView].apply(mrn, arrivalId, checkYourAnswersViewModel)(fakeRequest, messages)

  val checkYourAnswersViewModel: CheckYourAnswersViewModel = new CheckYourAnswersViewModel(sections, false)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

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

  "must render link for discrepancies when AddCommentsYesNoPage is true" - {

    val checkYourAnswersViewModel: CheckYourAnswersViewModel = new CheckYourAnswersViewModel(sections, true)

    val view: HtmlFormat.Appendable =
      injector.instanceOf[CheckYourAnswersView].apply(mrn, arrivalId, checkYourAnswersViewModel)(fakeRequest, messages)

    val doc = parseView(view)

    behave like pageWithLink(
      doc,
      "unloadingFindings",
      "Back to discrepancies between the transit and unloading permission",
      controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId).url
    )
  }
}
