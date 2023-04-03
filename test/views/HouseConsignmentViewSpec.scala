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
import viewModels.HouseConsignmentViewModel
import viewModels.sections.Section
import views.behaviours.DetailsListViewBehaviours
import views.html.HouseConsignmentView

class HouseConsignmentViewSpec extends DetailsListViewBehaviours with Generators {

  override val prefix: String = "unloadingFindings"

  lazy val sections: Seq[Section]                          = arbitrary[List[Section]].sample.value
  val houseConsignmentViewModel: HouseConsignmentViewModel = new HouseConsignmentViewModel(sections, sections)

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[HouseConsignmentView].apply(mrn, arrivalId, houseConsignmentViewModel, index)(fakeRequest, messages)

  override def summaryLists: Seq[SummaryList] = sections.map(
    section => SummaryList(section.rows)
  )

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(mrn.toString)

  behave like pageWithHeading()

  behave like pageWithSections()

  behave like pageWithFormAction(controllers.routes.HouseConsignmentController.onSubmit(arrivalId).url)

  behave like pageWithSubmitButton("Back to summary")

  "must render section titles when rows are non-empty" - {
    sections.foreach(_.sectionTitle.map {
      sectionTitle =>
        behave like pageWithContent("span", sectionTitle)
    })
  }

}
