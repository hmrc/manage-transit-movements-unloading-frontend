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

import models.ArrivalId
import models.requests.{IdentifierRequest, OptionalDataRequest}
import play.api.mvc.ActionTransformer
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionProviderImpl @Inject() (sessionRepository: SessionRepository, ec: ExecutionContext) extends DataRetrievalActionProvider {

  def apply(arrivalId: ArrivalId): ActionTransformer[IdentifierRequest, OptionalDataRequest] =
    new DataRetrievalAction(arrivalId, ec, sessionRepository)
}

trait DataRetrievalActionProvider {

  def apply(arrivalId: ArrivalId): ActionTransformer[IdentifierRequest, OptionalDataRequest]
}

class DataRetrievalAction(
  arrivalId: ArrivalId,
  implicit protected val executionContext: ExecutionContext,
  sessionRepository: SessionRepository
) extends ActionTransformer[IdentifierRequest, OptionalDataRequest] {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] =
    sessionRepository.get(arrivalId, request.eoriNumber).map {
      userAnswers =>
        OptionalDataRequest(request.request, request.eoriNumber, userAnswers)
    }
}
