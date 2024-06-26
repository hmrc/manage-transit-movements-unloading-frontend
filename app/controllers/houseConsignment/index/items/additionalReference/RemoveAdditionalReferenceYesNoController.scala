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
import forms.YesNoFormProvider
import models.removable.AdditionalReference
import models.requests.DataRequest
import models.{ArrivalId, Index, Mode, UserAnswers}
import pages.houseConsignment.index.items.additionalReference.{AdditionalReferenceTypePage, RemoveAdditionalReferenceNumberYesNoPage}
import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferenceSection
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.items.additionalReference.RemoveAdditionalReferenceYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveAdditionalReferenceYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveAdditionalReferenceYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("houseConsignment.index.items.additionalReference.removeAdditionalReferenceYesNo")

  private def addAnother(arrivalId: ArrivalId, houseConsignmentMode: Mode, itemMode: Mode, houseConsignmentIndex: Index, itemIndex: Index): Call =
    controllers.houseConsignment.index.items.additionalReference.routes.AddAnotherAdditionalReferenceController
      .onSubmit(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex)

  def insetText(userAnswers: UserAnswers, houseConsignmentIndex: Index, itemIndex: Index, additionalReferenceIndex: Index): Option[String] =
    AdditionalReference(userAnswers, houseConsignmentIndex, itemIndex, additionalReferenceIndex).map(_.forRemoveDisplay)

  def onPageLoad(
    arrivalId: ArrivalId,
    houseConsignmentMode: Mode,
    itemMode: Mode,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    additionalReferenceIndex: Index
  ): Action[AnyContent] =
    actions
      .requireIndex(
        arrivalId,
        AdditionalReferenceSection(houseConsignmentIndex, itemIndex, additionalReferenceIndex),
        addAnother(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex)
      )
      .andThen(getMandatoryPage.getFirst(AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex))) {
        implicit request =>
          val preparedForm =
            request.userAnswers.get(RemoveAdditionalReferenceNumberYesNoPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

          Ok(
            view(
              preparedForm,
              request.userAnswers.mrn,
              arrivalId,
              houseConsignmentMode,
              itemMode,
              houseConsignmentIndex,
              itemIndex,
              additionalReferenceIndex,
              insetText(request.userAnswers, houseConsignmentIndex, itemIndex, additionalReferenceIndex)
            )
          )
      }

  def onSubmit(
    arrivalId: ArrivalId,
    houseConsignmentMode: Mode,
    itemMode: Mode,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    additionalReferenceIndex: Index
  ): Action[AnyContent] =
    actions
      .requireIndex(
        arrivalId,
        AdditionalReferenceSection(houseConsignmentIndex, itemIndex, additionalReferenceIndex),
        addAnother(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex)
      )
      .async {
        implicit request =>
          val preparedForm =
            request.userAnswers.get(RemoveAdditionalReferenceNumberYesNoPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

          preparedForm
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future
                  .successful(
                    BadRequest(
                      view(
                        formWithErrors,
                        request.userAnswers.mrn,
                        arrivalId,
                        houseConsignmentMode,
                        itemMode,
                        houseConsignmentIndex,
                        itemIndex,
                        additionalReferenceIndex,
                        insetText(request.userAnswers, houseConsignmentIndex, itemIndex, additionalReferenceIndex)
                      )
                    )
                  ),
              value => redirect(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex, additionalReferenceIndex, request, value)
            )
      }

  private def redirect(
    arrivalId: ArrivalId,
    houseConsignmentMode: Mode,
    itemMode: Mode,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    additionalReferenceIndex: Index,
    request: DataRequest[AnyContent],
    value: Boolean
  ) =
    for {
      updatedAnswers <-
        if (value) {
          val additionalReferenceSection = AdditionalReferenceSection(houseConsignmentIndex, itemIndex, additionalReferenceIndex)
          Future.fromTry(request.userAnswers.removeDataGroup(additionalReferenceSection))
        } else {
          Future.successful(request.userAnswers)
        }
      _ <- sessionRepository.set(updatedAnswers)
    } yield Redirect(addAnother(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex))
}
