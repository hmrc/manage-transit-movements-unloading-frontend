/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.mappings

import generators.Generators
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.{Form, FormError}

import java.time.LocalDate

class LocalDateFormatterSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators with OptionValues with Mappings {

  val form = Form(
    "value" -> localDate(
      requiredKey = "error.required",
      invalidKey = "error.invalid"
    )
  )

  val validData = datesBetween(
    min = LocalDate.of(2000, 1, 1),
    max = LocalDate.of(3000, 1, 1)
  )

  val invalidField: Gen[String] = nonEmptyString.retryUntil(_.toIntOption.isEmpty)

  "must bind valid data" in {

    forAll(validData -> "valid date") {
      date =>
        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.value.value mustEqual date
    }
  }

  "must bind valid data with spaces" in {

    forAll(validData -> "valid date") {
      date =>
        val data = Map(
          "value.day"   -> s"${date.getDayOfMonth.toString}   ",
          "value.month" -> s"${date.getMonthValue.toString}   ",
          "value.year"  -> s"${date.getYear.toString}   "
        )

        val result = form.bind(data)

        result.value.value mustEqual date
    }
  }

  "must fail to bind an empty date" in {

    val result = form.bind(Map.empty[String, String])

    result.errors must contain only FormError("value", "error.required.all", List("day", "month", "year"))
  }

  "must fail to bind a date with a missing day" in {

    forAll(validData -> "valid date") {
      date =>
        val data = Map(
          "value.day"   -> "",
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.required", List("day"))
    }
  }

  "must fail to bind a date with an invalid day" in {

    forAll(validData -> "valid date", invalidField -> "invalid field") {
      (date, field) =>
        val data = Map(
          "value.day"   -> field,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.invalid", List("day"))
    }
  }

  "must fail to bind a date with a missing month" in {

    forAll(validData -> "valid date") {
      date =>
        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> "",
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.required", List("month"))
    }
  }

  "must fail to bind a date with an invalid month" in {

    forAll(validData -> "valid data", invalidField -> "invalid field") {
      (date, field) =>
        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> field,
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.invalid", List("month"))
    }
  }

  "must fail to bind a date with a missing day and an invalid month" in {

    forAll(validData -> "valid data", invalidField -> "invalid field") {
      (date, field) =>
        val data = Map(
          "value.day"   -> "",
          "value.month" -> field,
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors mustBe Seq(
          FormError("value", "error.required", List("day")),
          FormError("value", "error.invalid", List("month"))
        )
    }
  }

  "must fail to bind a date with an invalid day and a missing month" in {

    forAll(validData -> "valid data", invalidField -> "invalid field") {
      (date, field) =>
        val data = Map(
          "value.day"   -> field,
          "value.month" -> "",
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors mustBe Seq(
          FormError("value", "error.invalid", List("day")),
          FormError("value", "error.required", List("month"))
        )
    }
  }

  "must fail to bind a date with a missing year" in {

    forAll(validData -> "valid date") {
      date =>
        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> ""
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.required", List("year"))
    }
  }

  "must fail to bind a date with an invalid year" in {

    forAll(validData -> "valid data", invalidField -> "invalid field") {
      (date, field) =>
        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> field
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.invalid", List("year"))
    }
  }

  "must fail to bind a date with a missing day and month" in {

    forAll(validData -> "valid date") {
      date =>
        val data = Map(
          "value.day"   -> "",
          "value.month" -> "",
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.required.multiple", List("day", "month"))
    }
  }

  "must fail to bind a date with a missing day and year" in {

    forAll(validData -> "valid date") {
      date =>
        val data = Map(
          "value.day"   -> "",
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> ""
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.required.multiple", List("day", "year"))
    }
  }

  "must fail to bind a date with a missing month and year" in {

    forAll(validData -> "valid date") {
      date =>
        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> "",
          "value.year"  -> ""
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.required.multiple", List("month", "year"))
    }
  }

  "must fail to bind an invalid day and month" in {

    forAll(validData -> "valid date", invalidField -> "invalid day", invalidField -> "invalid month") {
      (date, day, month) =>
        val data = Map(
          "value.day"   -> day,
          "value.month" -> month,
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.invalid.multiple", List("day", "month"))
    }
  }

  "must fail to bind an invalid day and year" in {

    forAll(validData -> "valid date", invalidField -> "invalid day", invalidField -> "invalid year") {
      (date, day, year) =>
        val data = Map(
          "value.day"   -> day,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> year
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.invalid.multiple", List("day", "year"))
    }
  }

  "must fail to bind an invalid month and year" in {

    forAll(validData -> "valid date", invalidField -> "invalid month", invalidField -> "invalid year") {
      (date, month, year) =>
        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> month,
          "value.year"  -> year
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.invalid.multiple", List("month", "year"))
    }
  }

  "must fail to bind an invalid day, month and year" in {

    forAll(invalidField -> "valid day", invalidField -> "invalid month", invalidField -> "invalid year") {
      (day, month, year) =>
        val data = Map(
          "value.day"   -> day,
          "value.month" -> month,
          "value.year"  -> year
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.invalid.all", List("day", "month", "year"))
    }
  }

  "must fail to bind an invalid date" in {

    val data = Map(
      "value.day"   -> "30",
      "value.month" -> "2",
      "value.year"  -> "2018"
    )

    val result = form.bind(data)

    result.errors must contain(
      FormError("value", "error.invalid.all", List("day", "month", "year"))
    )
  }

  "must unbind a date" in {

    forAll(validData -> "valid date") {
      date =>
        val filledForm = form.fill(date)

        filledForm("value.day").value.value mustEqual date.getDayOfMonth.toString
        filledForm("value.month").value.value mustEqual date.getMonthValue.toString
        filledForm("value.year").value.value mustEqual date.getYear.toString
    }
  }
}
