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

package services

import models.{GoodsReference, Index, RichOptionalJsArray, UserAnswers}
import pages.houseConsignment.index.items.{DeclarationGoodsItemNumberPage, ItemDescriptionPage}
import pages.transportEquipment.index.ItemPage

import javax.inject.Inject

class GoodsReferenceService @Inject() {

  def getGoodsReferences(userAnswers: UserAnswers): Seq[GoodsReference] = {
    import pages.sections.TransportEquipmentListSection
    import pages.sections.transport.equipment.ItemsSection
    println("***")
    println(userAnswers.data)
    (for {
      equipmentIndex      <- 0 until userAnswers.get(TransportEquipmentListSection).length
      goodsReferenceIndex <- 0 until userAnswers.get(ItemsSection(Index(equipmentIndex))).length
    } yield {
      println("***")
      println(equipmentIndex)
      println(goodsReferenceIndex)
      getGoodsReference(userAnswers, Index(equipmentIndex), Index(goodsReferenceIndex))
    }).flatten
  }

  def getGoodsReference(userAnswers: UserAnswers, equipmentIndex: Index, goodsReferenceIndex: Index): Option[GoodsReference] = {
    import pages.sections.{HouseConsignmentsSection, ItemsSection}

    userAnswers.get(ItemPage(equipmentIndex, goodsReferenceIndex)).flatMap {
      goodsReference =>
        (for {
          hcIndex                    <- 0 until userAnswers.get(HouseConsignmentsSection).length
          itemIndex                  <- 0 until userAnswers.get(ItemsSection(Index(hcIndex))).length
          declarationGoodsItemNumber <- userAnswers.get(DeclarationGoodsItemNumberPage(Index(hcIndex), Index(itemIndex)))
          description                <- userAnswers.get(ItemDescriptionPage(Index(hcIndex), Index(itemIndex)))
        } yield GoodsReference(declarationGoodsItemNumber, description)).find {
          _.declarationGoodsItemNumber == goodsReference.declarationGoodsItemNumber
        }
    }
  }
}
