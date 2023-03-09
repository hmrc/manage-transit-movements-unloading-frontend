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

package controllers

import config.FrontendAppConfig
import controllers.actions._
import extractors.RejectionMessageExtractor
import handlers.ErrorHandler
import logging.Logging
import models._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import repositories.SessionRepository
import services.{DateTimeService, UnloadingRemarksRejectionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.UnloadingRemarksRejectionViewModel
import viewModels.sections.Section
import views.html.{UnloadingRemarksMultipleErrorsRejectionView, UnloadingRemarksRejectionView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class UnloadingRemarksRejectionController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  val appConfig: FrontendAppConfig,
  unloadingRemarksRejectionService: UnloadingRemarksRejectionService,
  dateTimeService: DateTimeService,
  extractor: RejectionMessageExtractor,
  sessionRepository: SessionRepository,
  errorHandler: ErrorHandler,
  singleErrorView: UnloadingRemarksRejectionView,
  multipleErrorsView: UnloadingRemarksMultipleErrorsRejectionView,
  viewModel: UnloadingRemarksRejectionViewModel
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = identify.async {
    implicit request =>
      unloadingRemarksRejectionService.unloadingRemarksRejectionMessage(arrivalId) flatMap {
        case Some(rejectionMessage) =>
          val userAnswers = UserAnswers(
            arrivalId,
            rejectionMessage.movementReferenceNumber,
            request.eoriNumber,
            Json.obj(),
            Json.obj(),
            dateTimeService.now
          ) // TODO remove this
          extractor.apply(userAnswers, rejectionMessage) match {
            case Success(updatedAnswers) =>
              sessionRepository.set(updatedAnswers) flatMap {
                _ =>
                  errorView(updatedAnswers, rejectionMessage.errors) match {
                    case Some(result) =>
                      Future.successful(result)
                    case _ =>
                      logger.debug(s"Couldn't build a UnloadingRemarksRejectionViewModel for arrival: $arrivalId")
                      errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
                  }
              }
            case Failure(exception) =>
              logger.error(s"Failed to extract unloading permission to user answers for arrival: $arrivalId, ${exception.getMessage}")
              errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
          }
        case _ =>
          logger.error(s"Failed to pull back a rejection message for arrival: $arrivalId")
          errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
      }
  }

  private def errorView(
    userAnswers: UserAnswers,
    errors: Seq[FunctionalError]
  )(implicit request: Request[_]): Option[Result] = {
    val result: Option[Result] = errors match {
      case Nil      => None
      case _ :: Nil => singleError(userAnswers)
      case _        => multipleErrors(userAnswers, errors)
    }

    result.orElse(defaultError(userAnswers, errors.headOption))
  }

  private def singleError(
    userAnswers: UserAnswers
  )(implicit request: Request[_]): Option[Result] =
    viewModel.apply(userAnswers) map {
      row =>
        Ok(singleErrorView(userAnswers.id, Section(row)))
    }

  private def multipleErrors(
    userAnswers: UserAnswers,
    errors: Seq[FunctionalError]
  )(implicit request: Request[_]): Option[Result] =
    Some(Ok(multipleErrorsView(userAnswers.id, errors)))

  private def defaultError(
    userAnswers: UserAnswers,
    error: Option[FunctionalError]
  )(implicit request: Request[_]): Option[Result] =
    error flatMap {
      case error @ FunctionalError(_, _: DefaultPointer, _, _) =>
        multipleErrors(userAnswers, Seq(error))
      case _ =>
        None
    }
}
