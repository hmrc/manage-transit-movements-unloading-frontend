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

package pages.transportEquipment.index.seals

import generated.SealType04
import models.Index
import pages.DiscrepancyQuestionPage
import pages.sections.SealSection
import play.api.libs.json.JsPath

case class SealIdentificationNumberPage(equipmentIndex: Index, sealIndex: Index) extends DiscrepancyQuestionPage[String, Seq[SealType04], String] {

  override def path: JsPath = SealSection(equipmentIndex, sealIndex).path \ toString

  override def toString: String = "identifier"

  override def valueInIE043(transportEquipment: Seq[SealType04], sequenceNumber: Option[BigInt]): Option[String] =
    transportEquipment
      .find {
        x => sequenceNumber.contains(x.sequenceNumber)
      }
      .map(_.identifier)
}
