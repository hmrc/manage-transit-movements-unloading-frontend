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
import controllers.departureMeansOfTransport.routes
import forms.YesNoFormProvider
import models.removable.TransportMeans
import models.{ArrivalId, Index, Mode, UserAnswers}
import pages.sections.houseConsignment.index.departureTransportMeans.TransportMeansSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.departureMeansOfTransport.RemoveDepartureMeansOfTransportYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveDepartureMeansOfTransportYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveDepartureMeansOfTransportYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def form(transportMeansIndex: Index): Form[Boolean] =
    formProvider("houseConsignment.index.departureMeansOfTransport.removeDepartureMeansOfTransportYesNo", transportMeansIndex)

  private def addAnother(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index): Call =
    routes.AddAnotherDepartureMeansOfTransportController.onPageLoad(arrivalId, mode) //TODO: Needs updating with house consignment level controller

  def insetText(userAnswers: UserAnswers, houseConsignmentIndex: Index, transportMeansIndex: Index): Option[String] =
    TransportMeans(userAnswers, houseConsignmentIndex, transportMeansIndex).flatMap(_.forRemoveDisplay)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, transportMeansIndex: Index, mode: Mode): Action[AnyContent] = actions
    .requireIndex(arrivalId, TransportMeansSection(houseConsignmentIndex, transportMeansIndex), addAnother(arrivalId, mode, houseConsignmentIndex)) {
      implicit request =>
        Ok(
          view(
            form(transportMeansIndex),
            request.userAnswers.mrn,
            arrivalId,
            mode,
            houseConsignmentIndex,
            transportMeansIndex,
            insetText(request.userAnswers, houseConsignmentIndex, transportMeansIndex)
          )
        )
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, transportMeansIndex: Index, mode: Mode): Action[AnyContent] = actions
    .requireIndex(arrivalId, TransportMeansSection(houseConsignmentIndex, transportMeansIndex), addAnother(arrivalId, mode, houseConsignmentIndex))
    .async {
      implicit request =>
        form(transportMeansIndex)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future
                .successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      request.userAnswers.mrn,
                      arrivalId,
                      mode,
                      houseConsignmentIndex,
                      transportMeansIndex,
                      insetText(request.userAnswers, houseConsignmentIndex, transportMeansIndex)
                    )
                  )
                ),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.removeDataGroup(TransportMeansSection(houseConsignmentIndex, transportMeansIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(arrivalId, mode, houseConsignmentIndex))
          )
    }
}
