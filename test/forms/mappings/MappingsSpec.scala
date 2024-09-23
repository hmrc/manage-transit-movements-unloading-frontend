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

import models.{Enumerable, Radioable}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.{Form, FormError}

object MappingsSpec {

  sealed trait Foo extends Radioable[Foo]

  case object Bar extends Foo {
    override val code: String             = "1"
    override val messageKeyPrefix: String = "bar"
  }

  case object Baz extends Foo {
    override val code: String             = "2"
    override val messageKeyPrefix: String = "baz"
  }

  object Foo {

    val values: Set[Foo] = Set(Bar, Baz)

    implicit val fooEnumerable: Enumerable[Foo] =
      Enumerable(
        values.toSeq
          .map(
            v => v.toString -> v
          )*
      )
  }
}

class MappingsSpec extends AnyFreeSpec with Matchers with OptionValues with Mappings {

  import MappingsSpec._

  "text" - {

    val testForm: Form[String] =
      Form(
        "value" -> text()
      )

    "must bind a valid string" in {
      val result = testForm.bind(Map("value" -> "foobar"))
      result.get mustEqual "foobar"
    }

    "must bind a valid string with trailing whitespace" in {
      val result = testForm.bind(Map("value" -> "foobar   "))
      result.get mustEqual "foobar"
    }

    "must not bind an empty string" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must return a custom error message" in {
      val form   = Form("value" -> text("custom.error"))
      val result = form.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "custom.error"))
    }

    "must unbind a valid value" in {
      val result = testForm.fill("foobar")
      result.apply("value").value.value mustEqual "foobar"
    }
  }

  "boolean" - {

    val testForm: Form[Boolean] =
      Form(
        "value" -> boolean()
      )

    "must bind true" in {
      val result = testForm.bind(Map("value" -> "true"))
      result.get mustEqual true
    }

    "must bind false" in {
      val result = testForm.bind(Map("value" -> "false"))
      result.get mustEqual false
    }

    "must not bind a non-boolean" in {
      val result = testForm.bind(Map("value" -> "not a boolean"))
      result.errors must contain(FormError("value", "error.boolean"))
    }

    "must not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must unbind" in {
      val result = testForm.fill(true)
      result.apply("value").value.value mustEqual "true"
    }
  }

  "int" - {

    val testForm: Form[Int] =
      Form(
        "value" -> int()
      )

    "must bind a valid integer" in {
      val result = testForm.bind(Map("value" -> "1"))
      result.get mustEqual 1
    }

    "must bind a valid integer with comma separators" in {
      val result = testForm.bind(Map("value" -> "1,000"))
      result.get mustEqual 1000
    }

    "must bind a valid integer spaces" in {
      val result = testForm.bind(Map("value" -> "1 000 000"))
      result.get mustEqual 1000000
    }

    "must not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must unbind a valid value" in {
      val result = testForm.fill(123)
      result.apply("value").value.value mustEqual "123"
    }
  }

  "BigDecimal" - {

    val decimalPlaces          = 6
    val length                 = 16
    val isZeroAllowed: Boolean = arbitrary[Boolean].sample.value

    val testForm: Form[BigDecimal] =
      Form(
        "value" -> bigDecimal(decimalPlaces, length, isZeroAllowed)
      )

    "must bind a valid BigDecimal" in {
      val result = testForm.bind(Map("value" -> "1"))
      result.get mustEqual 1
    }

    "must bind a valid BigDecimal with comma separators" in {
      val result = testForm.bind(Map("value" -> "1,000"))
      result.get mustEqual 1000
    }

    "must bind a valid BigDecimal with spaces" in {
      val result = testForm.bind(Map("value" -> "1 000 000"))
      result.get mustEqual 1000000
    }

    "must bind a valid BigDecimal with fraction" in {
      val result = testForm.bind(Map("value" -> "1.2"))
      result.get mustEqual BigDecimal("1.2")
    }

    "must bind a valid BigDecimal with max integer length" in {
      val result = testForm.bind(Map("value" -> "1234567890123456"))
      result.get mustEqual BigDecimal("1234567890123456")
    }

    "must bind a valid BigDecimal with max fraction length" in {
      val result = testForm.bind(Map("value" -> "1.123456"))
      result.get mustEqual BigDecimal("1.123456")
    }

    "must bind a valid BigDecimal with max integer and fraction length" in {
      val result = testForm.bind(Map("value" -> "1234567890123456.123456"))
      result.get mustEqual BigDecimal("1234567890123456.123456")
    }

    "must not bind BigDecimal with more than max integer length" in {
      val result = testForm.bind(Map("value" -> "12345678901234567"))
      result.errors must contain(FormError("value", "error.invalidValue"))
    }

    "must not bind BigDecimal with more than max fraction length" in {
      val result = testForm.bind(Map("value" -> "0.1234567"))
      result.errors must contain(FormError("value", "error.invalidFormat"))
    }

    "must not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must unbind a valid value" in {
      val result = testForm.fill(123)
      result.apply("value").value.value mustEqual "123"
    }

    "when zero is allowed" - {
      "must bind 0" in {
        val testForm: Form[BigDecimal] =
          Form(
            "value" -> bigDecimal(decimalPlaces, length, isZeroAllowed = true)
          )

        val result = testForm.bind(Map("value" -> "0"))
        result.get mustEqual 0
      }
    }

    "when zero is not allowed" - {
      "must not bind 0" in {
        val testForm: Form[BigDecimal] =
          Form(
            "value" -> bigDecimal(decimalPlaces, length, isZeroAllowed = false)
          )

        val result = testForm.bind(Map("value" -> "0"))
        result.errors must contain(FormError("value", "error.zero"))
      }
    }
  }

  "enumerable" - {

    val testForm = Form(
      "value" -> enumerable[Foo]()
    )

    "must bind a valid option" in {
      val result = testForm.bind(Map("value" -> "Bar"))
      result.get mustEqual Bar
    }

    "must not bind an invalid option" in {
      val result = testForm.bind(Map("value" -> "Not Bar"))
      result.errors must contain(FormError("value", "error.invalid"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }
  }
}
