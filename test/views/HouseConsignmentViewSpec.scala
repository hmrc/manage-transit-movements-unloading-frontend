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
import viewModels.HouseConsignmentViewModel
import viewModels.sections.Section
import viewModels.sections.Section.StaticSection
import views.behaviours.DetailsListViewBehaviours
import views.html.HouseConsignmentView

class HouseConsignmentViewSpec extends DetailsListViewBehaviours with Generators {

  override val prefix: String = "houseConsignment"

  private val section: Section = arbitrary[StaticSection].sample.value

  override lazy val sections: Seq[Section] = Seq(section)

  val houseConsignmentViewModel: HouseConsignmentViewModel = new HouseConsignmentViewModel(section)

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[HouseConsignmentView].apply(mrn, arrivalId, houseConsignmentViewModel, index)(fakeRequest, messages)

  behave like pageWithTitle(args = "1")

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(args = "1")

  behave like pageWithContent("p", "Change the declaration details below to match the transit movement that arrived.")

  behave like pageWithChildSections()

  behave like pageWithSubmitButton("Back to summary")

  "must render child section titles" - {
    section.children.foreach(_.sectionTitle.map {
      sectionTitle =>
        behave like pageWithContent("span", sectionTitle)
    })
  }
}
