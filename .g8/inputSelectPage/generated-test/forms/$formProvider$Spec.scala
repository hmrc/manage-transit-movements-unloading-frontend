package forms

import forms.behaviours.StringFieldBehaviours
import models.$referenceListClass$
import play.api.data.FormError
import generators.Generators
import org.scalacheck.Gen

class $formProvider$Spec extends StringFieldBehaviours with Generators{

  private val prefix      = Gen.alphaNumStr.sample.value
  private val requiredKey = s"\$prefix.error.required"
  private val maxLength   = 8

  private val $referenceClass;format="decap"$1 = arbitrary$referenceClass$.arbitrary.sample.get
  private val $referenceClass;format="decap"$2 = arbitrary$referenceClass$.arbitrary.sample.get
  private val $referenceListClass;format="decap"$ = $referenceListClass$(Seq($referenceClass;format="decap"$1, $referenceClass;format="decap"$2))

  private val form = new $formProvider$()(prefix, $referenceListClass;format="decap"$)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "not bind if customs office id does not exist in the $referenceListClass;format="decap"$" in {
      val boundForm = form.bind(Map("value" -> "foobar"))
      val field     = boundForm("value")
      field.errors mustNot be(empty)
    }

    "bind a $referenceClass;format="decap"$ id which is in the list" in {
      val boundForm = form.bind(Map("value" -> $referenceClass;format="decap"$1.id))
      val field     = boundForm("value")
      field.errors must be(empty)
    }
  }
}
