package forms

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import models.AddressLine._
import org.scalacheck.Gen
import play.api.data.FormError

class $formProvider$Spec extends StringFieldBehaviours with SpecBase {

  private val prefix = Gen.alphaNumStr.sample.value
  private val arg1   = Gen.alphaNumStr.sample.value
  private val arg2   = Gen.alphaNumStr.sample.value

  private val requiredKey = s"\$prefix.error.required"
  private val lengthKey   = s"\$prefix.error.length"
  private val invalidKey  = s"\$prefix.error.invalid"

  "when postal code is required" - {

    val form = DynamicAddressFormProvider(prefix, isPostalCodeRequired = true, arg1, arg2)

    ".numberAndStreet" - {

      val fieldName = NumberAndStreet.field

      behave like fieldThatBindsValidData(
        form = form,
        fieldName = fieldName,
        validDataGenerator = stringsWithMaxLength(NumberAndStreet.length)
      )

      behave like fieldWithMaxLength(
        form = form,
        fieldName = fieldName,
        maxLength = NumberAndStreet.length,
        lengthError = FormError(fieldName, lengthKey, Seq(NumberAndStreet.arg, arg1, arg2, NumberAndStreet.length))
      )

      behave like mandatoryTrimmedField(
        form = form,
        fieldName = fieldName,
        requiredError = FormError(fieldName, requiredKey, Seq(NumberAndStreet.arg, arg1, arg2))
      )

      behave like fieldWithInvalidCharacters(
        form = form,
        fieldName = fieldName,
        error = FormError(fieldName, invalidKey, Seq(NumberAndStreet.arg, arg1, arg2)),
        length = NumberAndStreet.length
      )
    }

    ".city" - {

      val fieldName = City.field

      behave like fieldThatBindsValidData(
        form = form,
        fieldName = fieldName,
        validDataGenerator = stringsWithMaxLength(City.length)
      )

      behave like fieldWithMaxLength(
        form = form,
        fieldName = fieldName,
        maxLength = City.length,
        lengthError = FormError(fieldName, lengthKey, Seq(City.arg, arg1, arg2, City.length))
      )

      behave like mandatoryTrimmedField(
        form = form,
        fieldName = fieldName,
        requiredError = FormError(fieldName, requiredKey, Seq(City.arg, arg1, arg2))
      )

      behave like fieldWithInvalidCharacters(
        form = form,
        fieldName = fieldName,
        error = FormError(fieldName, invalidKey, Seq(City.arg, arg1, arg2)),
        length = City.length
      )
    }

    ".postalCode" - {

      val invalidKey = s"\$prefix.error.postalCode.invalid"

      val fieldName = PostalCode.field

      val invalidPostalOverLength = stringsLongerThan(PostalCode.length + 1)

      behave like fieldThatBindsValidData(
        form = form,
        fieldName = fieldName,
        validDataGenerator = stringsWithMaxLength(PostalCode.length)
      )

      behave like fieldWithMaxLength(
        form = form,
        fieldName = fieldName,
        maxLength = PostalCode.length,
        lengthError = FormError(fieldName, lengthKey, Seq(PostalCode.arg, arg1, arg2, PostalCode.length)),
        gen = invalidPostalOverLength
      )

      behave like mandatoryTrimmedField(
        form = form,
        fieldName = fieldName,
        requiredError = FormError(fieldName, requiredKey, Seq(PostalCode.arg, arg1, arg2))
      )

      behave like fieldWithInvalidCharacters(
        form = form,
        fieldName = fieldName,
        error = FormError(fieldName, invalidKey, Seq(arg1, arg2)),
        length = PostalCode.length
      )
    }
  }

  "when postal code is not required" - {

    val form = DynamicAddressFormProvider(prefix, isPostalCodeRequired = false, arg1, arg2)

    ".numberAndStreet" - {

      val fieldName = NumberAndStreet.field

      behave like fieldThatBindsValidData(
        form = form,
        fieldName = fieldName,
        validDataGenerator = stringsWithMaxLength(NumberAndStreet.length)
      )

      behave like fieldWithMaxLength(
        form = form,
        fieldName = fieldName,
        maxLength = NumberAndStreet.length,
        lengthError = FormError(fieldName, lengthKey, Seq(NumberAndStreet.arg, arg1, arg2, NumberAndStreet.length))
      )

      behave like mandatoryTrimmedField(
        form = form,
        fieldName = fieldName,
        requiredError = FormError(fieldName, requiredKey, Seq(NumberAndStreet.arg, arg1, arg2))
      )

      behave like fieldWithInvalidCharacters(
        form = form,
        fieldName = fieldName,
        error = FormError(fieldName, invalidKey, Seq(NumberAndStreet.arg, arg1, arg2)),
        length = NumberAndStreet.length
      )
    }

    ".city" - {

      val fieldName = City.field

      behave like fieldThatBindsValidData(
        form = form,
        fieldName = fieldName,
        validDataGenerator = stringsWithMaxLength(City.length)
      )

      behave like fieldWithMaxLength(
        form = form,
        fieldName = fieldName,
        maxLength = City.length,
        lengthError = FormError(fieldName, lengthKey, Seq(City.arg, arg1, arg2, City.length))
      )

      behave like mandatoryTrimmedField(
        form = form,
        fieldName = fieldName,
        requiredError = FormError(fieldName, requiredKey, Seq(City.arg, arg1, arg2))
      )

      behave like fieldWithInvalidCharacters(
        form = form,
        fieldName = fieldName,
        error = FormError(fieldName, invalidKey, Seq(City.arg, arg1, arg2)),
        length = City.length
      )
    }

    ".postalCode" - {

      val invalidKey = s"\$prefix.error.postalCode.invalid"

      val fieldName = PostalCode.field

      val invalidPostalOverLength = stringsLongerThan(PostalCode.length + 1)

      behave like fieldThatBindsValidData(
        form = form,
        fieldName = fieldName,
        validDataGenerator = stringsWithMaxLength(PostalCode.length)
      )

      behave like fieldWithMaxLength(
        form = form,
        fieldName = fieldName,
        maxLength = PostalCode.length,
        lengthError = FormError(fieldName, lengthKey, Seq(PostalCode.arg, arg1, arg2, PostalCode.length)),
        gen = invalidPostalOverLength
      )

      behave like optionalField(
        form = form,
        fieldName = fieldName
      )

      behave like fieldWithInvalidCharacters(
        form = form,
        fieldName = fieldName,
        error = FormError(fieldName, invalidKey, Seq(arg1, arg2)),
        length = PostalCode.length
      )
    }
  }
}
