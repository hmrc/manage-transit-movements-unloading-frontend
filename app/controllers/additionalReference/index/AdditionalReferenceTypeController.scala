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

package controllers.additionalReference.index

import controllers.actions._
import forms.SelectableFormProvider
import models.reference.AdditionalReferenceType
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.AdditionalReferenceNavigator
import pages.additionalReference.AdditionalReferenceTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.AdditionalReferencesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.additionalReference.index.AdditionalReferenceTypeViewModel.AdditionalReferenceTypeViewModelProvider
import views.html.additionalReference.index.AdditionalReferenceTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalReferenceTypeController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: AdditionalReferenceNavigator,
  actions: Actions,
  formProvider: SelectableFormProvider,
  service: AdditionalReferencesService,
  val controllerComponents: MessagesControllerComponents,
  view: AdditionalReferenceTypeView,
  viewModelProvider: AdditionalReferenceTypeViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix: String = "additionalReference.index.additionalReferenceType"

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, additionalReferenceIndex: Index): Action[AnyContent] =
    actions
      .requireData(arrivalId)
      .async {
        implicit request =>
          service.getAdditionalReferences().map {
            additionalReferences =>
              val form      = formProvider(mode, prefix, additionalReferences)
              val viewModel = viewModelProvider.apply(arrivalId, mode, additionalReferenceIndex)
              val preparedForm = request.userAnswers.get(AdditionalReferenceTypePage(additionalReferenceIndex)) match {
                case None        => form
                case Some(value) => form.fill(value)
              }

              Ok(
                view(preparedForm, request.userAnswers.mrn, additionalReferences.values, viewModel)
              )
          }
      }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, additionalReferenceIndex: Index): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        service.getAdditionalReferences().flatMap {
          additionalReferences =>
            val form      = formProvider(mode, prefix, additionalReferences)
            val viewModel = viewModelProvider.apply(arrivalId, mode, additionalReferenceIndex)
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(
                      view(formWithErrors, request.userAnswers.mrn, additionalReferences.values, viewModel)
                    )
                  ),
                value => redirect(value, additionalReferenceIndex, mode)
              )
        }
    }

  private def redirect(
    value: AdditionalReferenceType,
    additionalReferenceIndex: Index,
    mode: Mode
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(AdditionalReferenceTypePage(additionalReferenceIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(AdditionalReferenceTypePage(additionalReferenceIndex), mode, request.userAnswers))
}
