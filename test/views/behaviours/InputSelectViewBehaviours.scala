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
import models.reference.Selectable
import org.scalacheck.Arbitrary

trait InputSelectViewBehaviours[T <: Selectable] extends QuestionViewBehaviours[T] with Generators {

  implicit val arbitraryT: Arbitrary[T]
  lazy val values: Seq[T] = listWithMaxLength[T]().sample.value

  val field: String

  def pageWithSelect(): Unit =
    "behave like a page with a select element" - {

      "when rendered" - {
        "must contain an input for the value" in {
          assertRenderedById(doc, field)
        }

        val options = getElementsByTag(doc, "option")
        values.map(_.toSelectItem()).foreach {
          selectItem =>
            s"must contain a select item for ${selectItem.text}" in {
              assertElementExists(options, x => x.text() == selectItem.text && x.`val`() == selectItem.value.get)
            }
        }
      }

      "when rendered with a valid form" - {
        "must have the correct selection option value 'selected' for the form input value" - {
          values.foreach {
            value =>
              s"when $value selected" in {
                val filledForm = form.fill(value)
                val doc        = parseView(applyView(filledForm))
                doc.getElementsByAttribute("selected").attr("value") mustEqual value.toSelectItem().value.get
              }
          }
        }
      }

      behave like pageWithErrorSummary(field)
    }
}
