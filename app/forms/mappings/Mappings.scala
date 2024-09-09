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

import models.reference.Selectable

import java.time.LocalDate
import play.api.data.FieldMapping
import play.api.data.Forms.of
import models.{Enumerable, Radioable, SelectableList}
import play.api.data.format.Formats.ignoredFormat

trait Mappings extends Formatters with Constraints {

  protected def text(errorKey: String = "error.required", args: Seq[String] = Seq.empty): FieldMapping[String] =
    of(stringFormatter(errorKey, args)(identity))

  protected def adaptedText(errorKey: String = "error.required", args: Seq[Any] = Seq.empty)(f: String => String): FieldMapping[String] =
    of(stringFormatter(errorKey, args)(f))

  protected def int(
    requiredKey: String = "error.required",
    wholeNumberKey: String = "error.wholeNumber",
    nonNumericKey: String = "error.nonNumeric"
  ): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey))

  protected def bigInt(
    requiredKey: String = "error.required",
    wholeNumberKey: String = "error.wholeNumber",
    nonNumericKey: String = "error.nonNumeric",
    args: Seq[String] = Seq.empty
  ): FieldMapping[BigInt] =
    of(bigIntFormatter(requiredKey, wholeNumberKey, nonNumericKey, args))

  protected def boolean(requiredKey: String = "error.required", invalidKey: String = "error.boolean", args: Seq[Any] = Seq.empty): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey, args))

  protected def enumerable[A <: Radioable[A]](requiredKey: String = "error.required", invalidKey: String = "error.invalid", args: Seq[Any] = Seq.empty)(implicit
    ev: Enumerable[A]
  ): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey, args))

  protected def bigDecimal(
    decimalPlaces: Int,
    integerLength: Int,
    isZeroAllowed: Boolean,
    prefix: String = "error",
    requiredKey: String = "error.required",
    args: Seq[String] = Seq.empty
  ): FieldMapping[BigDecimal] =
    of(bigDecimalFormatter(decimalPlaces, integerLength, isZeroAllowed, prefix, requiredKey, args))

  protected def localDate(
    invalidKey: String,
    requiredKey: String
  ): FieldMapping[LocalDate] =
    of(new LocalDateFormatter(invalidKey, requiredKey))

  protected def selectable[T <: Selectable](
    selectableList: SelectableList[T],
    errorKey: String = "error.required",
    args: Seq[Any] = Seq.empty
  ): FieldMapping[T] =
    of(selectableFormatter[T](selectableList, errorKey, args))

  protected def mandatoryIfBoolean(errorKey: String, condition: Boolean, defaultValue: Boolean, args: Any*): FieldMapping[Boolean] =
    if (condition) boolean(errorKey, args = args) else of(ignoredFormat(defaultValue))
}
