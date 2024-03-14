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

package viewModels.sections

import models.Link
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

sealed trait Section {
  val sectionTitle: Option[String]
  val rows: Seq[SummaryListRow]
  val children: Seq[Section]
  val viewLinks: Seq[Link]
  val id: Option[String]
  val optionalInformationHeading: Option[String]
}

object Section {

  case class AccordionSection(
    sectionTitle: Option[String] = None,
    rows: Seq[SummaryListRow] = Nil,
    children: Seq[Section] = Nil,
    viewLinks: Seq[Link] = Nil,
    id: Option[String] = None,
    optionalInformationHeading: Option[String] = None
  ) extends Section

  object AccordionSection {

    def apply(sectionTitle: String, rows: Seq[SummaryListRow]): AccordionSection =
      new AccordionSection(sectionTitle = Some(sectionTitle), rows = rows)

    def apply(sectionTitle: String, rows: Seq[SummaryListRow], viewLinks: Seq[Link], id: String): AccordionSection =
      new AccordionSection(Some(sectionTitle), rows = rows, viewLinks = viewLinks, id = Some(id))

    def apply(sectionTitle: String, rows: Seq[SummaryListRow], children: Seq[Section], viewLinks: Seq[Link], id: String): AccordionSection =
      new AccordionSection(Some(sectionTitle), rows, children, viewLinks, id = Some(id))
  }

  case class StaticSection(
    sectionTitle: Option[String] = None,
    rows: Seq[SummaryListRow] = Nil,
    viewLinks: Seq[Link] = Nil,
    id: Option[String] = None,
    optionalInformationHeading: Option[String] = None
  ) extends Section {

    override val children: Seq[Section] = Seq.empty
  }

  object StaticSection {

    def apply(sectionTitle: String, rows: Seq[SummaryListRow]): StaticSection =
      new StaticSection(sectionTitle = Some(sectionTitle), rows = rows)

    def apply(sectionTitle: String, rows: Seq[SummaryListRow], optionalInformationHeading: String): StaticSection =
      new StaticSection(sectionTitle = Some(sectionTitle), rows = rows, optionalInformationHeading = Some(optionalInformationHeading))
  }
}
