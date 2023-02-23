package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class $formProvider$ @Inject() extends Mappings {

  def apply(prefix: String, maximum: Int): Form[Int] =
    Form(
      "value" -> int(s"\$prefix.error.required",
        s"\$prefix.error.wholeNumber",
        s"\$prefix.error.nonNumeric")
        .verifying(maximumValue(maximum, s"\$prefix.error.maximum"))
    )
}
