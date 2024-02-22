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

import models.UserAnswers
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IE043Transformer @Inject() (
  consignmentTransformer: ConsignmentTransformer,
  transitOperationTransformer: TransitOperationTransformer,
  customsOfficeOfDestinationActualTransformer: CustomsOfficeOfDestinationActualTransformer
) extends FrontendHeaderCarrierProvider {

  def transform(userAnswers: UserAnswers)(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[UserAnswers] = {

    val transformerPipeline =
      transitOperationTransformer.transform(userAnswers.ie043Data.TransitOperation) andThen
        consignmentTransformer.transform(userAnswers.ie043Data.Consignment) andThen
        customsOfficeOfDestinationActualTransformer.transform(userAnswers.ie043Data.CustomsOfficeOfDestinationActual)

    transformerPipeline(userAnswers)
  }
}
