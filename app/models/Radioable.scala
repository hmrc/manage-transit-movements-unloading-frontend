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

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Hint, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

trait Radioable[T] {

  val messageKeyPrefix: String

  def toRadioItem(index: Int, formKey: String, checked: Boolean)(implicit messages: Messages): RadioItem = RadioItem(
    content = Text(messages(s"$messageKeyPrefix.$this")),
    id = Some(if (index == 0) formKey else s"${formKey}_$index"),
    value = Some(this.toString),
    checked = checked,
    hint = {
      val hintKey = s"$messageKeyPrefix.$this.hint"
      if (messages.isDefinedAt(hintKey)) Some(Hint(content = Text(messages(hintKey)))) else None
    }
  )
}

object Radioable {

  implicit class Radioables[T](radioables: Seq[Radioable[T]]) {

    def toRadioItems(formKey: String, checkedValue: Option[Radioable[T]])(implicit messages: Messages): Seq[RadioItem] =
      radioables.zipWithIndex
        .map {
          case (value, index) =>
            value.toRadioItem(index, formKey, checkedValue.contains(value))
        }
  }
}
