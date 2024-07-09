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
import forms.CUSCodeFormProvider
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.items.HouseConsignmentItemNavigator.HouseConsignmentItemNavigatorProvider
import pages.houseConsignment.index.items.CustomsUnionAndStatisticsCodePage
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.items.CustomsUnionAndStatisticsCodeViewModel
import viewModels.houseConsignment.index.items.CustomsUnionAndStatisticsCodeViewModel.CustomsUnionAndStatisticsCodeViewModelProvider
import views.html.houseConsignment.index.items.CustomsUnionAndStatisticsCodeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsUnionAndStatisticsCodeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: HouseConsignmentItemNavigatorProvider,
  formProvider: CUSCodeFormProvider,
  service: ReferenceDataService,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: CustomsUnionAndStatisticsCodeView,
  viewModelProvider: CustomsUnionAndStatisticsCodeViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix = "houseConsignment.item.customsUnionAndStatisticsCode"

  private def form(viewModel: CustomsUnionAndStatisticsCodeViewModel): Form[String] =
    formProvider(prefix, viewModel.requiredError)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentMode: Mode, itemMode: Mode, houseConsignmentIndex: Index, itemIndex: Index): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val viewModel = viewModelProvider.apply(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex)
        val preparedForm = request.userAnswers.get(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex)) match {
          case None        => form(viewModel)
          case Some(value) => form(viewModel).fill(value)
        }
        Ok(view(preparedForm, request.userAnswers.mrn, viewModel))
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentMode: Mode, itemMode: Mode, houseConsignmentIndex: Index, itemIndex: Index): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        val viewModel = viewModelProvider.apply(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex)
        form(viewModel)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, request.userAnswers.mrn, viewModel))
              ),
            value =>
              service.doesCUSCodeExist(value).flatMap {
                case true => redirect(value, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
                case false =>
                  val formWithErrors =
                    form(viewModel).withError(FormError("value", s"$prefix.error.not.exists"))
                  Future.successful(
                    BadRequest(view(formWithErrors, request.userAnswers.mrn, viewModel))
                  )
              }
          )
    }

  private def redirect(
    value: String,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    houseConsignmentMode: Mode,
    itemMode: Mode
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield {
      val navigator = navigatorProvider.apply(houseConsignmentMode)
      Redirect(navigator.nextPage(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex), itemMode, request.userAnswers))
    }
}
