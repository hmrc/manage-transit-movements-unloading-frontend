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

package controllers.transportEquipment.index

import controllers.actions._
import forms.YesNoFormProvider
import models.{ArrivalId, Index, Mode, TransportEquipment}
import pages.ContainerIdentificationNumberPage
import pages.sections.TransportEquipmentSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.http.HttpVerbs.GET
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RemoveTransportEquipmentYesNoView
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveTransportEquipmentYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveTransportEquipmentYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def form(transportEquipmentIndex: Index): Form[Boolean] =
    formProvider("transportEquipment.index.removeTransportEquipmentYesNo", transportEquipmentIndex.display)

  private def addAnother(arrivalId: ArrivalId, mode: Mode): Call =
    Call(GET, "#") //todo changes to AddAnotherTransportEquipmentController once implemented

  private def formatInsetText(containerId: Option[String]): Option[String] =
    TransportEquipment(containerId).asString

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, transportEquipmentIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, TransportEquipmentSection(transportEquipmentIndex), addAnother(arrivalId, mode)) {
      implicit request =>
        val containerId: Option[String] = request.userAnswers.get(ContainerIdentificationNumberPage(transportEquipmentIndex))
        val insetText                   = formatInsetText(containerId)
        Ok(view(form(transportEquipmentIndex), request.userAnswers.mrn, arrivalId, transportEquipmentIndex, insetText, mode))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, transportEquipmentIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, TransportEquipmentSection(transportEquipmentIndex), addAnother(arrivalId, mode))
    .async {
      implicit request =>
        val containerId: Option[String] = request.userAnswers.get(ContainerIdentificationNumberPage(transportEquipmentIndex))
        val insetText                   = formatInsetText(containerId)
        form(transportEquipmentIndex)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future
                .successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, transportEquipmentIndex, insetText, mode))),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.removeExceptSequenceNumber(TransportEquipmentSection(transportEquipmentIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(arrivalId, mode))
          )
    }
}
