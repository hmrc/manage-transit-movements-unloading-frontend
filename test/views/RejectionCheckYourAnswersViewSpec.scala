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

package views

import generators.{Generators, ViewModelGenerators}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewModels.sections.Section
import views.behaviours.SummaryListViewBehaviours
import views.html.RejectionCheckYourAnswersView

class RejectionCheckYourAnswersViewSpec extends SummaryListViewBehaviours with Generators with ViewModelGenerators {

  override val prefix: String = "checkYourAnswers"

  private val sections: Seq[Section] = listWithMaxLength[Section]().sample.value

  override def summaryLists: Seq[SummaryList] = sections.map(
    section => new SummaryList(section.rows)
  )

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[RejectionCheckYourAnswersView].apply(mrn, arrivalId, sections)(fakeRequest, messages)

  behave like pageWithBackLink

  behave like pageWithCaption(mrn.toString)

  behave like pageWithHeading()

  sections.foreach(_.sectionTitle.map {
    sectionTitle =>
      behave like pageWithContent("h2", sectionTitle)
  })

  behave like pageWithSummaryLists()

  behave like pageWithContent("h2", "Now send your unloading remarks")

  behave like pageWithContent("p", "By sending this you are confirming that the details you are providing are correct, to the best of your knowledge.")

  behave like pageWithSubmitButton("Confirm and send")

}
