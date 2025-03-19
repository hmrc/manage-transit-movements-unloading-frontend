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
import models.{Mode, SelectableList}
import play.api.data.Form
import play.api.i18n.Messages

trait SelectableFormProvider extends Mappings {

  val field: String

  def apply[T <: Selectable](mode: Mode, prefix: String, selectableList: SelectableList[T], args: Any*)(implicit
    messages: Messages
  ): Form[T] =
    Form(
      field -> selectable[T](selectableList, messages(s"$prefix.$mode.error.required", args*), args)
    )
}

object SelectableFormProvider {

  class CountryFormProvider extends SelectableFormProvider {
    override val field: String = CountryFormProvider.field
  }

  object CountryFormProvider {
    val field: String = "country"
  }

  class GoodsReferenceTypeFormProvider extends SelectableFormProvider {
    override val field: String = GoodsReferenceTypeFormProvider.field
  }

  object GoodsReferenceTypeFormProvider {
    val field: String = "goodsReferenceType"
  }

  class DocumentTypeFormProvider extends SelectableFormProvider {
    override val field: String = DocumentTypeFormProvider.field
  }

  object DocumentTypeFormProvider {
    val field: String = "documentType"
  }

  class AdditionalReferenceTypeFormProvider extends SelectableFormProvider {
    override val field: String = AdditionalReferenceTypeFormProvider.field
  }

  object AdditionalReferenceTypeFormProvider {
    val field: String = "additionalReferenceType"
  }

  class PackageTypeFormProvider extends SelectableFormProvider {
    override val field: String = PackageTypeFormProvider.field
  }

  object PackageTypeFormProvider {
    val field: String = "packageType"
  }
}
