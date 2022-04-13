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

package views.behaviours

import java.time.LocalDate

trait DateInputViewBehaviour extends QuestionViewBehaviours[LocalDate] {

  def pageWithDateInput() =
    "page with date input" - {
      "when rendered" - {

        "must display day" in {
          assertRenderedById(doc, "value.day")
        }

        "must display month" in {
          assertRenderedById(doc, "value.month")
        }

        "must display year" in {
          assertRenderedById(doc, "value.year")
        }
      }

      "when rendered with error" - {

        "when rendered with an error" - {
          "must show an error summary" in {
            assertRenderedById(docWithError(), "error-summary-title")
          }

          "must show an error in the value field's label" in {
            val errorSpan = docWithError().getElementsByClass("govuk-error-message").first
            assertElementContainsText(errorSpan, s"${messages("error.title.prefix")} ${messages(errorMessage)}")
          }
        }
      }
    }
}
