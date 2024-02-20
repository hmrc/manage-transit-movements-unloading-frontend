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

import generated.ConsignmentItemType04
import models.{Index, UserAnswers}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConsignmentItemTransformer @Inject() (
  commodityTransformer: CommodityTransformer,
  additionalReferenceTransformer: AdditionalReferenceTransformer
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(consignmentItems: Seq[ConsignmentItemType04], hcIndex: Index)(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    consignmentItems.zipWithIndex.foldLeft(Future.successful(userAnswers))({
      case (acc, (ConsignmentItemType04(_, _, _, _, _, commodity, _, _, _, _, additionalReference, _), i)) =>
        acc.flatMap {
          userAnswers =>
            val itemIndex: Index = Index(i)
            val pipeline: UserAnswers => Future[UserAnswers] =
              commodityTransformer.transform(commodity, hcIndex, itemIndex) andThen
                additionalReferenceTransformer.transform(additionalReference, hcIndex, itemIndex)

            pipeline(userAnswers)
        }
    })
}
