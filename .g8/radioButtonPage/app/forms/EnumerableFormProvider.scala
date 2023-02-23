package forms

import forms.mappings.Mappings
import models.Enumerable
import play.api.data.Form

class EnumerableFormProvider @Inject() extends Mappings {

  def apply[T](prefix: String)(implicit et: Enumerable[T]): Form[T] =
    Form(
      "value" -> enumerable[T](s"$prefix.error.required")
    )
}
