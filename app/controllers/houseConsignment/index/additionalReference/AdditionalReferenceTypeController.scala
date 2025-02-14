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

import controllers.actions.*
import forms.SelectableFormProvider
import models.reference.AdditionalReferenceType
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.AdditionalReferenceNavigator.AdditionalReferenceNavigatorProvider
import pages.houseConsignment.index.additionalReference.HouseConsignmentAdditionalReferenceTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.additionalReference.AdditionalReferenceTypeViewModel.AdditionalReferenceTypeViewModelProvider
import views.html.houseConsignment.index.additionalReference.AdditionalReferenceTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalReferenceTypeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: AdditionalReferenceNavigatorProvider,
  actions: Actions,
  formProvider: SelectableFormProvider,
  referenceDataService: ReferenceDataService,
  val controllerComponents: MessagesControllerComponents,
  view: AdditionalReferenceTypeView,
  viewModelProvider: AdditionalReferenceTypeViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix: String = "houseConsignment.index.additionalReference.additionalReferenceType"

  def onPageLoad(
    arrivalId: ArrivalId,
    houseConsignmentMode: Mode,
    additionalReferenceMode: Mode,
    houseConsignmentIndex: Index,
    additionalReferenceIndex: Index
  ): Action[AnyContent] =
    actions
      .requireData(arrivalId)
      .async {
        implicit request =>
          referenceDataService.getAdditionalReferences().map {
            additionalReferences =>
              val form      = formProvider(additionalReferenceMode, prefix, additionalReferences, houseConsignmentIndex.display)
              val viewModel = viewModelProvider.apply(arrivalId, houseConsignmentMode, additionalReferenceMode, houseConsignmentIndex, additionalReferenceIndex)
              val preparedForm = request.userAnswers.get(HouseConsignmentAdditionalReferenceTypePage(houseConsignmentIndex, additionalReferenceIndex)) match {
                case None        => form
                case Some(value) => form.fill(value)
              }

              Ok(
                view(preparedForm, request.userAnswers.mrn, additionalReferences.values, viewModel)
              )
          }
      }

  def onSubmit(
    arrivalId: ArrivalId,
    houseConsignmentMode: Mode,
    additionalReferenceMode: Mode,
    houseConsignmentIndex: Index,
    additionalReferenceIndex: Index
  ): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        referenceDataService.getAdditionalReferences().flatMap {
          additionalReferences =>
            val form      = formProvider(additionalReferenceMode, prefix, additionalReferences, houseConsignmentIndex.display)
            val viewModel = viewModelProvider.apply(arrivalId, houseConsignmentMode, additionalReferenceMode, houseConsignmentIndex, additionalReferenceIndex)
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(
                      view(formWithErrors, request.userAnswers.mrn, additionalReferences.values, viewModel)
                    )
                  ),
                value => redirect(value, houseConsignmentIndex, additionalReferenceIndex, houseConsignmentMode, additionalReferenceMode)
              )
        }
    }

  private def redirect(
    value: AdditionalReferenceType,
    houseConsignmentIndex: Index,
    additionalReferenceIndex: Index,
    houseConsignmentMode: Mode,
    additionalReferenceMode: Mode
  )(implicit request: MandatoryDataRequest[?]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(
        request.userAnswers.set(HouseConsignmentAdditionalReferenceTypePage(houseConsignmentIndex, additionalReferenceIndex), value)
      )
      _ <- sessionRepository.set(updatedAnswers)
    } yield {
      val navigator = navigatorProvider.apply(houseConsignmentMode)
      Redirect(
        navigator.nextPage(HouseConsignmentAdditionalReferenceTypePage(houseConsignmentIndex, additionalReferenceIndex),
                           additionalReferenceMode,
                           request.userAnswers
        )
      )
    }
}
