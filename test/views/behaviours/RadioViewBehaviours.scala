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
import uk.gov.hmrc.govukfrontend.views.html.components._

trait RadioViewBehaviours[T] extends QuestionViewBehaviours[T] {

  val getValue: T => String
  val fieldId: String = "value"

  def radioItems(fieldId: String, checkedValue: Option[T] = None): Seq[RadioItem]
  def values: Seq[T]

  // scalastyle:off method.length
  def pageWithRadioItems(
    legendIsHeading: Boolean = true,
    hintTextPrefix: Option[String] = None,
    args: Seq[Any] = Nil,
    legendIsVisible: Boolean = true
  ): Unit =
    "page with a radio question" - {
      "when rendered" - {

        "must contain a legend for the question" in {
          val legends = getElementsByTag(doc, "legend")
          legends.size mustBe 1
          if (legendIsHeading) {
            assertElementIncludesText(legends.first(), messages(s"$prefix.heading", args: _*))
          } else {
            assertElementIncludesText(legends.first(), messages(s"$prefix.label", args: _*))
            assert(legends.first().hasClass("govuk-visually-hidden") != legendIsVisible)
          }

          hintTextPrefix.map {
            prefix =>
              val hint = getElementByClass(doc, "govuk-hint")
              assertElementIncludesText(hint, messages(s"$prefix.hint"))
          }
        }

        radioItems(fieldId).zipWithIndex.foreach {
          case (radioItem, index) =>
            s"must contain an input for the value ${radioItem.value.get}" in {
              assertRenderedById(doc, radioItem.id.get)
            }

            radioItem.hint.foreach {
              hint =>
                s"must contain a hint for the value ${radioItem.value.get}" in {
                  val element = {
                    val id = if (index == 0) s"$fieldId-item-hint" else s"${fieldId}_$index-item-hint"
                    getElementById(doc, id)
                  }
                  assertElementContainsText(element, hint.content.asHtml.toString())
                }
            }

            s"must not have ${radioItem.value.get} checked when rendered with no form" in {
              assert(!doc.getElementById(radioItem.id.get).hasAttr("checked"))
            }
        }

        behave like pageWithoutErrorSummary()
      }

      values foreach {
        value =>
          s"when rendered with a value of $value" - {
            behave like answeredRadioPage(value)
          }
      }

      behave like pageWithErrorSummary()
    }

  // scalastyle:on method.length

  private def answeredRadioPage(answer: T): Unit = {

    val filledForm = form.fill(answer)
    val doc        = parseView(applyView(filledForm))

    radioItems(fieldId, Some(answer)) foreach {
      radioItem =>
        if (radioItem.value.get == getValue(answer)) {
          s"must have ${radioItem.value.get} checked" in {
            assert(doc.getElementById(radioItem.id.get).hasAttr("checked"))
          }
        } else {
          s"must have ${radioItem.value.get} unchecked" in {
            assert(!doc.getElementById(radioItem.id.get).hasAttr("checked"))
          }
        }
    }

    behave like pageWithoutErrorSummary()
  }

  def pageWithoutRadioItems(doc: Document): Unit =
    "page without radio items" - {
      "must not render radio inputs" in {
        assertElementDoesNotExist(doc, "govuk-radios__input")
      }

      "must not render a hint" in {
        assertElementDoesNotExist(doc, "govuk-hint")
      }
    }
}
