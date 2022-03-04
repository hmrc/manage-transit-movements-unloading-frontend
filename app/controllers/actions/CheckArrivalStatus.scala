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

package controllers.actions

import config.FrontendAppConfig
import connectors.UnloadingConnector
import models.ArrivalId
import models.ArrivalStatus.OtherStatus
import models.requests.IdentifierRequest
import models.response.ResponseArrival
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{ActionFilter, Result}
import renderer.Renderer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckArrivalStatusProvider @Inject() (unloadingConnector: UnloadingConnector, renderer: Renderer, appConfig: FrontendAppConfig)(implicit
  ec: ExecutionContext
) {

  def apply(arrivalId: ArrivalId): ActionFilter[IdentifierRequest] =
    new ArrivalStatusAction(arrivalId, unloadingConnector, renderer, appConfig)

}

class ArrivalStatusAction(
  arrivalId: ArrivalId,
  unloadingConnector: UnloadingConnector,
  renderer: Renderer,
  appConfig: FrontendAppConfig
)(implicit protected val executionContext: ExecutionContext)
    extends ActionFilter[IdentifierRequest] {

  override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    unloadingConnector.getArrival(arrivalId).flatMap {
      case Some(ResponseArrival(_, OtherStatus)) =>
        renderer
          .render("canNotSendUnloadingRemarks.njk",
                  Json.obj(
                    "arrivalNotifications" -> s"${appConfig.arrivalNotificationsUrl}"
                  )
          )(request)
          .map(
            html => Option(BadRequest(html))
          )
      case Some(_) => Future.successful(None)
      case None =>
        renderer
          .render("canNotSendUnloadingRemarks.njk",
                  Json.obj(
                    "arrivalNotifications" -> s"${appConfig.arrivalNotificationsUrl}"
                  )
          )(request)
          .map(
            html => Option(NotFound(html))
          )

    }
  }
}
