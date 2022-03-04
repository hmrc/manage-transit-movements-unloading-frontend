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

import java.time.LocalDate

import com.google.inject.Inject
import connectors.UnloadingConnector
import models.{ArrivalId, UnloadingRemarksRejectionMessage, UserAnswers}
import pages.QuestionPage
import uk.gov.hmrc.http.HeaderCarrier
import utils.{Date, IntValue}

import scala.concurrent.{ExecutionContext, Future}

class UnloadingRemarksRejectionService @Inject() (connector: UnloadingConnector)(implicit ec: ExecutionContext) {

  def unloadingRemarksRejectionMessage(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Option[UnloadingRemarksRejectionMessage]] =
    connector.getSummary(arrivalId) flatMap {
      case Some(summary) =>
        summary.messagesLocation.unloadingRemarksRejection match {
          case Some(rejectionLocation) =>
            connector.getRejectionMessage(rejectionLocation)
          case _ =>
            Future.successful(None)
        }
      case _ => Future.successful(None)
    }

  def getRejectedValueAsString(arrivalId: ArrivalId, userAnswers: Option[UserAnswers])(
    page: QuestionPage[String]
  )(implicit hc: HeaderCarrier): Future[Option[String]] =
    userAnswers match {
      case Some(userAnswers: UserAnswers) if userAnswers.get(page).isDefined => Future.successful(userAnswers.get(page))
      case _                                                                 => getRejectedValue(arrivalId)
    }

  def getRejectedValueAsInt(arrivalId: ArrivalId, userAnswers: Option[UserAnswers])(page: QuestionPage[Int])(implicit hc: HeaderCarrier): Future[Option[Int]] =
    userAnswers match {
      case Some(userAnswers: UserAnswers) if userAnswers.get(page).isDefined => Future.successful(userAnswers.get(page))
      case _                                                                 => getRejectedValue(arrivalId).map(_.flatMap(IntValue.getInt))
    }

  def getRejectedValueAsDate(arrivalId: ArrivalId, userAnswers: Option[UserAnswers])(
    page: QuestionPage[LocalDate]
  )(implicit hc: HeaderCarrier): Future[Option[LocalDate]] =
    userAnswers match {
      case Some(userAnswers: UserAnswers) if userAnswers.get(page).isDefined => Future.successful(userAnswers.get(page))
      case _                                                                 => getRejectedValue(arrivalId).map(_.flatMap(Date.getDate))
    }

  private def getRejectedValue(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Option[String]] =
    unloadingRemarksRejectionMessage(arrivalId) map {
      case Some(rejectionMessage) if rejectionMessage.errors.length == 1 =>
        rejectionMessage.errors.head.originalAttributeValue
      case _ => None
    }
}
