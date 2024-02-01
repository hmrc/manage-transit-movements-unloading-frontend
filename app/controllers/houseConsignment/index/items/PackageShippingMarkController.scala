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

package controllers.houseConsignment.index.items

import controllers.actions._
import forms.DescriptionFormProvider
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.Navigator
import pages.houseConsignment.index.items.PackageShippingMarkPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.items.PackageShippingMarkView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackageShippingMarkController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  navigator: Navigator,
  val controllerComponents: MessagesControllerComponents,
  formProvider: DescriptionFormProvider,
  view: PackageShippingMarkView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(houseConsignmentIndex: Index, itemIndex: Index, packageIndex: Index): Form[String] =
    formProvider("houseConsignment.index.item.packageShippingMark", itemIndex.display, houseConsignmentIndex.display, packageIndex.display)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, packageIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val preparedForm = request.userAnswers.get(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex)) match {
          case None        => form(houseConsignmentIndex, itemIndex, packageIndex)
          case Some(value) => form(houseConsignmentIndex, itemIndex, packageIndex).fill(value)
        }
        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode))
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, packageIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        form(houseConsignmentIndex, itemIndex, packageIndex)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode))),
            value => redirect(value, houseConsignmentIndex, itemIndex, packageIndex, mode)
          )
    }

  private def redirect(
    value: String,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    packageIndex: Index,
    mode: Mode
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), mode, request.userAnswers))
}
