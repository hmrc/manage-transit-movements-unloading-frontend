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
import viewModels.sections.SummarySection
import views.behaviours.SummaryListViewBehaviours
import views.html.UnloadingRemarksRejectionView

class UnloadingRemarksRejectionViewSpec extends SummaryListViewBehaviours with ViewModelGenerators with Generators {

  private val sections: Seq[SummarySection] = listWithMaxLength[SummarySection]().sample.value

  override def summaryLists: Seq[SummaryList] = sections.map(
    section => SummaryList(section.rows)
  )

  private val contactUrl = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/new-computerised-transit-system-enquiries"
  private val reviewUrl  = s"/manage-transit-movements/unloading/${arrivalId.toString}"

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[UnloadingRemarksRejectionView].apply(arrivalId, sections)(fakeRequest, messages)

  override val prefix: String = "unloadingRemarksRejection"

  behave like pageWithBackLink

  behave like pageWithHeading()

  sections.foreach(_.sectionTitle.map {
    sectionTitle =>
      behave like pageWithContent("h2", sectionTitle)
  })

  behave like pageWithSummaryLists()

  behave like pageWithPartialContent("p", "You must review the error and")
  behave like pageWithLink(
    "review",
    "send new unloading remarks with the right information",
    reviewUrl
  )

  behave like pageWithPartialContent("p", "You can ")
  behave like pageWithLink(
    "contact",
    "contact the New Computerised Transit System helpdesk if you need help understanding the error (opens in a new tab)",
    contactUrl
  )
}
