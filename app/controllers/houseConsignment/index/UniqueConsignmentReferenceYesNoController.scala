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

package controllers.houseConsignment.index

import controllers.actions.*
import forms.YesNoFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.HouseConsignmentNavigator
import pages.houseConsignment.index.UniqueConsignmentReferenceYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.UniqueConsignmentReferenceYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UniqueConsignmentReferenceYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: HouseConsignmentNavigator,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: UniqueConsignmentReferenceYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix = "houseConsignment.index.uniqueConsignmentReferenceYesNo"

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requirePhase6(arrivalId) {
      implicit request =>
        val form = formProvider(prefix, houseConsignmentIndex)
        val preparedForm =
          request.userAnswers.get(UniqueConsignmentReferenceYesNoPage(houseConsignmentIndex)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, houseConsignmentIndex))
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requirePhase6(arrivalId).async {
      implicit request =>
        val form = formProvider(prefix, houseConsignmentIndex)
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, houseConsignmentIndex))
              ),
            value =>
              for {
                updatedAnswers <- Future
                  .fromTry(
                    request.userAnswers.set(UniqueConsignmentReferenceYesNoPage(houseConsignmentIndex), value)
                  )
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(
                navigator.nextPage(UniqueConsignmentReferenceYesNoPage(houseConsignmentIndex), mode, updatedAnswers)
              )
          )
    }
}
