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

import generated.TransportEquipmentType05
import models.{Index, UserAnswers}
import pages.ContainerIdentificationNumberPage
import pages.sections.TransportEquipmentSection
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportEquipmentTransformer @Inject() (
  sealsTransformer: SealsTransformer,
  itemsTransformer: ItemsTransformer
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(transportEquipment: Seq[TransportEquipmentType05]): UserAnswers => Future[UserAnswers] = userAnswers =>
    transportEquipment.zipWithIndex.foldLeft(Future.successful(userAnswers))({
      case (acc, (TransportEquipmentType05(_, containerIdentificationNumber, _, seals, goodsReference), i)) =>
        acc.flatMap {
          userAnswers =>
            val equipmentIndex: Index = Index(i)
            val pipeline: UserAnswers => Future[UserAnswers] = {
              set(TransportEquipmentSection(equipmentIndex), Json.obj()) andThen
                set(ContainerIdentificationNumberPage(equipmentIndex), containerIdentificationNumber) andThen
                sealsTransformer.transform(seals, equipmentIndex) andThen
                itemsTransformer.transform(goodsReference, equipmentIndex)
            }

            pipeline(userAnswers)
        }
    })
}
