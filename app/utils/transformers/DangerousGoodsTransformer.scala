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

import generated.DangerousGoodsType01
import models.{Index, UserAnswers}
import pages.houseConsignment.index.items.DangerousGoodsPage
import pages.sections.houseConsignment.index.items.dangerousGoods.DangerousGoodsSection

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DangerousGoodsTransformer @Inject() (implicit ec: ExecutionContext) extends PageTransformer {

  def transform(dangerousGoods: Seq[DangerousGoodsType01], hcIndex: Index, itemIndex: Index): UserAnswers => Future[UserAnswers] =
    userAnswers =>
      dangerousGoods.zipWithIndex.foldLeft(Future.successful(userAnswers)) {
        case (acc, (DangerousGoodsType01(sequenceNumber, unNumber), i)) =>
          val index = Index(i)
          acc.flatMap {
            setSequenceNumber(DangerousGoodsSection(hcIndex, itemIndex, index), sequenceNumber) andThen
              set(DangerousGoodsPage(hcIndex, itemIndex, index), unNumber)
          }
      }
}
