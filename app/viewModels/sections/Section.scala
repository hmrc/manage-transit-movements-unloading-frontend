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

package viewModels.sections

import play.api.libs.functional.syntax._
import play.api.libs.json.{__, OWrites}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

case class Section(sectionTitle: Option[String], rows: Seq[SummaryListRow])

object Section {
  def apply(sectionTitle: String, rows: Seq[SummaryListRow]): Section = new Section(Some(sectionTitle), rows)

  def apply(rows: Seq[SummaryListRow]): Section = new Section(None, rows)

  def apply(row: SummaryListRow): Section = new Section(None, Seq(row))

  implicit val sectionWrites: OWrites[Section] =
    (
      (__ \ "sectionTitle").write[Option[String]] and
        (__ \ "rows").write[Seq[SummaryListRow]]
    )(unlift(Section.unapply))

}
