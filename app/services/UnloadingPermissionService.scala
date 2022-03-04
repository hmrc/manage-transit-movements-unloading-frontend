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
import models.{ArrivalId, UnloadingPermission, UserAnswers}
import queries.SealsQuery
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class UnloadingPermissionServiceImpl @Inject() (connector: UnloadingConnector) extends UnloadingPermissionService {

  def getUnloadingPermission(arrivalId: ArrivalId)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[UnloadingPermission]] =
    connector.getSummary(arrivalId) flatMap {
      case Some(summary) =>
        connector.getUnloadingPermission(summary.messagesLocation.unloadingPermission)
      case _ => Future.successful(None)
    }

  def convertSeals(userAnswers: UserAnswers)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[UserAnswers]] =
    getUnloadingPermission(userAnswers.id).map {
      _.flatMap {
        unloadingPermission =>
          unloadingPermission.seals.fold(Option(userAnswers)) {
            seals =>
              userAnswers.set(SealsQuery, seals.SealId).toOption
          }
      }
    }

  def convertSeals(userAnswers: UserAnswers, unloadingPermission: UnloadingPermission): Option[UserAnswers] =
    unloadingPermission.seals match {
      case Some(seals) => userAnswers.set(SealsQuery, seals.SealId).toOption
      case _           => Some(userAnswers)
    }
}

trait UnloadingPermissionService {
  def getUnloadingPermission(arrivalId: ArrivalId)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[UnloadingPermission]]
  def convertSeals(userAnswers: UserAnswers)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[UserAnswers]]
}
