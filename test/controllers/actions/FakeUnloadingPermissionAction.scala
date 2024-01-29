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

import base.TestMessageData
import models.requests.{IdentifierRequest, UnloadingPermissionRequest}
import models.{ArrivalId, EoriNumber}
import play.api.mvc.Result
import services.P5.UnloadingPermissionMessageService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FakeUnloadingPermissionAction(arrivalId: ArrivalId, unloadingPermissionMessageService: UnloadingPermissionMessageService)
    extends UnloadingPermissionAction(arrivalId, unloadingPermissionMessageService)
    with TestMessageData {

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, UnloadingPermissionRequest[A]]] =
    Future.successful(Right(UnloadingPermissionRequest(request, EoriNumber("AB123"), basicIe043)))

}
