package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class YesNoFormProvider @Inject() extends Mappings {

  def apply(prefix: String): Form[Boolean] =
    Form(
      "value" -> boolean(s"$prefix.error.required")
  )
}