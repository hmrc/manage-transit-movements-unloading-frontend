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

package utils.transformers

import base.SpecBase
import generated.SealType01
import generators.Generators
import models.Index
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.sections.SealSection
import pages.transportEquipment.index.seals.SealIdentificationNumberPage

import scala.concurrent.ExecutionContext.Implicits.global

class SealsTransformerSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val transformer = new SealsTransformer()

  "must transform data" in {
    forAll(arbitrary[Seq[SealType01]]) {
      seals =>
        val result = transformer.transform(seals, equipmentIndex).apply(emptyUserAnswers).futureValue

        seals.zipWithIndex.map {
          case (seal, i) =>
            result.getSequenceNumber(SealSection(equipmentIndex, Index(i))) mustEqual seal.sequenceNumber
            result.getValue(SealIdentificationNumberPage(equipmentIndex, Index(i))) mustEqual seal.identifier
        }
    }
  }
}
