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

package controllers.houseConsignment.index.departureMeansOfTransport

import controllers.actions._
import forms.SelectableFormProvider
import models.{ArrivalId, Index, Mode, SelectableList}
import navigation.DepartureTransportMeansNavigator
import pages.houseConsignment.index.departureMeansOfTransport.CountryPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import services.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.departureTransportMeans.HouseConsignmentCountryViewModel.HouseConsignmentCountryViewModelProvider
import views.html.houseConsignment.index.departureMeansOfTransport.CountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CountryController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: SelectableFormProvider,
  referenceDataService: ReferenceDataService,
  val controllerComponents: MessagesControllerComponents,
  view: CountryView,
  navigator: DepartureTransportMeansNavigator,
  countryViewModelProvider: HouseConsignmentCountryViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val prefix = "houseConsignment.index.departureMeansOfTransport.country"

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, transportMeansIndex: Index, mode: Mode): Action[AnyContent] =
    actions.getStatus(arrivalId).async {
      implicit request =>
        referenceDataService
          .getCountries()
          .map(
            country => SelectableList(country.toSeq)
          ) map {
          countries =>
            val viewModel = countryViewModelProvider.apply(mode, houseConsignmentIndex)
            val form      = formProvider(mode, prefix, countries, houseConsignmentIndex, transportMeansIndex)

            val preparedForm = request.userAnswers.get(CountryPage(houseConsignmentIndex, transportMeansIndex)) match {
              case None        => form
              case Some(value) => form.fill(value)
            }
            Ok(view(preparedForm, countries.values, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, transportMeansIndex, mode, viewModel))
        }
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, transportMeansIndex: Index, mode: Mode): Action[AnyContent] =
    actions.getStatus(arrivalId).async {
      implicit request =>
        referenceDataService
          .getCountries()
          .map(
            country => SelectableList(country.toSeq)
          ) flatMap {
          countries =>
            val viewModel = countryViewModelProvider.apply(mode, houseConsignmentIndex)
            val form      = formProvider(mode, prefix, countries, houseConsignmentIndex, transportMeansIndex)
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(
                      view(formWithErrors, countries.values, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, transportMeansIndex, mode, viewModel)
                    )
                  ),
                value =>
                  for {
                    updatedAnswers <- Future
                      .fromTry(request.userAnswers.set(CountryPage(houseConsignmentIndex, transportMeansIndex), value))
                    _ <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(CountryPage(houseConsignmentIndex, transportMeansIndex), mode, request.userAnswers))
              )
        }
    }
}
