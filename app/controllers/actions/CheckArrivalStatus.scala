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

import models.ArrivalId
import models.P5.ArrivalMessageType.{RejectionFromOfficeOfDestination, UnloadingPermission}
import models.requests.IdentifierRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import services.P5.UnloadingPermissionMessageService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckArrivalStatusProvider @Inject() (unloadingPermissionMessageService: UnloadingPermissionMessageService)(implicit ec: ExecutionContext) {

  def apply(arrivalId: ArrivalId): ActionFilter[IdentifierRequest] =
    new ArrivalStatusAction(arrivalId, unloadingPermissionMessageService)
}

class ArrivalStatusAction(
  arrivalId: ArrivalId,
  unloadingPermissionMessageService: UnloadingPermissionMessageService
)(implicit protected val executionContext: ExecutionContext)
    extends ActionFilter[IdentifierRequest] {

  override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    unloadingPermissionMessageService.getMessageHead(arrivalId).flatMap {
      case Some(messageMetaData) =>
        messageMetaData.messageType match {
          case UnloadingPermission => Future.successful(None)
          case _                   => Future.successful(Option(Redirect(controllers.routes.CannotSendUnloadingRemarksController.onPageLoad(arrivalId))))
        }
      case _ => Future.successful(Option(Redirect(controllers.routes.CannotSendUnloadingRemarksController.onPageLoad(arrivalId))))

    }
  }

}
