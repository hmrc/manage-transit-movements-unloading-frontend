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

package models.removable

import models.reference.AdditionalReferenceType
import models.{Index, UserAnswers}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads

case class AdditionalReference(`type`: AdditionalReferenceType, referenceNumber: Option[String]) {

  def forRemoveDisplay: String = referenceNumber match {
    case Some(value) => s"${`type`} - $value"
    case None        => `type`.toString
  }

  def forAddAnotherDisplay: String = referenceNumber match {
    case Some(value) => s"${`type`.documentType} - $value"
    case None        => `type`.documentType
  }
}

object AdditionalReference {

  def apply(userAnswers: UserAnswers, additionalReferenceIndex: Index): Option[AdditionalReference] = {
    import pages.additionalReference._
    implicit val reads: Reads[AdditionalReference] = (
      AdditionalReferenceTypePage(additionalReferenceIndex).path.read[AdditionalReferenceType] and
        AdditionalReferenceNumberPage(additionalReferenceIndex).path.readNullable[String]
    ).apply {
      (additionalReferenceType, referenceNumber) => AdditionalReference(additionalReferenceType, referenceNumber)
    }
    userAnswers.data.asOpt[AdditionalReference]
  }

  def apply(userAnswers: UserAnswers, houseConsignmentIndex: Index, additionalReferenceIndex: Index): Option[AdditionalReference] = {
    import pages.houseConsignment.index.additionalReference._
    implicit val reads: Reads[AdditionalReference] = (
      HouseConsignmentAdditionalReferenceTypePage(houseConsignmentIndex, additionalReferenceIndex).path.read[AdditionalReferenceType] and
        HouseConsignmentAdditionalReferenceNumberPage(houseConsignmentIndex, additionalReferenceIndex).path.readNullable[String]
    ).apply {
      (additionalReferenceType, referenceNumber) => AdditionalReference(additionalReferenceType, referenceNumber)
    }
    userAnswers.data.asOpt[AdditionalReference]
  }

  def apply(userAnswers: UserAnswers, houseConsignmentIndex: Index, itemIndex: Index, additionalReferenceIndex: Index): Option[AdditionalReference] = {
    import pages.houseConsignment.index.items.additionalReference._
    implicit val reads: Reads[AdditionalReference] = (
      AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex).path.read[AdditionalReferenceType] and
        AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex).path.readNullable[String]
    ).apply {
      (additionalReferenceType, referenceNumber) => AdditionalReference(additionalReferenceType, referenceNumber)
    }
    userAnswers.data.asOpt[AdditionalReference]
  }

}
