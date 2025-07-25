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

package forms

object Constants {
  lazy val maxItemDescriptionLength: Int         = 512
  lazy val exactCUSCodeLength: Int               = 9
  lazy val grossWeightDecimalPlaces: Int         = 6
  lazy val grossWeightIntegerLength: Int         = 16
  lazy val netWeightDecimalPlaces: Int           = 6
  lazy val netWeightIntegerLength: Int           = 16
  lazy val maxPackageShippingMarkLength: Int     = 512
  lazy val maxSealIdentificationLength: Int      = 20
  lazy val maxDocumentRefNumberLength: Int       = 70
  lazy val maxAdditionalInfoLength: Int          = 35
  lazy val maxUCRLength: Int                     = 70
  lazy val maxAdditionalReferenceNumLength: Int  = 70
  lazy val maxNumberOfPackages: BigInt           = 999999999
  lazy val otherThingsToReportLength: Int        = 512
  lazy val maxDocumentsAdditionalInfoLength: Int = 35
}
