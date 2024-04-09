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
import viewModels.HouseConsignmentViewModel
import views.behaviours.DetailsListViewBehaviours
import views.html.HouseConsignmentView

class HouseConsignmentViewSpec extends DetailsListViewBehaviours with Generators {

  override val prefix: String = "houseConsignment"

  val houseConsignmentViewModel: HouseConsignmentViewModel = new HouseConsignmentViewModel(section)

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[HouseConsignmentView].apply(mrn, arrivalId, houseConsignmentViewModel, index)(fakeRequest, messages)

  behave like pageWithTitle(args = "1")

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(args = "1")

  behave like pageWithContent("p", "Compare the transit that arrived with the following information to identify any discrepancies.")

  behave like pageWithChildSections()

  behave like pageWithLinkAsButton("Back to summary", controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId).url)

  "must render section titles when rows are non-empty" - {
    section.children.foreach(_.sectionTitle.map {
      sectionTitle =>
        behave like pageWithContent("span", sectionTitle)
    })
  }

}
