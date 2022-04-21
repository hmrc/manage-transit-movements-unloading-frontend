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

import play.api.data.FormError

import java.time.LocalDate

trait DateInputViewBehaviour extends QuestionViewBehaviours[LocalDate] {

  // scalastyle:off method.length
  def pageWithDateInput(): Unit =
    "page with date input" - {
      "when rendered" - {

        "must display day" in {
          assertRenderedById(doc, "value_day")
        }

        "must display month" in {
          assertRenderedById(doc, "value_month")
        }

        "must display year" in {
          assertRenderedById(doc, "value_year")
        }
      }

      "when rendered with error" - {
        "must show an error summary" in {
          assertRenderedById(docWithError(), "error-summary-title")
        }

        "must show an error in the value field's label" in {
          val errorSpan = docWithError().getElementsByClass("govuk-error-message").first
          assertElementContainsText(errorSpan, s"${messages("error.title.prefix")} ${messages(errorMessage)}")
        }

        "must show an error class on the inputs" in {
          val docWithError = parseView(applyView(form.withError(FormError("value", errorMessage, Seq("day", "month", "year")))))

          val dayInput = docWithError.getElementById("value_day")
          assert(dayInput.hasClass("govuk-input--error"))

          val monthInput = docWithError.getElementById("value_month")
          assert(monthInput.hasClass("govuk-input--error"))

          val yearInput = docWithError.getElementById("value_year")
          assert(yearInput.hasClass("govuk-input--error"))
        }

        "must have correct href on error link" - {
          "when no args" in {
            val docWithError = parseView(applyView(form.withError(FormError("value", errorMessage))))
            val link         = docWithError.select(".govuk-error-summary__list > li > a").first()
            assertElementContainsHref(link, "#value_day")
          }

          "when error in day input" in {
            val docWithError = parseView(applyView(form.withError(FormError("value", errorMessage, Seq("day")))))
            val link         = docWithError.select(".govuk-error-summary__list > li > a").first()
            assertElementContainsHref(link, "#value_day")
          }

          "when error in month input" in {
            val docWithError = parseView(applyView(form.withError(FormError("value", errorMessage, Seq("month")))))
            val link         = docWithError.select(".govuk-error-summary__list > li > a").first()
            assertElementContainsHref(link, "#value_month")
          }

          "when error in year input" in {
            val docWithError = parseView(applyView(form.withError(FormError("value", errorMessage, Seq("year")))))
            val link         = docWithError.select(".govuk-error-summary__list > li > a").first()
            assertElementContainsHref(link, "#value_year")
          }

          "when error in day and month inputs" in {
            val docWithError = parseView(applyView(form.withError(FormError("value", errorMessage, Seq("day", "month")))))
            val link         = docWithError.select(".govuk-error-summary__list > li > a").first()
            assertElementContainsHref(link, "#value_day")
          }

          "when error in day and year inputs" in {
            val docWithError = parseView(applyView(form.withError(FormError("value", errorMessage, Seq("day", "year")))))
            val link         = docWithError.select(".govuk-error-summary__list > li > a").first()
            assertElementContainsHref(link, "#value_day")
          }

          "when error in month and year inputs" in {
            val docWithError = parseView(applyView(form.withError(FormError("value", errorMessage, Seq("month", "year")))))
            val link         = docWithError.select(".govuk-error-summary__list > li > a").first()
            assertElementContainsHref(link, "#value_month")
          }

          "when error in day, month and year inputs" in {
            val docWithError = parseView(applyView(form.withError(FormError("value", errorMessage, Seq("day", "month", "year")))))
            val link         = docWithError.select(".govuk-error-summary__list > li > a").first()
            assertElementContainsHref(link, "#value_day")
          }
        }
      }
    }
  // scalastyle:on method.length
}
