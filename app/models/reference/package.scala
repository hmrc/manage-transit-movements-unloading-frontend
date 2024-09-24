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

package object reference {

  implicit class RichComparison[T](value: (T, T)) {

    def compareBy(fs: (T => String)*): Int =
      value match {
        case (x, y) =>
          fs.toList match {
            case Nil => 0
            case f :: tail =>
              f(x).compareToIgnoreCase(f(y)) match {
                case 0      => compareBy(tail*)
                case result => result
              }
          }
      }
  }
}
