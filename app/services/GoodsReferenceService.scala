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

import models.reference.GoodsReference
import models.{Index, RichOptionalJsArray, UserAnswers}
import pages.houseConsignment.index.items.{DeclarationGoodsItemNumberPage, ItemDescriptionPage}
import pages.sections.{HouseConsignmentsSection, ItemsSection}
import pages.transportEquipment.index.ItemPage
import utils.transformers.Removed

import javax.inject.Inject

class GoodsReferenceService @Inject() {

  def getGoodsReferences(userAnswers: UserAnswers, equipmentIndex: Index, goodsReferenceIndex: Option[Index]): Seq[GoodsReference] = {
    import pages.sections.transport.equipment.{ItemSection => GoodsReferenceSection, ItemsSection => GoodsReferencesSection}

    val unavailableDeclarationGoodsItemNumbers = {
      val numberOfGoodsReferences = userAnswers.get(GoodsReferencesSection(equipmentIndex)).length
      (0 until numberOfGoodsReferences).map(Index(_)).foldLeft(Seq.empty[BigInt]) {
        case (acc, index) =>
          if (goodsReferenceIndex.contains(index)) {
            acc
          } else {
            userAnswers.get[Boolean](GoodsReferenceSection(equipmentIndex, index).path \ Removed) match {
              case Some(true) => acc
              case _ =>
                userAnswers.get(ItemPage(equipmentIndex, index)) match {
                  case Some(value) => acc :+ value
                  case None        => acc
                }
            }
          }
      }
    }

    getGoodsReferences(userAnswers).filterNot {
      goodsReference =>
        unavailableDeclarationGoodsItemNumbers.contains(goodsReference.declarationGoodsItemNumber)
    }
  }

  def getGoodsReference(userAnswers: UserAnswers, equipmentIndex: Index, goodsReferenceIndex: Index): Option[GoodsReference] =
    userAnswers.get(ItemPage(equipmentIndex, goodsReferenceIndex)).flatMap {
      declarationGoodsItemNumber =>
        getGoodsReferences(userAnswers).find {
          _.declarationGoodsItemNumber == declarationGoodsItemNumber
        }
    }

  private def getGoodsReferences(userAnswers: UserAnswers): Seq[GoodsReference] = {
    import pages.sections.{HouseConsignmentsSection, ItemsSection}
    for {
      hcIndex                    <- (0 until userAnswers.get(HouseConsignmentsSection).length).map(Index(_))
      itemIndex                  <- (0 until userAnswers.get(ItemsSection(hcIndex)).length).map(Index(_))
      declarationGoodsItemNumber <- userAnswers.get(DeclarationGoodsItemNumberPage(hcIndex, itemIndex))
      description                <- userAnswers.get(ItemDescriptionPage(hcIndex, itemIndex))
    } yield GoodsReference(declarationGoodsItemNumber, description)
  }

  def getNextDeclarationGoodsItemNumber(userAnswers: UserAnswers): BigInt = {
    val declarationGoodsItemNumbers = for {
      hcIndex                    <- (0 until userAnswers.get(HouseConsignmentsSection).length).map(Index(_))
      itemIndex                  <- (0 until userAnswers.get(ItemsSection(hcIndex)).length).map(Index(_))
      declarationGoodsItemNumber <- userAnswers.get(DeclarationGoodsItemNumberPage(hcIndex, itemIndex))
    } yield declarationGoodsItemNumber

    declarationGoodsItemNumbers.maxOption.getOrElse(BigInt(0)) + 1
  }
}
