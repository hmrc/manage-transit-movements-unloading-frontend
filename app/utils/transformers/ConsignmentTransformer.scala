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

import generated.ConsignmentType05
import models.UserAnswers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConsignmentTransformer @Inject() (
  transportEquipmentTransformer: TransportEquipmentTransformer,
  departureTransportMeansTransformer: DepartureTransportMeansTransformer,
  supportingDocumentTransformer: SupportingDocumentTransformer,
  transportDocumentTransformer: TransportDocumentTransformer,
  houseConsignmentTransformer: HouseConsignmentTransformer
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(consignment: Option[ConsignmentType05]): UserAnswers => Future[UserAnswers] = userAnswers =>
    consignment match {
      case Some(
            ConsignmentType05(_,
                              _,
                              _,
                              _,
                              _,
                              _,
                              transportEquipment,
                              departureTransportMeans,
                              _,
                              supportingDocuments,
                              transportDocuments,
                              _,
                              _,
                              _,
                              houseConsignments
            )
          ) =>
        lazy val pipeline: UserAnswers => Future[UserAnswers] =
          transportEquipmentTransformer.transform(transportEquipment) andThen
            departureTransportMeansTransformer.transform(departureTransportMeans) andThen
            supportingDocumentTransformer.transform(supportingDocuments) andThen
            transportDocumentTransformer.transform(transportDocuments) andThen
            houseConsignmentTransformer.transform(houseConsignments)

        pipeline(userAnswers)
      case None =>
        Future.successful(userAnswers)
    }
}
