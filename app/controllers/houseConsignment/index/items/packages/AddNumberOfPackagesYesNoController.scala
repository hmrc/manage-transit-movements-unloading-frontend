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
import navigation.DocumentNavigator
import pages.houseConsignment.index.items.packages.{AddNumberOfPackagesYesNoPage, PackageTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.items.packages.AddNumberOfPackagesYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddNumberOfPackagesYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: DocumentNavigator,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddNumberOfPackagesYesNoView,
  getMandatoryPage: SpecificDataRequiredActionProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("houseConsignment.index.items.packages.addNumberOfPackagesYesNo")

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, itemIndex: Index, packageIndex: Index): Action[AnyContent] =
    actions
      .getStatus(arrivalId)
      .andThen(getMandatoryPage(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex))) {
        implicit request =>
          val preparedForm = request.userAnswers.get(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, itemIndex, packageIndex, request.arg.toString, mode))
      }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, itemIndex: Index, packageIndex: Index): Action[AnyContent] =
    actions
      .getStatus(arrivalId)
      .andThen(getMandatoryPage(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex)))
      .async {
        implicit request =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(formWithErrors, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, itemIndex, packageIndex, request.arg.toString, mode)
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future
                    .fromTry(request.userAnswers.set(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), value))
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), mode, updatedAnswers))
            )
      }
}
