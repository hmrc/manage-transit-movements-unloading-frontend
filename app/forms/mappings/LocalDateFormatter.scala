/*
 * Copyright 2025 HM Revenue & Customs
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

import forms.mappings.LocalDateFormatter.*
import models.RichString
import play.api.data.FormError
import play.api.data.format.Formatter

import java.time.{LocalDate, Month, Year}
import scala.util.{Success, Try}

private[mappings] class LocalDateFormatter(
  invalidKey: String,
  requiredKey: String
) extends Formatter[LocalDate]
    with Formatters {

  private def bind[T](
    key: String,
    data: Map[String, String],
    field: Field,
    args: Any*
  )(f: Int => T)(predicate: T => Boolean): Either[FieldError, T] =
    stringFormatter(requiredKey, Seq(field.key))(_.removeSpaces()).bind(field.id(key), data) match {
      case Left(errors) =>
        Left(FieldError(field, requiredKey))
      case Right(value) =>
        Try(f(Integer.parseInt(value))) match {
          case Success(t) if predicate(t) =>
            Right(t)
          case _ =>
            Left(FieldError(field, invalidKey, args*))
        }
    }

  private def bindDay(key: String, data: Map[String, String], month: Month, isLeap: Boolean): Either[FieldError, Int] =
    bindDay(key, data, month.length(isLeap))

  private def bindDay(key: String, data: Map[String, String], days: Int): Either[FieldError, Int] =
    bind(key, data, DayField, days)(identity)(1 to days contains _)

  private def bindMonth(key: String, data: Map[String, String]): Either[FieldError, Month] =
    bind(key, data, MonthField)(Month.of) {
      _ => true
    }

  private def bindYear(key: String, data: Map[String, String]): Either[FieldError, Year] =
    bind(key, data, YearField)(Year.of) {
      _ => true
    }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] =
    (bindMonth(key, data), bindYear(key, data)) match {
      case (Right(month), Right(year)) =>
        bindDay(key, data, month, year.isLeap) match {
          case Right(day) =>
            Right(LocalDate.of(year.getValue, month, day))
          case dayBinding @ Left(_) =>
            Left(Seq(dayBinding).toFormErrors(key))
        }
      case (Right(month), yearBinding @ Left(_)) =>
        val dayBinding = bindDay(key, data, month, true)
        Left(Seq(dayBinding, yearBinding).toFormErrors(key))
      case (monthBinding, yearBinding) =>
        val dayBinding = bindDay(key, data, 31)
        Left(Seq(dayBinding, monthBinding, yearBinding).toFormErrors(key))
    }

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      DayField.id(key)   -> value.getDayOfMonth.toString,
      MonthField.id(key) -> value.getMonthValue.toString,
      YearField.id(key)  -> value.getYear.toString
    )

  implicit class RichBindings(value: Seq[Either[FieldError, ?]]) {

    def toFormErrors(key: String): Seq[FormError] =
      value
        .collect {
          case Left(fieldErrors) => fieldErrors
        }
        .groupByPreserveOrder(_.errorKey)
        .flatMap {
          case (errorKey, errors) if errors.size == 3 =>
            val fields = errors.toSeq.map(_.field)
            fields.map {
              field => FormError(field.id(key), s"$errorKey.all", fields.map(_.key))
            }
          case (errorKey, errors) if errors.size == 2 =>
            val fields = errors.toSeq.map(_.field)
            fields.map {
              field => FormError(field.id(key), s"$errorKey.multiple", fields.map(_.key))
            }
          case (_, error :: Nil) =>
            val field = error.field
            Seq(FormError(field.id(key), error.messageKey, error.args :+ field.key))
          case _ =>
            Nil
        }
  }
}

object LocalDateFormatter {

  val fieldKeys: List[String] = List(DayField.key, MonthField.key, YearField.key)

  sealed trait Field {
    val key: String

    def id(field: String): String = s"$field.$key"
  }

  private case object DayField extends Field {
    override val key: String = "day"
  }

  private case object MonthField extends Field {
    override val key: String = "month"
  }

  private case object YearField extends Field {
    override val key: String = "year"
  }

  case class FieldError(field: Field, errorKey: String, args: Any*) {

    val messageKey: String = s"$errorKey.${field.key}"
  }
}
