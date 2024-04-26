/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.houseConsignment.index.documents

import config.FrontendAppConfig
import controllers.actions._
import forms.SelectableFormProvider
import models.reference.DocumentType
import models.requests.DataRequest
import models.{ArrivalId, HouseConsignmentLevelDocuments, Index, Mode}
import navigation.Navigator
import pages.houseConsignment.index.documents.TypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.DocumentsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.documents.TypeViewModel.TypeViewModelProvider
import views.html.houseConsignment.index.documents.TypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TypeController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: SelectableFormProvider,
  service: DocumentsService,
  val controllerComponents: MessagesControllerComponents,
  view: TypeView,
  viewModelProvider: TypeViewModelProvider
)(implicit ec: ExecutionContext, config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix: String = "houseConsignment.index.document.type"

  private def houseConsignmentLevelDocuments(houseConsigmentIndex: Index, documentIndex: Index)(implicit
    request: DataRequest[AnyContent]
  ): HouseConsignmentLevelDocuments =
    HouseConsignmentLevelDocuments(request.userAnswers, houseConsigmentIndex, documentIndex)

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, documentIndex: Index): Action[AnyContent] = actions
    .requireData(arrivalId)
    .async {
      implicit request =>
        service.getDocumentList(request.userAnswers, houseConsignmentIndex, documentIndex, mode).map {
          documentList =>
            val documents          = houseConsignmentLevelDocuments(houseConsignmentIndex, documentIndex)
            val viewModel          = viewModelProvider.apply(mode, documents, houseConsignmentIndex)
            val availableDocuments = documents.availableDocuments(documentList.values)
            val form               = formProvider(mode, prefix, documentList, houseConsignmentIndex.display)
            val preparedForm = request.userAnswers.get(TypePage(houseConsignmentIndex, documentIndex)) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, mode, availableDocuments, viewModel, houseConsignmentIndex, documentIndex))
        }
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, documentIndex: Index): Action[AnyContent] = actions
    .requireData(arrivalId)
    .async {
      implicit request =>
        service.getDocumentList(request.userAnswers, houseConsignmentIndex, documentIndex, mode).flatMap {
          documentList =>
            val documents          = houseConsignmentLevelDocuments(houseConsignmentIndex, documentIndex)
            val viewModel          = viewModelProvider.apply(mode, documents, houseConsignmentIndex)
            val availableDocuments = documents.availableDocuments(documentList.values)
            val form               = formProvider(mode, prefix, documentList, houseConsignmentIndex.display)
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(
                      view(formWithErrors, request.userAnswers.mrn, arrivalId, mode, availableDocuments, viewModel, houseConsignmentIndex, documentIndex)
                    )
                  ),
                value => redirect(mode, value, houseConsignmentIndex, documentIndex)
              )
        }
    }

  private def redirect(
    mode: Mode,
    value: DocumentType,
    houseConsignmentIndex: Index,
    documentIndex: Index
  )(implicit request: DataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(TypePage(houseConsignmentIndex, documentIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(TypePage(houseConsignmentIndex, documentIndex), mode, updatedAnswers))
}
