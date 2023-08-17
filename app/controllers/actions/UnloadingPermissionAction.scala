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

package controllers.actions

import controllers.routes
import logging.Logging
import models.ArrivalId
import models.requests.{IdentifierRequest, UnloadingPermissionRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import services.P5.UnloadingPermissionMessageService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnloadingPermissionActionProvider @Inject() (unloadingPermissionMessageService: UnloadingPermissionMessageService)(implicit ec: ExecutionContext)
    extends Logging {

  def apply(arrivalId: ArrivalId, messageId: String): ActionRefiner[IdentifierRequest, UnloadingPermissionRequest] =
    new UnloadingPermissionAction(arrivalId, messageId, unloadingPermissionMessageService)
}

class UnloadingPermissionAction(arrivalId: ArrivalId, messageId: String, unloadingPermissionMessageService: UnloadingPermissionMessageService)(implicit
  protected val executionContext: ExecutionContext
) extends ActionRefiner[IdentifierRequest, UnloadingPermissionRequest]
    with Logging {

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, UnloadingPermissionRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    unloadingPermissionMessageService
      .getUnloadingPermission(arrivalId, messageId)
      .map {
        unloadingPermission =>
          Right(UnloadingPermissionRequest(request, request.eoriNumber, unloadingPermission.data))
      }
      .recover {
        _ =>
          logger.error("Retreiving UnloadingPermission failed.")
          Left(Redirect(routes.ErrorController.technicalDifficulties()))
      }

  }
}
