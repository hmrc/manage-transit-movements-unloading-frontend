/*
 * Copyright 2023 HM Revenue & Customs
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

package connectors

import akka.event.Logging
import config.FrontendAppConfig
import logging.Logging
import models.ArrivalId
import models.P5.{IE043Data, MessageMetaData, Messages}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ArrivalMovementConnector @Inject() (config: FrontendAppConfig, http: HttpClient) extends Logging {

  def getMessageMetaData(arrivalId: ArrivalId, messageId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[MessageMetaData]] = {
    val headers = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+json"))

    val serviceUrl = s"${config.commonTransitConventionTradersUrl}movements/arrivals/${arrivalId.value}/messages/$messageId"

    http
      .GET[MessageMetaData](serviceUrl)(implicitly, headers, ec)
      .map {
        case response => Some(response)
        case _        => None
      }
      .recover {
        case e =>
          logger.error(s"Failed to get arrival movements with error: $e")
          None
      }
  }

  def getUnloadingPermission(path: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[IE043Data] = {
    val headers = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+json"))

    val serviceUrl = s"${config.commonTransitConventionTradersUrl}$path"

    http.GET[IE043Data](serviceUrl)(implicitly, headers, ec)
  }
}
