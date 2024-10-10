/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import scala.annotation.tailrec

case class Bytes(value: String) {

  override def toString: String = value
}

object Bytes {

  def apply(bytes: Int): Bytes = {
    @tailrec
    def rec(values: List[(Int, String)]): String =
      values match
        case Nil =>
          s"$bytes B"
        case (order, unit) :: tail =>
          bytes.toFloat / Math.pow(1024, order) match
            case value if value > 1 => f"$value%1.2f $unit"
            case _                  => rec(tail)

    new Bytes(rec(values))
  }

  private val values = List(
    3 -> "GB",
    2 -> "MB",
    1 -> "KB"
  )
}
