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

import generated.TransportEquipmentType03
import models.UserAnswers
import pages.ContainerIdentificationNumberPage
import pages.sections.TransportEquipmentSection

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportEquipmentTransformer @Inject() (
  sealsTransformer: SealsTransformer,
  goodsReferencesTransformer: GoodsReferencesTransformer
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(
    transportEquipment: Seq[TransportEquipmentType03]
  ): UserAnswers => Future[UserAnswers] =
    transportEquipment.mapWithSets {
      (value, equipmentIndex) =>
        setSequenceNumber(TransportEquipmentSection(equipmentIndex), value.sequenceNumber) andThen
          set(ContainerIdentificationNumberPage(equipmentIndex), value.containerIdentificationNumber) andThen
          sealsTransformer.transform(value.Seal, equipmentIndex) andThen
          goodsReferencesTransformer.transform(value.GoodsReference, equipmentIndex)
    }
}
