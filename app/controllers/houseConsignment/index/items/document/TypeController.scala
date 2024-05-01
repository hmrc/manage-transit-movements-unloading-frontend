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

package controllers.houseConsignment.index.items.document

import config.FrontendAppConfig
import controllers.actions._
import forms.SelectableFormProvider
import models.reference.DocumentType
import models.requests.DataRequest
import models.{ArrivalId, HouseConsignmentLevelDocuments, Index, Mode}
import navigation.houseConsignment.index.items.DocumentNavigator.DocumentNavigatorProvider
import pages.houseConsignment.index.items.document.TypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.DocumentsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.items.document.TypeViewModel.TypeViewModelProvider
import views.html.houseConsignment.index.items.document.TypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TypeController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: DocumentNavigatorProvider,
  actions: Actions,
  formProvider: SelectableFormProvider,
  service: DocumentsService,
  val controllerComponents: MessagesControllerComponents,
  view: TypeView,
  viewModelProvider: TypeViewModelProvider
)(implicit ec: ExecutionContext, config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix: String = "houseConsignment.index.items.document.type"

  private def houseConsignmentLevelDocuments(houseConsignmentIndex: Index, itemIndex: Index, documentIndex: Index)(implicit
    request: DataRequest[AnyContent]
  ): HouseConsignmentLevelDocuments =
    HouseConsignmentLevelDocuments(request.userAnswers, houseConsignmentIndex, itemIndex, documentIndex)

  def onPageLoad(
    arrivalId: ArrivalId,
    houseConsignmentMode: Mode,
    itemMode: Mode,
    documentMode: Mode,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    documentIndex: Index
  ): Action[AnyContent] =
    actions
      .requireData(arrivalId)
      .async {
        implicit request =>
          service.getDocumentList(request.userAnswers, houseConsignmentIndex, itemIndex, documentIndex, documentMode).map {
            documentList =>
              val documents          = houseConsignmentLevelDocuments(houseConsignmentIndex, itemIndex, documentIndex)
              val viewModel          = viewModelProvider.apply(documentMode, documents, houseConsignmentIndex, itemIndex)
              val availableDocuments = documents.availableDocuments(documentList.values)
              val form               = formProvider(documentMode, prefix, documentList, houseConsignmentIndex.display, itemIndex.display)
              val preparedForm = request.userAnswers.get(TypePage(houseConsignmentIndex, itemIndex, documentIndex)) match {
                case None        => form
                case Some(value) => form.fill(value)
              }

              Ok(
                view(
                  preparedForm,
                  request.userAnswers.mrn,
                  arrivalId,
                  houseConsignmentMode,
                  itemMode,
                  documentMode,
                  availableDocuments,
                  viewModel,
                  houseConsignmentIndex,
                  itemIndex,
                  documentIndex
                )
              )
          }
      }

  def onSubmit(
    arrivalId: ArrivalId,
    houseConsignmentMode: Mode,
    itemMode: Mode,
    documentMode: Mode,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    documentIndex: Index
  ): Action[AnyContent] =
    actions
      .requireData(arrivalId)
      .async {
        implicit request =>
          service.getDocumentList(request.userAnswers, houseConsignmentIndex, itemIndex, documentIndex, documentMode).flatMap {
            documentList =>
              val documents          = houseConsignmentLevelDocuments(houseConsignmentIndex, itemIndex, documentIndex)
              val viewModel          = viewModelProvider.apply(documentMode, documents, houseConsignmentIndex, itemIndex)
              val availableDocuments = documents.availableDocuments(documentList.values)
              val form               = formProvider(documentMode, prefix, documentList, houseConsignmentIndex.display, itemIndex.display)
              form
                .bindFromRequest()
                .fold(
                  formWithErrors =>
                    Future.successful(
                      BadRequest(
                        view(
                          formWithErrors,
                          request.userAnswers.mrn,
                          arrivalId,
                          houseConsignmentMode,
                          itemMode,
                          documentMode,
                          availableDocuments,
                          viewModel,
                          houseConsignmentIndex,
                          itemIndex,
                          documentIndex
                        )
                      )
                    ),
                  value => redirect(houseConsignmentMode, itemMode, documentMode, value, houseConsignmentIndex, itemIndex, documentIndex)
                )
          }
      }

  private def redirect(
    houseConsignmentMode: Mode,
    itemMode: Mode,
    documentMode: Mode,
    value: DocumentType,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    documentIndex: Index
  )(implicit request: DataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(TypePage(houseConsignmentIndex, itemIndex, documentIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield {
      val navigator = navigatorProvider.apply(houseConsignmentMode, itemMode)
      Redirect(navigator.nextPage(TypePage(houseConsignmentIndex, itemIndex, documentIndex), documentMode, updatedAnswers))
    }
}
