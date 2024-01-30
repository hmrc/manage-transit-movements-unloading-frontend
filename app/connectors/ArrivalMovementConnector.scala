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

import config.FrontendAppConfig
import models.ArrivalId
import models.P5.{IE043Data, Messages}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Node, XML}

class ArrivalMovementConnector @Inject() (config: FrontendAppConfig, http: HttpClient) {

  def getMessageMetaData(arrivalId: ArrivalId)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Messages] = {
    val headers = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+json"))

    val serviceUrl = s"${config.commonTransitConventionTradersUrl}movements/arrivals/${arrivalId.value}/messages"

    http.GET[Messages](serviceUrl)(implicitly, headers, ec)
  }

  def getUnloadingPermission(path: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[IE043Data] = {
    val headers = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+json"))

    val serviceUrl = s"${config.commonTransitConventionTradersUrl}$path"

    http.GET[IE043Data](serviceUrl)(implicitly, headers, ec)
  }

  def getUnloadingPermissionXml(path: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Node] = {
    val headers = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+xml"))

    val url = s"${config.commonTransitConventionTradersUrl}$path/body"

    http.GET[HttpResponse](url)(readRaw, headers, ec).map(_.body).map(XML.loadString)
  }
}
