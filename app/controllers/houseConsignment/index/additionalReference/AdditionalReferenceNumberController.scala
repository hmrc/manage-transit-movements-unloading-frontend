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

package controllers.houseConsignment.index.additionalReference

import controllers.actions._
import forms.HouseConsignmentAdditionalReferenceNumberFormProvider
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.AdditionalReferenceNavigator.AdditionalReferenceNavigatorProvider
import pages.houseConsignment.index.additionalReference.HouseConsignmentAdditionalReferenceNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.additionalReference.AdditionalReferenceNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalReferenceNumberController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: AdditionalReferenceNavigatorProvider,
  formProvider: HouseConsignmentAdditionalReferenceNumberFormProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: AdditionalReferenceNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("houseConsignment.index.additionalReference.additionalReferenceNumber")

  def onPageLoad(
    arrivalId: ArrivalId,
    houseConsignmentMode: Mode,
    additionalReferenceMode: Mode,
    houseConsignmentIndex: Index,
    additionalReferenceIndex: Index
  ): Action[AnyContent] = actions
    .requireData(arrivalId) {
      implicit request =>
        val preparedForm = request.userAnswers.get(HouseConsignmentAdditionalReferenceNumberPage(houseConsignmentIndex, additionalReferenceIndex)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(
          view(preparedForm, arrivalId, request.userAnswers.mrn, houseConsignmentMode, additionalReferenceMode, houseConsignmentIndex, additionalReferenceIndex)
        )
    }

  def onSubmit(
    arrivalId: ArrivalId,
    houseConsignmentMode: Mode,
    additionalReferenceMode: Mode,
    houseConsignmentIndex: Index,
    additionalReferenceIndex: Index
  ): Action[AnyContent] = actions
    .requireData(arrivalId)
    .async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(
                  view(formWithErrors,
                       arrivalId,
                       request.userAnswers.mrn,
                       houseConsignmentMode,
                       additionalReferenceMode,
                       houseConsignmentIndex,
                       additionalReferenceIndex
                  )
                )
              ),
            value => redirect(value, houseConsignmentIndex, additionalReferenceIndex, houseConsignmentMode, additionalReferenceMode)
          )
    }

  private def redirect(
    value: String,
    houseConsignmentIndex: Index,
    additionalReferenceIndex: Index,
    houseConsignmentMode: Mode,
    additionalReferenceMode: Mode
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(
        request.userAnswers.set(HouseConsignmentAdditionalReferenceNumberPage(houseConsignmentIndex, additionalReferenceIndex), value)
      )
      _ <- sessionRepository.set(updatedAnswers)
    } yield {
      val navigator = navigatorProvider.apply(houseConsignmentMode)
      Redirect(
        navigator.nextPage(HouseConsignmentAdditionalReferenceNumberPage(houseConsignmentIndex, additionalReferenceIndex),
                           additionalReferenceMode,
                           request.userAnswers
        )
      )
    }
}
