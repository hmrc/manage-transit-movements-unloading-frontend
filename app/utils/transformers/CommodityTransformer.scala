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
import pages.houseConsignment.index.items.ItemDescriptionPage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommodityTransformer @Inject() (
  goodsMeasureTransformer: GoodsMeasureTransformer
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(commodity: CommodityType08, hcIndex: Index, itemIndex: Index): UserAnswers => Future[UserAnswers] = userAnswers =>
    commodity match {
      case CommodityType08(descriptionOfGoods, _, _, _, goodsMeasure) =>
        lazy val pipeline: UserAnswers => Future[UserAnswers] =
          set(ItemDescriptionPage(hcIndex, itemIndex), descriptionOfGoods) andThen
            goodsMeasureTransformer.transform(goodsMeasure, hcIndex, itemIndex)

        pipeline(userAnswers)
    }
}
