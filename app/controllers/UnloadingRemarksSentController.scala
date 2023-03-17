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
import forms.VehicleIdentificationNumberFormProvider
import models.reference.CustomsOffice
import models.{ArrivalId, Mode, MovementReferenceNumber}
import navigation.Navigator
import pages.VehicleIdentificationNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.{FrontendBaseController, FrontendController}
import models.UnloadingRemarksSentViewModel
import views.html.UnloadingRemarksSentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnloadingRemarksSentController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  referenceDataService: ReferenceDataService,
  cc: MessagesControllerComponents,
  view: UnloadingRemarksSentView
)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      val officeOfDestination = "CODE-001"
      referenceDataService
        .getCustomsOfficeByCode(officeOfDestination)
        .map(
          customsOffice => Ok(view(request.userAnswers.mrn, UnloadingRemarksSentViewModel(customsOffice, officeOfDestination)))
        )
  }
}
