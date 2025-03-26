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

import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.data.FormError

import java.time.LocalDate

trait DateInputViewBehaviour extends QuestionViewBehaviours[LocalDate] {

  private lazy val dayRequiredErrorMessage      = s"$prefix.error.required.day"
  private lazy val monthRequiredErrorMessage    = s"$prefix.error.required.month"
  private lazy val yearRequiredErrorMessage     = s"$prefix.error.required.year"
  private lazy val multipleRequiredErrorMessage = s"$prefix.error.required.multiple"
  private lazy val allRequiredErrorMessage      = s"$prefix.error.required.all"

  private lazy val dayInvalidErrorMessage      = s"$prefix.error.invalid.day"
  private lazy val monthInvalidErrorMessage    = s"$prefix.error.invalid.month"
  private lazy val yearInvalidErrorMessage     = s"$prefix.error.invalid.year"
  private lazy val multipleInvalidErrorMessage = s"$prefix.error.invalid.multiple"
  private lazy val allInvalidErrorMessage      = s"$prefix.error.invalid.all"

  private def getLinks(doc: Document): Elements =
    doc.select(".govuk-error-summary__list > li > a")

  // scalastyle:off method.length
  def pageWithDateInput(): Unit =
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
        behave like pageWithErrorSummary()

        "must show an error class on the inputs" in {
          val formWithErrors = form.withError(FormError("value", errorMessage, Seq("day", "month", "year")))
          val docWithErrors  = parseView(applyView(formWithErrors))

          val dayInput = docWithErrors.getElementById("value.day")
          assert(dayInput.hasClass("govuk-input--error"))

          val monthInput = docWithErrors.getElementById("value.month")
          assert(monthInput.hasClass("govuk-input--error"))

          val yearInput = docWithErrors.getElementById("value.year")
          assert(yearInput.hasClass("govuk-input--error"))
        }

