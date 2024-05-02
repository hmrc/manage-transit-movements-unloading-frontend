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

package controllers.houseConsignment.index.additionalReference

import controllers.actions._
import forms.YesNoFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.AdditionalReferenceNavigator
import pages.houseConsignment.index.AddAdditionalReferenceYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.additionalReference.AddAdditionalReferenceYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddAdditionalReferenceYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: AdditionalReferenceNavigator,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAdditionalReferenceYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix = "houseConsignment.index.additionalReference.addAdditionalReferenceYesNo"

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index): Action[AnyContent] =
    actions.getStatus(arrivalId) {

      implicit request =>
        val form = formProvider(prefix, houseConsignmentIndex)
        val preparedForm =
          request.userAnswers.get(AddAdditionalReferenceYesNoPage(houseConsignmentIndex)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, mode))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index): Action[AnyContent] =
    actions.getStatus(arrivalId).async {
      implicit request =>
        val form = formProvider(prefix, houseConsignmentIndex)
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, mode))
              ),
            value =>
              for {
                updatedAnswers <- Future
                  .fromTry(
                    request.userAnswers.set(AddAdditionalReferenceYesNoPage(houseConsignmentIndex), value)
                  )
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(
                navigator.nextPage(AddAdditionalReferenceYesNoPage(houseConsignmentIndex), mode, updatedAnswers)
              )
          )
    }
}
