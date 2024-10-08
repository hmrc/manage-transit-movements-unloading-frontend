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

package controllers.departureMeansOfTransport

import controllers.actions._
import forms.EnumerableFormProvider
import models.reference.TransportMeansIdentification
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.DepartureTransportMeansNavigator
import pages.departureMeansOfTransport.TransportMeansIdentificationPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.MeansOfTransportIdentificationTypesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.departureTransportMeans.IdentificationViewModel.IdentificationViewModelProvider
import views.html.departureMeansOfTransport.IdentificationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentificationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
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
    formProvider[TransportMeansIdentification](mode, "departureMeansOfTransport.identification", identificationTypes)

  def onPageLoad(arrivalId: ArrivalId, transportMeansIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        service.getMeansOfTransportIdentificationTypes(request.userAnswers).flatMap {
          identifiers =>
            val viewModel = identificationViewModelProvider.apply(mode)
            val preparedForm = request.userAnswers.get(TransportMeansIdentificationPage(transportMeansIndex)) match {
              case None        => form(mode, identifiers)
              case Some(value) => form(mode, identifiers).fill(value)
            }
            Future.successful(Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, transportMeansIndex, identifiers, mode, viewModel)))
        }
    }

  def onSubmit(arrivalId: ArrivalId, transportMeansIndex: Index, mode: Mode): Action[AnyContent] =
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
                    BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, transportMeansIndex, identifiers, mode, viewModel))
                  ),
                value => redirect(value, transportMeansIndex, mode)
              )
        }
    }

  private def redirect(
    value: TransportMeansIdentification,
    transportMeansIndex: Index,
    mode: Mode
  )(implicit request: MandatoryDataRequest[?]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(TransportMeansIdentificationPage(transportMeansIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(TransportMeansIdentificationPage(transportMeansIndex), mode, request.userAnswers))
}
