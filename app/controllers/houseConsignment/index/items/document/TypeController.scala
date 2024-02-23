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

import controllers.actions._
import forms.SelectableFormProvider
import models.reference.DocumentType
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.Navigator
import pages.houseConsignment.index.items.document.TypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.DocumentsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.items.document.TypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TypeController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: SelectableFormProvider,
  service: DocumentsService,
  val controllerComponents: MessagesControllerComponents,
  view: TypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix: String = "houseConsignment.index.items.document.type"

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, itemIndex: Index, documentIndex: Index): Action[AnyContent] = actions
    .requireData(arrivalId)
    .async {
      implicit request =>
        service.getDocumentList(request.userAnswers, houseConsignmentIndex, itemIndex, documentIndex).map {
          documentList =>
            val form = formProvider(mode, prefix, documentList, houseConsignmentIndex.display, itemIndex.display)
            val preparedForm = request.userAnswers.get(TypePage(houseConsignmentIndex, itemIndex, documentIndex)) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, mode, documentList.values, houseConsignmentIndex, itemIndex, documentIndex))
        }
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, itemIndex: Index, documentIndex: Index): Action[AnyContent] = actions
    .requireData(arrivalId)
    .async {
      implicit request =>
        service.getDocumentList(request.userAnswers, houseConsignmentIndex, itemIndex, documentIndex).flatMap {
          documentList =>
            val form = formProvider(mode, prefix, documentList, houseConsignmentIndex.display, itemIndex.display)
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(
                      view(formWithErrors, request.userAnswers.mrn, arrivalId, mode, documentList.values, houseConsignmentIndex, itemIndex, documentIndex)
                    )
                  ),
                value => redirect(mode, value, houseConsignmentIndex, itemIndex, documentIndex)
              )
        }
    }

  private def redirect(
    mode: Mode,
    value: DocumentType,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    documentIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(TypePage(houseConsignmentIndex, itemIndex, documentIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(TypePage(houseConsignmentIndex, itemIndex, documentIndex), mode, request.userAnswers))
}
