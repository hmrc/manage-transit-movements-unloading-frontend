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
import models.{ArrivalId, UserAnswers}
import play.api.Logging
import play.api.http.HeaderNames
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, InternalServerError}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpClient, HttpErrorFunctions, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApiConnector @Inject() (httpClient: HttpClient, appConfig: FrontendAppConfig)(implicit ec: ExecutionContext) extends HttpErrorFunctions with Logging {

  private val requestHeaders = Seq(
    HeaderNames.ACCEPT       -> "application/vnd.hmrc.2.0+json",
    HeaderNames.CONTENT_TYPE -> "application/json"
  )

  def submit(userAnswers: UserAnswers, arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Either[Result, HttpResponse]] = {

    val serviceUrl = s"${appConfig.commonTransitConventionTradersUrl}movements/arrivals/${arrivalId.value}/messages"

    httpClient
      .POSTString[HttpResponse](serviceUrl, userAnswers.data.toString, requestHeaders)
      .map {
        response =>
          logger.debug(s"ApiConnector:submit: success: ${response.status}-${response.body}")
          Right(response)
      }
      .recover {
        case httpEx: BadRequestException =>
          logger.warn(s"ApiConnector:submit: bad request: ${httpEx.responseCode}-${httpEx.getMessage}")
          Left(BadRequest("ApiConnector:submit: bad request"))
        case e: Exception =>
          logger.error(s"ApiConnector:submit: failed with exception: ${e.getMessage}")
          Left(InternalServerError(s"ApiConnector:submit: failed with exception: ${e.getMessage}"))
      }
  }
}
