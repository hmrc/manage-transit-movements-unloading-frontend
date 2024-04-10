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
import models.Mode
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import viewModels.UnloadingFindingsViewModel
import viewModels.sections.Section.AccordionSection
import views.behaviours.DetailsListViewBehaviours
import views.html.UnloadingFindingsView

class UnloadingFindingsViewSpec extends DetailsListViewBehaviours with Generators {

  override val prefix: String = "unloadingFindings"

  private val mode = arbitrary[Mode].sample.value

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[UnloadingFindingsView].apply(mrn, arrivalId, unloadingFindingsViewModel, mode)(fakeRequest, messages)

  private val unloadingFindingsViewModel: UnloadingFindingsViewModel = new UnloadingFindingsViewModel(sections)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading()

  behave like pageWithSections()

  behave like pageWithFormAction(controllers.routes.UnloadingFindingsController.onSubmit(arrivalId, mode).url)

  behave like pageWithSubmitButton("Continue")

  "must render section titles when rows are non-empty" - {
    sections.foreach(_.sectionTitle.map {
      sectionTitle =>
        behave like pageWithContent("span", sectionTitle)
    })
  }

  "must render 'No information provided' when rows and children are empty" in {
    val accordionId                = "foo"
    val sections                   = Seq(AccordionSection(rows = Nil, children = Nil, id = Some(accordionId)))
    val unloadingFindingsViewModel = new UnloadingFindingsViewModel(sections)

    val view = injector.instanceOf[UnloadingFindingsView].apply(mrn, arrivalId, unloadingFindingsViewModel, mode)(fakeRequest, messages)

    val doc       = parseView(view)
    val accordion = doc.getElementById(accordionId)

    accordion.text() mustBe "No information provided"
  }
}
