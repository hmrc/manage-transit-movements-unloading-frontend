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

package connectors

import config.FrontendAppConfig
import javax.inject.Inject
import logging.Logging
import models.QueryGroupsEnrolmentsResponseModel
import play.api.http.Status._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class EnrolmentStoreConnector @Inject() (val config: FrontendAppConfig, val http: HttpClient)(implicit ec: ExecutionContext) extends Logging {

  def checkGroupEnrolments(groupId: String, enrolmentKey: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val serviceUrl: String = s"${config.enrolmentProxyUrl}/enrolment-store/groups/$groupId/enrolments?type=principal&service=$enrolmentKey"

    http.GET[HttpResponse](serviceUrl).map {
      response =>
        response.status match {
          case OK         => response.json.as[QueryGroupsEnrolmentsResponseModel].enrolments.exists(_.service.contains(enrolmentKey))
          case NO_CONTENT => false
          case other =>
            logger.info(s"[EnrolmentStoreProxyConnector][checkSaGroup] Enrolment Store Proxy error with status $other")
            false
        }
    } recover {
      case exception =>
        logger.info("[EnrolmentStoreProxyConnector][checkSaGroup] Enrolment Store Proxy error", exception)
        false
    }
  }
}
