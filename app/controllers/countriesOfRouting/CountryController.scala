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

package controllers.countriesOfRouting

import controllers.actions.*
import forms.SelectableFormProvider.CountryFormProvider
import models.{ArrivalId, Index, Mode, SelectableList}
import pages.CountryOfRoutingPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import services.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.countriesOfRouting.CountryViewModel.CountryViewModelProvider
import views.html.countriesOfRouting.CountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CountryController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: CountryFormProvider,
  referenceDataService: ReferenceDataService,
  val controllerComponents: MessagesControllerComponents,
  view: CountryView,
  countryViewModelProvider: CountryViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix = "countriesOfRouting.country"

  def onPageLoad(arrivalId: ArrivalId, index: Index, mode: Mode): Action[AnyContent] =
    actions.requirePhase6(arrivalId).async {
      implicit request =>
        referenceDataService
          .getCountries()
          .map(SelectableList(_))
          .map {
            countries =>
              val viewModel = countryViewModelProvider.apply(mode)
              val form      = formProvider(mode, prefix, countries, index)
              val preparedForm = request.userAnswers.get(CountryOfRoutingPage(index)) match {
                case None        => form
                case Some(value) => form.fill(value)
              }
              Ok(view(preparedForm, countries.values, request.userAnswers.mrn, arrivalId, index, mode, viewModel))
          }
    }

  def onSubmit(arrivalId: ArrivalId, index: Index, mode: Mode): Action[AnyContent] =
    actions.requirePhase6(arrivalId).async {
      implicit request =>
        referenceDataService
          .getCountries()
          .map(SelectableList(_))
          .flatMap {
            countries =>
              val viewModel = countryViewModelProvider.apply(mode)
              val form      = formProvider(mode, prefix, countries, index)
              form
                .bindFromRequest()
                .fold(
                  formWithErrors =>
                    Future.successful(BadRequest(view(formWithErrors, countries.values, request.userAnswers.mrn, arrivalId, index, mode, viewModel))),
                  value =>
                    for {
                      updatedAnswers <- Future.fromTry(request.userAnswers.set(CountryOfRoutingPage(index), value))
                      _              <- sessionRepository.set(updatedAnswers)
                    } yield Redirect(routes.CountryController.onSubmit(arrivalId, index, mode))
                )
          }
    }
}
