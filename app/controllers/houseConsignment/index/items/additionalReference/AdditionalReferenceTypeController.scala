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

import controllers.actions.*
import forms.SelectableFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.items.AdditionalReferenceNavigator.AdditionalReferenceNavigatorProvider
import pages.houseConsignment.index.items.additionalReference.{AdditionalReferenceInCL234Page, AdditionalReferenceTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.items.additionalReference.AdditionalReferenceTypeViewModel.AdditionalReferenceTypeViewModelProvider
import views.html.houseConsignment.index.items.additionalReference.AdditionalReferenceTypeView

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

  private val prefix: String = "houseConsignment.index.items.additionalReference.additionalReferenceType"

  def onPageLoad(
    arrivalId: ArrivalId,
    houseConsignmentMode: Mode,
    itemMode: Mode,
    additionalReferenceMode: Mode,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    additionalReferenceIndex: Index
  ): Action[AnyContent] =
    actions
      .requireData(arrivalId)
      .async {
        implicit request =>
          referenceDataService.getAdditionalReferences().map {
            additionalReferences =>
              val form = formProvider(additionalReferenceMode, prefix, additionalReferences, houseConsignmentIndex.display, itemIndex.display)
              val viewModel = viewModelProvider
                .apply(arrivalId, houseConsignmentMode, itemMode, additionalReferenceMode, houseConsignmentIndex, itemIndex, additionalReferenceIndex)
              val preparedForm = request.userAnswers.get(AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)) match {
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
    itemMode: Mode,
    additionalReferenceMode: Mode,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    additionalReferenceIndex: Index
  ): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        referenceDataService.getAdditionalReferences().flatMap {
          additionalReferences =>
            val form = formProvider(additionalReferenceMode, prefix, additionalReferences, houseConsignmentIndex.display, itemIndex.display)
            val viewModel = viewModelProvider
              .apply(arrivalId, houseConsignmentMode, itemMode, additionalReferenceMode, houseConsignmentIndex, itemIndex, additionalReferenceIndex)
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(
                      view(formWithErrors, request.userAnswers.mrn, additionalReferences.values, viewModel)
                    )
                  ),
                value =>
                  for {
                    isInCL234 <- referenceDataService.isDocumentTypeExcise(value.documentType)
                    updatedAnswers <- Future.fromTry(
                      request.userAnswers
                        .set(AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), value)
                        .flatMap(_.set(AdditionalReferenceInCL234Page(houseConsignmentIndex, itemIndex, additionalReferenceIndex), isInCL234))
                    )
                    _ <- sessionRepository.set(updatedAnswers)
                  } yield {
                    val navigator = navigatorProvider.apply(houseConsignmentMode, itemMode)
                    Redirect(
                      navigator.nextPage(
                        AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex),
                        additionalReferenceMode,
                        request.userAnswers
                      )
                    )
                  }
              )
        }
    }
}
