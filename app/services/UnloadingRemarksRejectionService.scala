/*
 * Copyright 2022 HM Revenue & Customs
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

import cats.data.OptionT
import cats.implicits._
import com.google.inject.Inject
import connectors.UnloadingConnector
import models.{ArrivalId, UnloadingRemarksRejectionMessage}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class UnloadingRemarksRejectionService @Inject() (connector: UnloadingConnector)(implicit ec: ExecutionContext) {

  def unloadingRemarksRejectionMessage(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Option[UnloadingRemarksRejectionMessage]] =
    (for {
      summary           <- OptionT(connector.getSummary(arrivalId))
      rejectionLocation <- OptionT.fromOption[Future](summary.messagesLocation.unloadingRemarksRejection)
      message           <- OptionT(connector.getRejectionMessage(rejectionLocation))
    } yield message).value
}
