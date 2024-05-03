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

package controllers.houseConsignment.index.departureMeansOfTransport

import controllers.actions._
import forms.VehicleIdentificationNumberFormProvider
import models.requests.DataRequest
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.departureMeansOfTransport.DepartureTransportMeansNavigator.DepartureTransportMeansNavigatorProvider
import pages.houseConsignment.index.departureMeansOfTransport.VehicleIdentificationNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.departureTransportMeans.IdentificationNumberViewModel.IdentificationNumberViewModelProvider
import views.html.houseConsignment.index.departureMeansOfTransport.IdentificationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentificationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: VehicleIdentificationNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  navigatorProvider: DepartureTransportMeansNavigatorProvider,
  view: IdentificationNumberView,
  identificationNumberViewModelProvider: IdentificationNumberViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val prefix = "houseConsignment.index.departureMeansOfTransport.identificationNumber"

  def onPageLoad(
    arrivalId: ArrivalId,
    houseConsignmentIndex: Index,
    transportMeansIndex: Index,
    houseConsignmentMode: Mode,
    transportMeansMode: Mode
  ): Action[AnyContent] =
    actions.getStatus(arrivalId) {
      implicit request =>
        val viewModel = identificationNumberViewModelProvider.apply(transportMeansMode, houseConsignmentIndex)
        val form      = formProvider(prefix, transportMeansMode, houseConsignmentIndex.display)

        val preparedForm = request.userAnswers.get(VehicleIdentificationNumberPage(houseConsignmentIndex, transportMeansIndex)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(
          view(preparedForm,
               request.userAnswers.mrn,
               arrivalId,
               houseConsignmentIndex,
               transportMeansIndex,
               houseConsignmentMode,
               transportMeansMode,
               viewModel
          )
        )
    }

  def onSubmit(
    arrivalId: ArrivalId,
    houseConsignmentIndex: Index,
    transportMeansIndex: Index,
    houseConsignmentMode: Mode,
    transportMeansMode: Mode
  ): Action[AnyContent] =
    actions.getStatus(arrivalId).async {
      implicit request =>
        val viewModel = identificationNumberViewModelProvider.apply(transportMeansMode, houseConsignmentIndex)
        val form      = formProvider(prefix, transportMeansMode, houseConsignmentIndex.display)
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future
                .successful(
                  BadRequest(
                    view(formWithErrors,
                         request.userAnswers.mrn,
                         arrivalId,
                         houseConsignmentIndex,
                         transportMeansIndex,
                         houseConsignmentMode,
                         transportMeansMode,
                         viewModel
                    )
                  )
                ),
            value => redirect(houseConsignmentIndex, transportMeansIndex, houseConsignmentMode, transportMeansMode, value)
          )
    }

  private def redirect(
    houseConsignmentIndex: Index,
    transportMeansIndex: Index,
    houseConsignmentMode: Mode,
    transportMeansMode: Mode,
    value: String
  )(implicit request: DataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(VehicleIdentificationNumberPage(houseConsignmentIndex, transportMeansIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield {
      val navigator = navigatorProvider.apply(houseConsignmentMode)
      Redirect(navigator.nextPage(VehicleIdentificationNumberPage(houseConsignmentIndex, transportMeansIndex), transportMeansMode, request.userAnswers))
    }
}
