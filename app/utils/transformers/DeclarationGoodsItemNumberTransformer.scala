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

import generated.CommodityType08
import models.{Index, UserAnswers}
import pages.houseConsignment.index.items.{CustomsUnionAndStatisticsCodePage, DeclarationGoodsItemNumberPage, ItemDescriptionPage}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationGoodsItemNumberTransformer @Inject() (implicit ec: ExecutionContext) extends PageTransformer {

  def transform(declarationGoodsItemNumber: BigInt, hcIndex: Index, itemIndex: Index): UserAnswers => Future[UserAnswers] = userAnswers => {
    lazy val pipeline: UserAnswers => Future[UserAnswers] = set(DeclarationGoodsItemNumberPage(hcIndex, itemIndex), declarationGoodsItemNumber.toString())
    pipeline(userAnswers)

  }

}
