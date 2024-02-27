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
import models.reference.Item
import models.{Index, UserAnswers}
import pages.incident.transportEquipment.IncidentItemNumberPage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IncidentGoodsReferencesTransformer @Inject() (implicit ec: ExecutionContext) extends PageTransformer {

  def transform(goodsReferenceSeq: Seq[GoodsReferenceType01], incidentIndex: Index, equipmentIndex: Index): UserAnswers => Future[UserAnswers] = userAnswers =>
    goodsReferenceSeq.zipWithIndex.foldLeft(Future.successful(userAnswers))({
      case (acc, (GoodsReferenceType01(sequenceNumber, declarationGoodsItemNumber), i)) =>
        acc.flatMap {
          userAnswers =>
            val itemIndex: Index = Index(i)
            val pipeline: UserAnswers => Future[UserAnswers] =
              set(IncidentItemNumberPage(equipmentIndex, incidentIndex, itemIndex), Item(declarationGoodsItemNumber.intValue, sequenceNumber))
            pipeline(userAnswers)
        }
    })
}
