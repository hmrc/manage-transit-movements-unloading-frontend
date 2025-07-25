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

package pages.houseConsignment.index.items.packages

import generated.PackagingType01
import models.Index
import pages.DiscrepancyQuestionPage
import pages.sections.PackagingSection
import play.api.libs.json.JsPath

case class NumberOfPackagesPage(houseConsignmentIndex: Index, itemIndex: Index, packageIndex: Index)
    extends DiscrepancyQuestionPage[BigInt, Seq[PackagingType01], BigInt] {

  override def path: JsPath = PackagingSection(houseConsignmentIndex, itemIndex, packageIndex).path \ toString

  override def toString: String = "numberOfPackages"

  override def valueInIE043(ie043: Seq[PackagingType01], sequenceNumber: Option[BigInt]): Option[BigInt] =
    ie043
      .find {
        x => sequenceNumber.contains(x.sequenceNumber)
      }
      .flatMap(_.numberOfPackages)
}
