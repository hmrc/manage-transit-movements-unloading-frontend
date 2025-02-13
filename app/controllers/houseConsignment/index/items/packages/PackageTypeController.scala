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

import controllers.actions.*
import forms.SelectableFormProvider
import models.reference.PackageType
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.items.PackagesNavigator.PackagesNavigatorProvider
import pages.houseConsignment.index.items.packages.PackageTypePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.items.packages.PackageTypeViewModel.PackageTypeViewModelProvider
import views.html.houseConsignment.index.items.packages.PackageTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackageTypeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  navigatorProvider: PackagesNavigatorProvider,
  val controllerComponents: MessagesControllerComponents,
  formProvider: SelectableFormProvider,
  referenceDataService: ReferenceDataService,
  view: PackageTypeView,
  viewModelProvider: PackageTypeViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix: String = "houseConsignment.index.item.packageType"

  def onPageLoad(
    arrivalId: ArrivalId,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    packageIndex: Index,
    houseConsignmentMode: Mode,
    itemMode: Mode,
    packageMode: Mode
  ): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        referenceDataService.getPackageTypes().map {
          packageTypeList =>
            val form      = formProvider(packageMode, prefix, packageTypeList, houseConsignmentIndex.display, itemIndex.display)
            val viewModel = viewModelProvider.apply(houseConsignmentIndex, itemIndex, packageMode)
            val preparedForm = request.userAnswers.get(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex)) match {
              case None        => form
              case Some(value) => form.fill(value)
            }
            Ok(
              view(
                viewModel,
                preparedForm,
                request.userAnswers.mrn,
                request.userAnswers.id,
                packageTypeList.values,
                houseConsignmentMode,
                itemMode,
                packageMode,
                houseConsignmentIndex,
                itemIndex,
                packageIndex
              )
            )
        }
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
    actions.requireData(arrivalId).async {
      implicit request =>
        referenceDataService.getPackageTypes().flatMap {
          packagesTypeList =>
            val viewModel               = viewModelProvider.apply(houseConsignmentIndex, itemIndex, packageMode)
            val form: Form[PackageType] = formProvider(packageMode, prefix, packagesTypeList, houseConsignmentIndex.display, itemIndex.display)
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(
                      view(
                        viewModel,
                        formWithErrors,
                        request.userAnswers.mrn,
                        request.userAnswers.id,
                        packagesTypeList.values,
                        houseConsignmentMode,
                        itemMode,
                        packageMode,
                        houseConsignmentIndex,
                        itemIndex,
                        packageIndex
                      )
                    )
                  ),
                value => redirect(value, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
              )

        }
    }

  private def redirect(
    value: PackageType,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    packageIndex: Index,
    houseConsignmentMode: Mode,
    itemMode: Mode,
    packageMode: Mode
  )(implicit request: MandatoryDataRequest[?]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield {
      val navigator = navigatorProvider.apply(houseConsignmentMode, itemMode)
      Redirect(navigator.nextPage(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, request.userAnswers))
    }
}
