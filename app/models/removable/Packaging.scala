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

import models.reference.PackageType
import models.{Index, UserAnswers}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads

case class Packaging(`type`: PackageType, quantity: Option[BigInt]) {

  def forRemoveDisplay: String = quantity match {
    case Some(value) => s"$value ${`type`.toString}"
    case None        => `type`.toString
  }

  def forAddAnotherDisplay: String = quantity match {
    case Some(value) => s"$value * ${`type`.toString}"
    case None        => `type`.toString
  }
}

object Packaging {

  def apply(userAnswers: UserAnswers, houseConsignmentIndex: Index, itemIndex: Index, packageIndex: Index): Option[Packaging] = {
    import pages.houseConsignment.index.items.packages._
    implicit val reads: Reads[Packaging] = (
      PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex).path.read[PackageType] and
        NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex).path.readNullable[BigInt]
    ).apply {
      (packageType, quantity) => Packaging(packageType, quantity)
    }
    userAnswers.data.asOpt[Packaging]
  }
}
