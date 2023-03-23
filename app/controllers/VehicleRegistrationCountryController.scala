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

package controllers

import controllers.actions._
import forms.VehicleRegistrationCountryFormProvider
import models.reference.Country
import models.{ArrivalId, Index, Mode}
import navigation.Navigator
import pages.VehicleRegistrationCountryPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import services.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.VehicleRegistrationCountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VehicleRegistrationCountryController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: VehicleRegistrationCountryFormProvider,
  referenceDataService: ReferenceDataService,
  val controllerComponents: MessagesControllerComponents,
  view: VehicleRegistrationCountryView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, transportMeansIndex: Index, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      referenceDataService.getCountries() map {
        countries =>
          val form = formProvider(countries)
          val preparedForm = request.userAnswers.get(VehicleRegistrationCountryPage(transportMeansIndex)) match {
            case None        => form
            case Some(value) => form.fill(Country(value, "test")) // TODO: Fix this change back to country
          }
          Ok(view(preparedForm, countries, request.userAnswers.mrn, arrivalId, transportMeansIndex, mode))
      }
  }

  def onSubmit(arrivalId: ArrivalId, transportMeansIndex: Index, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      referenceDataService.getCountries() flatMap {
        countries =>
          val form = formProvider(countries)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, countries, request.userAnswers.mrn, arrivalId, transportMeansIndex, mode))),
              value =>
                for {
                  updatedAnswers <- Future
                    .fromTry(request.userAnswers.set(VehicleRegistrationCountryPage(transportMeansIndex), value.code)) // TODO: Fix this change back to country
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
            )
      }
  }
}
