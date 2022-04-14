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
import javax.inject.Inject
import logging.Logging
import models._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.UnloadingRemarksRejectionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.SummaryList.Row
import utils.Date.getDate
import utils.UnloadingRemarksRejectionHelper
import viewModels.sections.Section
import views.html.{UnloadingRemarksMultipleErrorsRejectionView, UnloadingRemarksRejectionView}

import scala.concurrent.{ExecutionContext, Future}

class UnloadingRemarksRejectionController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  val appConfig: FrontendAppConfig,
  service: UnloadingRemarksRejectionService,
  errorHandler: ErrorHandler,
  singleErrorView: UnloadingRemarksRejectionView,
  multipleErrorView: UnloadingRemarksMultipleErrorsRejectionView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = identify.async {
    implicit request =>
      service.unloadingRemarksRejectionMessage(arrivalId) flatMap {
        case Some(rejectionMessage) =>
          displayErrorsView(arrivalId, rejectionMessage.errors) match {
            case Some(displayView) => Future.successful(displayView)
            case _ =>
              logger.debug(s"Couldn't build a UnloadingRemarksRejectionViewModel for arrival: $arrivalId")
              errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
          }
        case _ =>
          logger.error(s"Failed to pull back a rejection message for arrival: $arrivalId")
          errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
      }
  }

  private def displayErrorsView(arrivalId: ArrivalId, errors: Seq[FunctionalError])(implicit request: Request[_]): Option[Result] = {
    val viewModel: Option[Result] = errors match {
      case error if errors.length == 1 =>
        singleErrorPage(arrivalId, error.head)
      case `errors` if errors.length > 1 =>
        Some(Ok(multipleErrorView(arrivalId, errors)))
      case _ => None
    }

    viewModel.orElse(defaultErrorPage(arrivalId, errors.headOption))
  }

  private def singleErrorPage(arrivalId: ArrivalId, error: FunctionalError)(implicit request: Request[_]): Option[Result] = {
    val rowOption: Option[Row] = error.originalAttributeValue flatMap {
      originalValue =>
        val cyaHelper = new UnloadingRemarksRejectionHelper()
        error.pointer match {
          case NumberOfPackagesPointer    => Some(cyaHelper.totalNumberOfPackages(arrivalId, originalValue))
          case VehicleRegistrationPointer => Some(cyaHelper.vehicleNameRegistrationReference(arrivalId, originalValue))
          case NumberOfItemsPointer       => Some(cyaHelper.totalNumberOfItems(arrivalId, originalValue))
          case GrossMassPointer           => Some(cyaHelper.grossMassAmount(arrivalId, originalValue))
          case UnloadingDatePointer =>
            getDate(originalValue) map (
              date => cyaHelper.unloadingDate(arrivalId, date)
            )
          case DefaultPointer(_) => None
        }
    }
    rowOption map {
      row =>
        Ok(singleErrorView(arrivalId, Seq(Section(Seq(row)))))
    }
  }

  private def defaultErrorPage(arrivalId: ArrivalId, error: Option[FunctionalError])(implicit request: Request[_]): Option[Result] =
    error.flatMap(
      functionalError =>
        functionalError.pointer match {
          case DefaultPointer(_) => Some(Ok(multipleErrorView(arrivalId, Seq(functionalError))))
          case _                 => None
        }
    )

}
