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

package controllers

import config.FrontendAppConfig
import controllers.actions._
import handlers.ErrorHandler
import logging.Logging
import models._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.UnloadingRemarksRejectionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.UnloadingRemarksRejectionViewModel
import viewModels.sections.Section
import views.html.{UnloadingRemarksMultipleErrorsRejectionView, UnloadingRemarksRejectionView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnloadingRemarksRejectionController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  val appConfig: FrontendAppConfig,
  service: UnloadingRemarksRejectionService,
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
      service.unloadingRemarksRejectionMessage(arrivalId) flatMap {
        case Some(rejectionMessage) =>
          errorView(arrivalId, rejectionMessage.errors) match {
            case Some(result) =>
              Future.successful(result)
            case _ =>
              logger.debug(s"Couldn't build a UnloadingRemarksRejectionViewModel for arrival: $arrivalId")
              errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
          }
        case _ =>
          logger.error(s"Failed to pull back a rejection message for arrival: $arrivalId")
          errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
      }
  }

  private def errorView(
    arrivalId: ArrivalId,
    errors: Seq[FunctionalError]
  )(implicit request: Request[_]): Option[Result] = {
    val result: Option[Result] = errors match {
      case Nil          => None
      case error :: Nil => singleError(arrivalId, error)
      case _            => multipleErrors(arrivalId, errors)
    }

    result.orElse(defaultError(arrivalId, errors.headOption))
  }

  private def singleError(
    arrivalId: ArrivalId,
    error: FunctionalError
  )(implicit request: Request[_]): Option[Result] =
    viewModel.apply(error, arrivalId) map {
      row =>
        Ok(singleErrorView(arrivalId, Section(row)))
    }

  private def multipleErrors(
    arrivalId: ArrivalId,
    errors: Seq[FunctionalError]
  )(implicit request: Request[_]): Option[Result] =
    Some(Ok(multipleErrorsView(arrivalId, errors)))

  private def defaultError(
    arrivalId: ArrivalId,
    error: Option[FunctionalError]
  )(implicit request: Request[_]): Option[Result] =
    error flatMap {
      case error @ FunctionalError(_, _: DefaultPointer, _, _) =>
        multipleErrors(arrivalId, Seq(error))
      case _ =>
        None
    }
}
