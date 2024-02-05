/*
 * Copyright 2024 HM Revenue & Customs
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

package forms

import forms.mappings.Mappings
import models.reference.Selectable
import models.{CheckMode, Index, Mode, NormalMode, SelectableList}
import play.api.data.Form
import play.api.i18n.Messages

import javax.inject.Inject

class SelectableFormProvider @Inject() extends Mappings {

  def apply[T <: Selectable](mode: Mode, prefix: String, selectableList: SelectableList[T], args: Any*)(implicit
    messages: Messages
  ): Form[T] = {

    val prefixMode = mode match {
      case NormalMode => prefix
      case CheckMode  => s"$prefix.check"

    }

    Form(
      "value" -> selectable[T](selectableList, messages(s"$prefixMode.error.required", args: _*), args)
    )
  }
}
