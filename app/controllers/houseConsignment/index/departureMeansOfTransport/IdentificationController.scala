/*
 * Copyright 2024 HM Revenue & Customs
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
import forms.EnumerableFormProvider
import models.reference.TransportMeansIdentification
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.DepartureTransportMeansNavigator
import pages.houseConsignment.index.departureMeansOfTransport.TransportMeansIdentificationPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.MeansOfTransportIdentificationTypesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.departureTransportMeans.IdentificationViewModel.IdentificationViewModelProvider
import views.html.houseConsignment.index.departureMeansOfTransport.IdentificationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentificationController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: EnumerableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IdentificationView,
  navigator: DepartureTransportMeansNavigator,
  service: MeansOfTransportIdentificationTypesService,
  identificationViewModelProvider: IdentificationViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(mode: Mode, identificationTypes: Seq[TransportMeansIdentification]): Form[TransportMeansIdentification] =
    formProvider[TransportMeansIdentification](mode, "houseConsignment.index.departureMeansOfTransport.identification", identificationTypes)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, transportMeansIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        service.getMeansOfTransportIdentificationTypes(request.userAnswers).flatMap {
          identifiers =>
            val viewModel = identificationViewModelProvider.apply(mode)
            val preparedForm = request.userAnswers.get(TransportMeansIdentificationPage(houseConsignmentIndex, transportMeansIndex)) match {
              case None        => form(mode, identifiers)
              case Some(value) => form(mode, identifiers).fill(value)
            }
            Future.successful(
              Ok(
                view(preparedForm, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, transportMeansIndex, identifiers, mode, viewModel)
              )
            )
        }
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, transportMeansIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        service.getMeansOfTransportIdentificationTypes(request.userAnswers).flatMap {
          identifiers =>
            val viewModel = identificationViewModelProvider.apply(mode)
            form(mode, identifiers)
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(
                      view(formWithErrors, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, transportMeansIndex, identifiers, mode, viewModel)
                    )
                  ),
                value => redirect(value, houseConsignmentIndex, transportMeansIndex, mode)
              )
        }
    }

  private def redirect(
    value: TransportMeansIdentification,
    houseConsignmentIndex: Index,
    transportMeansIndex: Index,
    mode: Mode
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(TransportMeansIdentificationPage(houseConsignmentIndex, transportMeansIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(TransportMeansIdentificationPage(houseConsignmentIndex, transportMeansIndex), mode, request.userAnswers))
}
