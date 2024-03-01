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

package controllers.transportEquipment.index.seals

import controllers.actions._
import forms.YesNoFormProvider
import models.requests.SpecificDataRequestProvider1
import models.{ArrivalId, Index, Mode}
import pages.sections.SealSection
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transportEquipment.index.seals.RemoveSealYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveSealYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveSealYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private type Request = SpecificDataRequestProvider1[String]#SpecificDataRequest[_]

  private def form(equipmentIndex: Index)(implicit request: Request): Form[Boolean] =
    formProvider("transportEquipment.index.seal.removeSealYesNo", equipmentIndex.display, request.arg)

  private def addAnother(arrivalId: ArrivalId, mode: Mode, equipmentIndex: Index): Call =
    controllers.transportEquipment.index.routes.AddAnotherSealController.onPageLoad(arrivalId, mode, equipmentIndex)

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, equipmentIndex: Index, sealIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, SealSection(equipmentIndex, sealIndex), addAnother(arrivalId, mode, equipmentIndex))
    .andThen(getMandatoryPage(SealIdentificationNumberPage(equipmentIndex, sealIndex))) {
      implicit request =>
        Ok(view(form(equipmentIndex), request.userAnswers.mrn, arrivalId, mode, equipmentIndex, sealIndex, request.arg))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, equipmentIndex: Index, sealIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, SealSection(equipmentIndex, sealIndex), addAnother(arrivalId, mode, equipmentIndex))
    .andThen(getMandatoryPage(SealIdentificationNumberPage(equipmentIndex, sealIndex)))
    .async {
      implicit request =>
        form(equipmentIndex)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, mode, equipmentIndex, sealIndex, request.arg))),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.remove(SealSection(equipmentIndex, sealIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(arrivalId, mode, equipmentIndex))
          )
    }
}
