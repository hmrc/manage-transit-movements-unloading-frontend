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

import controllers.routes
import generators.{Generators, ViewModelGenerators}
import models.{Index, NormalMode}
import org.jsoup.nodes.Document
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewModels.sections.Section
import views.behaviours.SummaryListViewBehaviours
import views.html.UnloadingSummaryView

class UnloadingSummaryViewSpec extends SummaryListViewBehaviours with Generators with ViewModelGenerators {

  override val prefix: String = "unloadingSummary"

  private val sealSections: Seq[Section]             = listWithMaxLength[Section]().sample.value
  private val transportAndItemSections: Seq[Section] = listWithMaxLength[Section]().sample.value
  private val sections: Seq[Section]                 = sealSections ++ transportAndItemSections

  private val numberOfSeals: Int           = arbitrary[Int].sample.value
  private val showAddCommentsLink: Boolean = arbitrary[Boolean].sample.value

  override def summaryLists: Seq[SummaryList] = sections.map(
    section => new SummaryList(section.rows)
  )

  override def view: HtmlFormat.Appendable =
    injector
      .instanceOf[UnloadingSummaryView]
      .apply(mrn, arrivalId, sealSections, transportAndItemSections, numberOfSeals, showAddCommentsLink)(fakeRequest, messages)

  behave like pageWithBackLink

  behave like pageWithCaption(mrn.toString)

  behave like pageWithHeading()

  sections.foreach(_.sectionTitle.map {
    sectionTitle =>
      behave like pageWithContent("h2", sectionTitle)
  })

  behave like pageWithSummaryLists()

  behave like pageWithLink(
    id = "add-seal",
    expectedText = "Add a new official customs seal number",
    expectedHref = routes.NewSealNumberController.onPageLoad(arrivalId, Index(numberOfSeals), NormalMode).url
  )

  behave like pageWithSubmitButton("Continue")

  "when there are no seal sections" - {
    val view =
      injector
        .instanceOf[UnloadingSummaryView]
        .apply(mrn, arrivalId, Nil, transportAndItemSections, numberOfSeals, showAddCommentsLink)(fakeRequest, messages)

    val doc: Document = parseView(view)

    behave like pageWithContent(doc, "h2", "Official customs seals")

    behave like pageWithContent(doc, "p", "There are no official customs seals.")
  }

  "when showing the add comment link" - {
    val view =
      injector
        .instanceOf[UnloadingSummaryView]
        .apply(mrn, arrivalId, sealSections, transportAndItemSections, numberOfSeals, showAddCommentLink = true)(fakeRequest, messages)

    val doc: Document = parseView(view)

    behave like pageWithLink(
      doc = doc,
      id = "add-comment",
      expectedText = "Add comment",
      expectedHref = routes.ChangesToReportController.onPageLoad(arrivalId, NormalMode).url
    )
  }

}
