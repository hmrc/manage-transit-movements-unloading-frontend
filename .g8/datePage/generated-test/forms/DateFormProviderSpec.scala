package forms

import forms.behaviours.DateBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

import java.time.LocalDate

class DateFormProviderSpec extends DateBehaviours {

  private val prefix = Gen.alphaNumStr.sample.value

  private val (minDate, mustBeAfter)  = (LocalDate.of(2021: Int, 1: Int, 1: Int), "31 December 2020")
  private val (maxDate, mustBeBefore) = (LocalDate.of(2021: Int, 12: Int, 31: Int), "1 January 2022")
  private val form                    = new DateFormProvider()(prefix, minDate, maxDate)

  ".value" - {

    val fieldName = "value"

    val validData = datesBetween(
      min = minDate,
      max = maxDate
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, fieldName, s"$prefix.error.required.all")

    behave like dateFieldWithMin(form, fieldName, min = minDate, FormError("value", s"$prefix.error.min.date", Seq(mustBeAfter)))

    behave like dateFieldWithMax(form, fieldName, max = maxDate, FormError("value", s"$prefix.error.max.date", Seq(mustBeBefore)))

  }
}
