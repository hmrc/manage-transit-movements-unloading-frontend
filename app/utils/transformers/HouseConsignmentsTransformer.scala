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

import generated.HouseConsignmentType04
import models.{Index, UserAnswers}
import pages.houseConsignment.index.GrossWeightPage
import pages.sections.HouseConsignmentSection
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HouseConsignmentsTransformer @Inject() (
  consigneeTransformer: ConsigneeTransformer,
  consignorTransformer: ConsignorTransformer,
  departureTransportMeansTransformer: DepartureTransportMeansTransformer,
  consignmentItemTransformer: ConsignmentItemTransformer
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(houseConsignments: Seq[HouseConsignmentType04])(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    houseConsignments.zipWithIndex
      .foldLeft(Future.successful(userAnswers))({
        case (acc, (houseConsignment, i)) =>
          acc.flatMap {
            userAnswers =>
              val hcIndex = Index(i)
              val pipeline =
                setSequenceNumber(HouseConsignmentSection(hcIndex), houseConsignment.sequenceNumber) andThen
                  set(GrossWeightPage(hcIndex), houseConsignment.grossMass) andThen
                  consigneeTransformer.transform(houseConsignment.Consignee, hcIndex) andThen
                  consignorTransformer.transform(houseConsignment.Consignor, hcIndex) andThen
                  departureTransportMeansTransformer.transform(houseConsignment.DepartureTransportMeans, hcIndex) andThen
                  consignmentItemTransformer.transform(houseConsignment.ConsignmentItem, hcIndex)

              pipeline(userAnswers)
          }
      })
}
