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

import play.api.data.validation.{Constraint, Invalid, Valid}

import java.time.LocalDate
import scala.util.matching.Regex

trait Constraints {

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint {
      input =>
        constraints
          .map(_.apply(input))
          .find(_ != Valid)
          .getOrElse(Valid)
    }

  protected def minimumValue[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>
        import ev.*

        if (input >= minimum) {
          Valid
        } else {
          Invalid(errorKey, minimum)
        }
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>
        import ev.*

        if (input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, maximum)
        }
    }

  protected def inRange[A](minimum: A, maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>
        import ev.*

        if (input >= minimum && input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, minimum, maximum)
        }
    }

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.matches(regex) =>
        Valid
      case _ =>
        Invalid(errorKey, regex)
    }

  protected def regexp(regex: Regex, errorKey: String): Constraint[String] =
    regexp(regex, errorKey, Seq(regex.regex))

  protected def regexp(regex: Regex, errorKey: String, args: Seq[Any]): Constraint[String] =
    Constraint {
      case str if str.matches(regex.pattern.pattern()) =>
        Valid
      case _ =>
        Invalid(errorKey, args*)
    }

  protected def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _ =>
        Invalid(errorKey, maximum)
    }

  protected def exactLength(exact: Int, errorKey: String): Constraint[String] =
    lengthConstraint(errorKey, _.length == exact, Seq(exact))

  private def lengthConstraint(errorKey: String, predicate: String => Boolean, args: Seq[Any]): Constraint[String] =
    Constraint {
      case str if predicate(str) =>
        Valid
      case _ =>
        Invalid(errorKey, args*)
    }

  protected def maxDate(maximum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isAfter(maximum) =>
        Invalid(errorKey, args*)
      case _ =>
        Valid
    }

  protected def minDate(minimum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isBefore(minimum) =>
        Invalid(errorKey, args*)
      case _ =>
        Valid
    }

  protected def nonEmptySet(errorKey: String): Constraint[Set[?]] =
    Constraint {
      case set if set.nonEmpty =>
        Valid
      case _ =>
        Invalid(errorKey)
    }

  protected def valueIsNotInList[T](list: Seq[T], errorKey: String): Constraint[T] =
    Constraint {
      case x if list.contains(x) => Invalid(errorKey)
      case _                     => Valid
    }

  protected def cl234Constraint(isDocumentInCL234: Boolean, errorKey: String): Constraint[String] =
    Constraint {
      case "0" if isDocumentInCL234 =>
        Invalid(errorKey)
      case _ =>
        Valid
    }

  protected def valid[T]: Constraint[T] =
    Constraint(
      _ => Valid
    )
}
