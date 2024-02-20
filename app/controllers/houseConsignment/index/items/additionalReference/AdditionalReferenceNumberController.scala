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

package controllers.houseConsignment.index.items.additionalReference

import controllers.actions._
import forms.ItemsAdditionalReferenceNumberFormProvider
import models.reference.AdditionalReferenceType
import models.requests.{MandatoryDataRequest, SpecificDataRequestProvider1}
import models.{ArrivalId, CheckMode, Index, Mode, NormalMode, RichOptionalJsArray}
import navigation.Navigator
import pages.houseConsignment.index.items.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferencePage}
import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferencesSection
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.items.additionalReference.AdditionalReferenceNumberViewModel.AdditionalReferenceNumberViewModelProvider
import views.html.houseConsignment.index.items.additionalReference.AdditionalReferenceNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalReferenceNumberController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: Navigator,
  formProvider: ItemsAdditionalReferenceNumberFormProvider,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AdditionalReferenceNumberView,
  viewModelProvider: AdditionalReferenceNumberViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private type Request = SpecificDataRequestProvider1[AdditionalReferenceType]#SpecificDataRequest[AnyContent]

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, itemIndex: Index, additionalReferenceIndex: Index): Action[AnyContent] =
    actions
      .requireData(arrivalId)
      .andThen(getMandatoryPage(AdditionalReferencePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex))) {
        implicit request =>
          val paragraphRequired = isParagraphRequired(mode, houseConsignmentIndex, itemIndex, additionalReferenceIndex)
          val viewModel         = viewModelProvider.apply(arrivalId, mode, houseConsignmentIndex, itemIndex, additionalReferenceIndex)
          val form              = formProvider(viewModel.requiredError)
          val preparedForm = request.userAnswers.get(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, request.userAnswers.mrn, viewModel, paragraphRequired))
      }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, itemIndex: Index, additionalReferenceIndex: Index): Action[AnyContent] = actions
    .requireData(arrivalId)
    .andThen(getMandatoryPage(AdditionalReferencePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)))
    .async {
      implicit request =>
        val paragraphRequired = isParagraphRequired(mode, houseConsignmentIndex, itemIndex, additionalReferenceIndex)
        val viewModel         = viewModelProvider.apply(arrivalId, mode, houseConsignmentIndex, itemIndex, additionalReferenceIndex)
        val form              = formProvider(viewModel.requiredError)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, viewModel, paragraphRequired))),
            value => redirect(value, houseConsignmentIndex, itemIndex, additionalReferenceIndex, mode)
          )
    }

  private def isParagraphRequired(mode: Mode, houseConsignmentIndex: Index, itemIndex: Index, additionalReferenceIndex: Index)(implicit
    request: Request
  ): Boolean =
    mode match {
      case CheckMode  => false
      case NormalMode => otherAdditionalReferenceTypes(houseConsignmentIndex, itemIndex, additionalReferenceIndex).contains(request.arg)
    }

  private def otherAdditionalReferenceTypes(houseConsignmentIndex: Index, itemIndex: Index, additionalReferenceIndex: Index)(implicit
    request: Request
  ): Seq[AdditionalReferenceType] = {
    val numberOfAdditionalReferences = request.userAnswers.get(AdditionalReferencesSection(houseConsignmentIndex, itemIndex)).length
    (0 until numberOfAdditionalReferences)
      .map(Index(_))
      .filterNot(_ == additionalReferenceIndex)
      .map(AdditionalReferencePage(houseConsignmentIndex, itemIndex, _))
      .flatMap(request.userAnswers.get(_))
  }

  private def redirect(
    value: String,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    additionalReferenceIndex: Index,
    mode: Mode
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(
        request.userAnswers.set(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), value)
      )
      _ <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), mode, request.userAnswers))
}
