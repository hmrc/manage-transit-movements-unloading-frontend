package forms

import forms.behaviours.IntFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class $formProvider$Spec extends IntFieldBehaviours {

  private val prefix      = Gen.alphaNumStr.sample.value
  val requiredKey = s"\$prefix.error.required"
  val maximum = 10

  val generatedInt = Gen.oneOf(1 to maximum)

  val form = new $formProvider$()(prefix, maximum)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      generatedInt.toString
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, s"\$prefix.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, s"\$prefix.error.wholeNumber")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
