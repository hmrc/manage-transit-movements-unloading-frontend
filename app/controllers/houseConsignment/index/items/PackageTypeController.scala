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
import forms.SelectableFormProvider
import models.reference.PackageType
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.items.ConsignmentItemNavigator
import pages.PackageTypePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.PackagesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.items.PackageTypeViewModel.PackageTypeViewModelProvider
import views.html.houseConsignment.index.items.PackageTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackageTypeController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  navigator: ConsignmentItemNavigator,
  val controllerComponents: MessagesControllerComponents,
  formProvider: SelectableFormProvider,
  service: PackagesService,
  view: PackageTypeView,
  viewModelProvider: PackageTypeViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix: String = "houseConsignment.index.item.packageType"

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, packageIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        service.getPackageTypes().map {
          packageTypeList =>
            val form      = formProvider(mode, prefix, packageTypeList, houseConsignmentIndex.display, itemIndex.display)
            val viewModel = viewModelProvider.apply(houseConsignmentIndex, itemIndex, mode)
            val preparedForm = request.userAnswers.get(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex)) match {
              case None        => form
              case Some(value) => form.fill(value)
            }
            Ok(
              view(viewModel,
                   preparedForm,
                   request.userAnswers.mrn,
                   request.userAnswers.id,
                   packageTypeList.values,
                   mode,
                   houseConsignmentIndex,
                   itemIndex,
                   packageIndex
              )
            )
        }
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, packageIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        service.getPackageTypes().flatMap {
          packagesTypeList =>
            val viewModel               = viewModelProvider.apply(houseConsignmentIndex, itemIndex, mode)
            val form: Form[PackageType] = formProvider(mode, prefix, packagesTypeList, houseConsignmentIndex.display, itemIndex.display)
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(
                      view(viewModel,
                           formWithErrors,
                           request.userAnswers.mrn,
                           request.userAnswers.id,
                           packagesTypeList.values,
                           mode,
                           houseConsignmentIndex,
                           itemIndex,
                           packageIndex
                      )
                    )
                  ),
                value => redirect(value, houseConsignmentIndex, itemIndex, packageIndex, mode)
              )

        }
    }

  private def redirect(
    value: PackageType,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    packageIndex: Index,
    mode: Mode
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), mode, request.userAnswers))
}
