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

package forms

import forms.mappings.Mappings
import models.{Enumerable, Radioable}
import play.api.data.Form

import javax.inject.Inject

class EnumerableFormProvider @Inject() extends Mappings {

  def apply[T <: Radioable[T]](prefix: String, args: Any*)(implicit et: Enumerable[T]): Form[T] = {
    val flattenedArgs = args.flatMap {
      case seq: Seq[_] => seq
      case other       => Seq(other)
    }

    Form(
      "value" -> enumerable[T](s"$prefix.error.required", args = flattenedArgs)
    )
  }

  def apply[T <: Radioable[T]](prefix: String, values: Seq[T], args: Any*)(implicit et: Seq[T] => Enumerable[T]): Form[T] =
    apply(prefix, args)(et(values))
}
