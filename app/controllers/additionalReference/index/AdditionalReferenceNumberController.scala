/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.additionalReference.index

import controllers.actions._
import forms.AdditionalReferenceNumberFormProvider
import models.reference.AdditionalReferenceType
import models.requests.SpecificDataRequestProvider1
import models.{ArrivalId, CheckMode, Index, Mode, NormalMode, RichOptionalJsArray}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import repositories.SessionRepository
import navigation.Navigator
import pages.AdditionalReferenceNumberPage
import pages.additionalReference.AdditionalReferenceTypePage
import pages.sections.AdditionalReferenceListSection
import viewModels.additionalReference.index.AdditionalReferenceNumberViewModel.AdditionalReferenceNumberViewModelProvider
import views.html.additionalReference.index.AdditionalReferenceNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalReferenceNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: AdditionalReferenceNumberFormProvider,
  viewModelProvider: AdditionalReferenceNumberViewModelProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AdditionalReferenceNumberView,
  getMandatoryPage: SpecificDataRequiredActionProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private type Request = SpecificDataRequestProvider1[AdditionalReferenceType]#SpecificDataRequest[_]

  def onPageLoad(arrivalId: ArrivalId, additionalReferenceIndex: Index, mode: Mode): Action[AnyContent] =
    actions
      .getStatus(arrivalId)
      .andThen(getMandatoryPage(AdditionalReferenceTypePage(additionalReferenceIndex))) {
        implicit request =>
          val viewModel = viewModelProvider.apply(mode)
          val form      = formProvider(viewModel.requiredError)
          val preparedForm = request.userAnswers.get(AdditionalReferenceNumberPage(additionalReferenceIndex)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          val additionalRefTypeList = otherAdditionalReferenceTypes(additionalReferenceIndex)
          Ok(
            view(
              preparedForm,
              arrivalId,
              request.userAnswers.mrn,
              additionalReferenceIndex,
              mode,
              viewModel,
              isParagraphRequired(mode, additionalRefTypeList)
            )
          )
      }

  def onSubmit(arrivalId: ArrivalId, additionalReferenceIndex: Index, mode: Mode): Action[AnyContent] =
    actions
      .getStatus(arrivalId)
      .andThen(getMandatoryPage(AdditionalReferenceTypePage(additionalReferenceIndex)))
      .async {
        implicit request =>
          val viewModel = viewModelProvider.apply(mode)
          val form      = formProvider(viewModel.requiredError)
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      arrivalId,
                      request.userAnswers.mrn,
                      additionalReferenceIndex,
                      mode,
                      viewModel,
                      isParagraphRequired(mode, otherAdditionalReferenceTypes(additionalReferenceIndex))
                    )
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(AdditionalReferenceNumberPage(additionalReferenceIndex), value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(AdditionalReferenceNumberPage(additionalReferenceIndex), mode, updatedAnswers))
            )
      }

  private def otherAdditionalReferenceTypes(additionalReferenceIndex: Index)(implicit request: Request): Seq[AdditionalReferenceType] = {
    val numberOfAdditionalReferences = request.userAnswers.get(AdditionalReferenceListSection).length
    (0 until numberOfAdditionalReferences)
      .map(Index(_))
      .filterNot(_ == additionalReferenceIndex)
      .map(AdditionalReferenceTypePage)
      .flatMap(request.userAnswers.get(_))
  }

  private def isParagraphRequired(mode: Mode, additionalRefTypeList: Seq[AdditionalReferenceType])(implicit request: Request) =
    mode match {
      case CheckMode  => false
      case NormalMode => additionalRefTypeList.contains(request.arg)
    }

}