        "must have correct href on error links" - {
          "when required error in day input" in {
            val formWithErrors = form.withError(FormError("value.day", dayRequiredErrorMessage, Seq("day")))
            val docWithErrors  = parseView(applyView(formWithErrors))
            val links          = getLinks(docWithErrors)

            links.size() mustEqual 1
            assertElementContainsHref(links.get(0), "#value.day")
            assertElementIncludesText(links.get(0), " must include a day")
          }

          "when invalid error in day input" in {
            val formWithErrors = form.withError(FormError("value.day", dayInvalidErrorMessage, Seq(31, "day")))
            val docWithErrors  = parseView(applyView(formWithErrors))
            val links          = getLinks(docWithErrors)

            links.size() mustEqual 1
            assertElementContainsHref(links.get(0), "#value.day")
            assertElementIncludesText(links.get(0), " must only include numbers 1 to 31")
          }

          "when required error in month input" in {
            val formWithErrors = form.withError(FormError("value.month", monthRequiredErrorMessage, Seq("month")))
            val docWithErrors  = parseView(applyView(formWithErrors))
            val links          = getLinks(docWithErrors)

            links.size() mustEqual 1
            assertElementContainsHref(links.get(0), "#value.month")
            assertElementIncludesText(links.get(0), " must include a month")
          }

          "when invalid error in month input" in {
            val formWithErrors = form.withError(FormError("value.month", monthInvalidErrorMessage, Seq("month")))
            val docWithErrors  = parseView(applyView(formWithErrors))
            val links          = getLinks(docWithErrors)

            links.size() mustEqual 1
            assertElementContainsHref(links.get(0), "#value.month")
            assertElementIncludesText(links.get(0), " must only include numbers 1 to 12")
          }

          "when required error in year input" in {
            val formWithErrors = form.withError(FormError("value.year", yearRequiredErrorMessage, Seq("year")))
            val docWithErrors  = parseView(applyView(formWithErrors))
            val links          = getLinks(docWithErrors)

            links.size() mustEqual 1
            assertElementContainsHref(links.get(0), "#value.year")
            assertElementIncludesText(links.get(0), " must include a year")
          }

          "when invalid error in year input" in {
            val formWithErrors = form.withError(FormError("value.year", yearInvalidErrorMessage, Seq("year")))
            val docWithErrors  = parseView(applyView(formWithErrors))
            val links          = getLinks(docWithErrors)

            links.size() mustEqual 1
            assertElementContainsHref(links.get(0), "#value.year")
            assertElementIncludesText(links.get(0), " must be a real year")
          }

          "when required errors in day and month inputs" in {
            val formWithErrors = form
              .withError(FormError("value.day", multipleRequiredErrorMessage, Seq("day", "month")))
              .withError(FormError("value.year", multipleRequiredErrorMessage, Seq("day", "month")))

            val docWithErrors = parseView(applyView(formWithErrors))
            val links         = getLinks(docWithErrors)

            links.size() mustEqual 1
            assertElementContainsHref(links.get(0), "#value.day")
            assertElementIncludesText(links.get(0), " must include a day and month")
          }

          "when different errors in day and month inputs" in {
            val formWithErrors = form
              .withError(FormError("value.day", dayRequiredErrorMessage, Seq("day", "month")))
              .withError(FormError("value.month", monthInvalidErrorMessage, Seq("day", "month")))

            val docWithErrors = parseView(applyView(formWithErrors))
            val links         = getLinks(docWithErrors)

            links.size() mustEqual 2
            assertElementContainsHref(links.get(0), "#value.day")
            assertElementIncludesText(links.get(0), " must include a day")
            assertElementContainsHref(links.get(1), "#value.month")
            assertElementIncludesText(links.get(1), " must only include numbers 1 to 12")
          }

          "when required errors in day and year inputs" in {
            val formWithErrors = form
              .withError(FormError("value.day", multipleRequiredErrorMessage, Seq("day", "year")))
              .withError(FormError("value.year", multipleRequiredErrorMessage, Seq("day", "year")))

            val docWithErrors = parseView(applyView(formWithErrors))
            val links         = getLinks(docWithErrors)

            links.size() mustEqual 1
            assertElementContainsHref(links.get(0), "#value.day")
            assertElementIncludesText(links.get(0), " must include a day and year")
          }

          "when different errors in day and year inputs" in {
            val formWithErrors = form
              .withError(FormError("value.day", dayRequiredErrorMessage, Seq("day", "year")))
              .withError(FormError("value.year", yearInvalidErrorMessage, Seq("day", "year")))

            val docWithErrors = parseView(applyView(formWithErrors))
            val links         = getLinks(docWithErrors)

            links.size() mustEqual 2
            assertElementContainsHref(links.get(0), "#value.day")
            assertElementIncludesText(links.get(0), " must include a day")
            assertElementContainsHref(links.get(1), "#value.year")
            assertElementIncludesText(links.get(1), " must be a real year")
          }

          "when required errors in month and year inputs" in {
            val formWithErrors = form
              .withError(FormError("value.month", multipleRequiredErrorMessage, Seq("month", "year")))
              .withError(FormError("value.year", multipleRequiredErrorMessage, Seq("month", "year")))

            val docWithErrors = parseView(applyView(formWithErrors))
            val links         = getLinks(docWithErrors)

            links.size() mustEqual 1
            assertElementContainsHref(links.get(0), "#value.month")
            assertElementIncludesText(links.get(0), " must include a month and year")
          }

          "when different errors in month and year inputs" in {
            val formWithErrors = form
              .withError(FormError("value.month", monthRequiredErrorMessage, Seq("month", "year")))
              .withError(FormError("value.year", yearInvalidErrorMessage, Seq("month", "year")))

            val docWithErrors = parseView(applyView(formWithErrors))
            val links         = getLinks(docWithErrors)

            links.size() mustEqual 2
            assertElementContainsHref(links.get(0), "#value.month")
            assertElementIncludesText(links.get(0), " must include a month")
            assertElementContainsHref(links.get(1), "#value.year")
            assertElementIncludesText(links.get(1), " must be a real year")
          }

          "when required errors in day, month and year inputs" in {
            val formWithErrors = form
              .withError(FormError("value.day", allRequiredErrorMessage, Seq("day", "month", "year")))
              .withError(FormError("value.month", allRequiredErrorMessage, Seq("day", "month", "year")))
              .withError(FormError("value.year", allRequiredErrorMessage, Seq("day", "month", "year")))

            val docWithErrors = parseView(applyView(formWithErrors))
            val links         = getLinks(docWithErrors)

            links.size() mustEqual 1
            assertElementContainsHref(links.get(0), "#value.day")
            assertElementIncludesText(links.get(0), "Enter the ")
          }

          "when invalid errors in day, month and year inputs" in {
            val formWithErrors = form
              .withError(FormError("value.day", allInvalidErrorMessage, Seq("day", "month", "year")))
              .withError(FormError("value.month", allInvalidErrorMessage, Seq("day", "month", "year")))
              .withError(FormError("value.year", allInvalidErrorMessage, Seq("day", "month", "year")))

            val docWithErrors = parseView(applyView(formWithErrors))
            val links         = getLinks(docWithErrors)

            links.size() mustEqual 1
            assertElementContainsHref(links.get(0), "#value.day")
            assertElementIncludesText(links.get(0), " must be a real date")
          }

          "when different error in day, month and year inputs" in {
            val formWithErrors = form
              .withError(FormError("value.day", multipleInvalidErrorMessage, Seq("day", "month", "year")))
              .withError(FormError("value.month", monthRequiredErrorMessage, Seq("day", "month", "year")))
              .withError(FormError("value.year", multipleInvalidErrorMessage, Seq("day", "month", "year")))

            val docWithErrors = parseView(applyView(formWithErrors))
            val links         = getLinks(docWithErrors)

            links.size() mustEqual 2
            assertElementContainsHref(links.get(0), "#value.day")
            assertElementIncludesText(links.get(0), " must be a real date")
            assertElementContainsHref(links.get(1), "#value.month")
            assertElementIncludesText(links.get(1), " must include a month")
          }
        }
      }
    }
  // scalastyle:on method.length
}
