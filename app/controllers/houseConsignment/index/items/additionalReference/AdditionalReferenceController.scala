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

package controllers.houseConsignment.index.items.additionalReference

import controllers.actions._
import forms.SelectableFormProvider
import models.reference.AdditionalReferenceType
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.{AdditionalReferenceNavigator, Navigator}
import pages.houseConsignment.index.items.additionalReference.AdditionalReferencePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.AdditionalReferencesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.items.additionalReference.AdditionalReferenceViewModel.AdditionalReferenceViewModelProvider
import views.html.houseConsignment.index.items.additionalReference.AdditionalReferenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: SelectableFormProvider,
  service: AdditionalReferencesService,
  val controllerComponents: MessagesControllerComponents,
  view: AdditionalReferenceView,
  viewModelProvider: AdditionalReferenceViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix: String = "houseConsignment.index.items.additionalReference.additionalReferenceType"

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, itemIndex: Index, additionalReferenceIndex: Index): Action[AnyContent] =
    actions
      .requireData(arrivalId)
      .async {
        implicit request =>
          service.getAdditionalReferences().map {
            additionalReferences =>
              val form      = formProvider(mode, prefix, additionalReferences, houseConsignmentIndex.display, itemIndex.display)
              val viewModel = viewModelProvider.apply(arrivalId, mode, houseConsignmentIndex, itemIndex, additionalReferenceIndex)
              val preparedForm = request.userAnswers.get(AdditionalReferencePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)) match {
                case None        => form
                case Some(value) => form.fill(value)
              }

              Ok(
                view(preparedForm, request.userAnswers.mrn, additionalReferences.values, viewModel)
              )
          }
      }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, itemIndex: Index, additionalReferenceIndex: Index): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        service.getAdditionalReferences().flatMap {
          additionalReferences =>
            val form      = formProvider(mode, prefix, additionalReferences, houseConsignmentIndex.display, itemIndex.display)
            val viewModel = viewModelProvider.apply(arrivalId, mode, houseConsignmentIndex, itemIndex, additionalReferenceIndex)
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(
                      view(formWithErrors, request.userAnswers.mrn, additionalReferences.values, viewModel)
                    )
                  ),
                value => redirect(value, houseConsignmentIndex, itemIndex, additionalReferenceIndex, mode)
              )
        }
    }

  private def redirect(
    value: AdditionalReferenceType,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    additionalReferenceIndex: Index,
    mode: Mode
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(AdditionalReferencePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(AdditionalReferencePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), mode, request.userAnswers))
}
