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

package config

object Constants {
  val GB = "GB"
  val AD = "AD"

  val T2 = "T2"
  val T  = "T"

  val Maritime = "1"
  val Rail     = "2"
  val Road     = "3"
  val Air      = "4"
  val Mail     = "5"
  val Fixed    = "7"
  val Unknown  = "9"
  val Other    = "D"

  object MeansOfTransportIdentification {
    val UnknownIdentification = "99"
  }
}
