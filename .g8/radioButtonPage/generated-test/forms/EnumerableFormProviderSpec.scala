package forms

import forms.behaviours.OptionFieldBehaviours
import models.RadioModel
import org.scalacheck.Gen
import play.api.data.FormError

class EnumerableFormProviderSpec extends OptionFieldBehaviours {

  private val prefix: String = Gen.alphaNumStr.sample.value

  sealed private trait FakeEnum

  private object FakeEnum extends RadioModel[FakeEnum] {
    override val messageKeyPrefix: String = prefix

    case object Foo extends FakeEnum
    case object Bar extends FakeEnum

    override val values: Seq[FakeEnum] = Seq(Foo, Bar)
  }

  private val formProvider = new EnumerableFormProvider()
  private val form         = formProvider.apply[FakeEnum](prefix)

  ".value" - {

    val fieldName   = "value"
    val requiredKey = s"\$prefix.error.required"

    behave like optionsField[FakeEnum](
      form,
      fieldName,
      validValues = FakeEnum.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
