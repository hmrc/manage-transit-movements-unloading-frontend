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

package models.messages

import scala.util.matching.Regex

object UnloadingRemarksRequest {

  val transportIdentityLength                       = 27
  val numberOfItems                                 = 99999
  val numberOfPackagesLength                        = 8
  val presentationOfficeLength                      = 8
  val newSealNumberMaximumLength                    = 20
  val vehicleIdentificationNumberMaxLength          = 35
  val alphaNumericRegex                             = "^[a-zA-Z0-9 ]*$"
  val alphaNumericWithSpacesRegex: Regex            = "^[a-zA-Z\\s0-9]*$".r
  val stringFieldRegexComma: Regex                  = "[\\sa-zA-Z0-9&'@,/.\\-? ]*".r
  val weightRegex                                   = "^(\\d{1,16}|(\\d{0,15}\\.{1}\\d{1,6}){1})$"
  val weightIntegerLength                           = 16
  val weightDecimalLength                           = 6
  val weightCharsRegex                              = "^([[0-9])(\\.])*"
  val newContainerIdentificationNumberMaximumLength = 17
  val numericRegex: Regex                           = "^[0-9]*$".r
  val commodityCodeLength                           = 6
  val combinedNomenclatureCodeLength                = 2
  val additionalReferenceNumberMaximumLength        = 70
}
