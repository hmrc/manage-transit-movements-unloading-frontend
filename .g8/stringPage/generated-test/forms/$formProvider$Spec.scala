package forms

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class $formProvider$Spec extends StringFieldBehaviours {

  private val prefix      = Gen.alphaNumStr.sample.value
  val requiredKey = s"\$prefix.error.required"
  val lengthKey = s"\$prefix.error.length"
  val maxLength = $maxLength$

  val form = new $formProvider$()(prefix)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
