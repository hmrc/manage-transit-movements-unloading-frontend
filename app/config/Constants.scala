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
  val GB  = "GB"
  val XI  = "XI"
  val AD  = "AD"
  val XXX = "XXX"

  val STANDARD    = "A"
  val `PRE-LODGE` = "D"

  val TIR = "TIR"
  val T2  = "T2"
  val T   = "T"

  val NoSecurityDetails                             = "0"
  val EntrySummaryDeclarationSecurityDetails        = "1"
  val ExitSummaryDeclarationSecurityDetails         = "2"
  val EntryAndExitSummaryDeclarationSecurityDetails = "3"

  val Maritime = "1"
  val Rail     = "2"
  val Road     = "3"
  val Air      = "4"
  val Mail     = "5"
  val Fixed    = "7"
  val Unknown  = "9"

  val CustomsOfficeIdentifier       = "V"
  val EoriNumberIdentifier          = "X"
  val AuthorisationNumberIdentifier = "Y"
  val UnlocodeIdentifier            = "U"
  val CoordinatesIdentifier         = "W"
  val AddressIdentifier             = "Z"
  val PostalCodeIdentifier          = "T"

  val DesignatedLocation = "A"
  val AuthorisedPlace    = "B"
  val ApprovedPlace      = "C"
  val Other              = "D"

  lazy val maxEoriNumberLength: Int        = 17
  lazy val maxContainerIdNumberLength: Int = 17

  object MeansOfTransportIdentification {
    val UnknownIdentification = "99"
  }
}
