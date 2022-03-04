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

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.viewmodels.SummaryList.{Key, Row, Value}
import viewModels.sections.Section

import scala.collection.convert.ImplicitConversions._

class RejectionCheckYourAnswersViewSpec extends SingleViewSpec("rejection-check-your-answers.njk", hasSignOutLink = true) {

  private val fakeSectionList: Seq[Section] = Seq(
    Section(rows = Seq(Row(Key(lit""), Value(lit""))))
  )

  private val json: JsObject =
    Json.obj(
      "sections" -> Json.toJson(fakeSectionList)
    )

  "must pass correct class list to summary list macro" in {

    val doc      = renderDocument(json).futureValue
    val sections = doc.getElementsByClass("govuk-summary-sectionRows__row")

    sections.size() mustEqual fakeSectionList.size

    sections.forEach {
      section =>
        val summaryList = section.getElementsByClass("govuk-summary-list").get(0)

        summaryList.classNames().toList mustEqual List(
          "govuk-summary-list",
          "govuk-!-margin-bottom-9",
          "ctc-add-to-a-list"
        )
    }

  }
}
