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

import com.google.inject.Inject
import connectors.UnloadingConnector
import models.ArrivalId
import models.messages.UnloadingRemarksRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class UnloadingRemarksMessageService @Inject() (connector: UnloadingConnector) {

  def unloadingRemarksMessage(arrivalId: ArrivalId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[UnloadingRemarksRequest]] =
    connector.getSummary(arrivalId) flatMap {
      case Some(summary) =>
        summary.messagesLocation.unloadingRemarks match {
          case Some(unloadingRemarks) => connector.getUnloadingRemarksMessage(unloadingRemarks)
          case _                      => Future.successful(None)
        }

      case _ => Future.successful(None)
    }
}
