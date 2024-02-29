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

package controllers.departureMeansOfTransport

import controllers.actions._
import forms.YesNoFormProvider
import models.departureTransportMeans.TransportMeansIdentification
import models.requests.SpecificDataRequestProvider1
import models.{ArrivalId, Index, Mode, TransportMeans}
import pages.departureMeansOfTransport._
import pages.sections.TransportMeansSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RemoveDepartureMeansOfTransportYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveDepartureMeansOfTransportYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveDepartureMeansOfTransportYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form: Form[Boolean] = formProvider("departureMeansOfTransport.index.removeDepartureMeansOfTransportYesNo")

  private def addAnother(arrivalId: ArrivalId, mode: Mode): Call =
    controllers.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController.onPageLoad(
      arrivalId,
      mode
    )

  private def formatInsetText(transportMeansIndex: Index,
                              request: SpecificDataRequestProvider1[TransportMeansIdentification]#SpecificDataRequest[AnyContent]
  ): String = {
    val identificationType: TransportMeansIdentification = request.arg
    val identificationNumber: Option[String]             = request.userAnswers.get(VehicleIdentificationNumberPage(transportMeansIndex))
    val insetText                                        = TransportMeans(identificationType.description, identificationNumber).asString
    insetText
  }

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, transportMeansIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, TransportMeansSection(transportMeansIndex), addAnother(arrivalId, mode))
    .andThen(getMandatoryPage(TransportMeansIdentificationPage(transportMeansIndex))) {
      implicit request =>
        val insetText: String = formatInsetText(transportMeansIndex, request)
        Ok(view(form, request.userAnswers.mrn, arrivalId, transportMeansIndex, insetText, mode))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, transportMeansIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, TransportMeansSection(transportMeansIndex), addAnother(arrivalId, mode))
    .andThen(getMandatoryPage(TransportMeansIdentificationPage(transportMeansIndex)))
    .async {
      implicit request =>
        val insetText: String = formatInsetText(transportMeansIndex, request)
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future
                .successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, transportMeansIndex, insetText, mode))),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.removeExceptSequenceNumber(TransportMeansSection(transportMeansIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(arrivalId, mode))
          )
    }
}
