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

package views.behaviours

import generators.Generators
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewModels.sections.Section
import viewModels.sections.Section.StaticSection

trait CheckYourAnswersViewBehaviours extends SummaryListViewBehaviours with Generators {

  // need to use arbitraryStaticSectionNoChildren as the summaryLists val does not take children sections into account
  lazy val sections: Seq[Section] = listWithMaxLength[StaticSection]()(arbitraryStaticSectionNoChildren).sample.value

  override def view: HtmlFormat.Appendable = viewWithSections(sections)

  def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable

  override lazy val summaryLists: Seq[SummaryList] = sections.map(
    section => SummaryList(section.rows)
  )

  def pageWithCheckYourAnswers(): Unit = {

    behave like pageWithSummaryLists()

    "page with check your answers" - {

      "must render section titles when rows are non-empty" - {
        sections.foreach(_.sectionTitle.map {
          sectionTitle =>
            behave like pageWithContent("h2", sectionTitle)
        })
      }

      "must not render section titles when rows and children are empty" - {
        val emptySections = sections.map {
          case x: Section.AccordionSection => x.copy(rows = Nil, children = Nil)
          case x: Section.StaticSection    => x.copy(rows = Nil, children = Nil)
        }
        val view = viewWithSections(emptySections)
        val doc  = parseView(view)
        emptySections.foreach(_.sectionTitle.map {
          sectionTitle =>
            behave like pageWithoutContent(doc, "h2", sectionTitle)
        })
      }
    }
  }
}
