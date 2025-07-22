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

import generated.GoodsReferenceType01
import models.{Index, UserAnswers}
import pages.sections.transport.equipment.ItemSection
import pages.transportEquipment.index.ItemPage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GoodsReferencesTransformer @Inject() (implicit ec: ExecutionContext) extends PageTransformer {

  def transform(
    goodsReferences: Seq[GoodsReferenceType01],
    equipmentIndex: Index
  ): UserAnswers => Future[UserAnswers] =
    goodsReferences.mapWithSets {
      (value, itemIndex) =>
        setSequenceNumber(ItemSection(equipmentIndex, itemIndex), value.sequenceNumber) andThen
          set(ItemPage(equipmentIndex, itemIndex), value.declarationGoodsItemNumber)
    }
}
