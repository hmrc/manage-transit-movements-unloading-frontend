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

package controllers.houseConsignment.index.items.packages

import controllers.actions._
import forms.YesNoFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.items.PackagesNavigator.PackagesNavigatorProvider
import pages.houseConsignment.index.items.packages.AddPackageShippingMarkYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.items.packages.AddPackageShippingMarkYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddPackageShippingMarkYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: PackagesNavigatorProvider,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddPackageShippingMarkYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("houseConsignment.index.items.packages.addPackageShippingMarkYesNo")

  def onPageLoad(
    arrivalId: ArrivalId,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    packageIndex: Index,
    houseConsignmentMode: Mode,
    itemMode: Mode,
    packageMode: Mode
  ): Action[AnyContent] =
    actions.getStatus(arrivalId) {
      implicit request =>
        val preparedForm = request.userAnswers.get(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode))
    }

  def onSubmit(
    arrivalId: ArrivalId,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    packageIndex: Index,
    houseConsignmentMode: Mode,
    itemMode: Mode,
    packageMode: Mode
  ): Action[AnyContent] =
    actions.getStatus(arrivalId).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(
                  view(formWithErrors,
                       request.userAnswers.mrn,
                       arrivalId,
                       houseConsignmentIndex,
                       itemIndex,
                       packageIndex,
                       houseConsignmentMode,
                       itemMode,
                       packageMode
                  )
                )
              ),
            value =>
              for {
                updatedAnswers <- Future
                  .fromTry(request.userAnswers.set(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield {
                val navigator = navigatorProvider.apply(houseConsignmentMode, itemMode)
                Redirect(
                  navigator.nextPage(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, updatedAnswers)
                )
              }
          )
    }
}
