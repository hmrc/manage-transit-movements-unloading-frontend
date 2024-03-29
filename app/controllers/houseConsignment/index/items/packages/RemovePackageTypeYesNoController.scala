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

package controllers.houseConsignment.index.items.packages

import controllers.actions._
import forms.YesNoFormProvider
import models.reference.PackageType
import models.requests.SpecificDataRequestProvider1
import models.{ArrivalId, Index, Mode}
import pages.houseConsignment.index.items.packages.{NumberOfPackagesPage, PackageTypePage}
import pages.sections.PackagingSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.items.packages.RemovePackageTypeYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemovePackageTypeYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemovePackageTypeYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def addAnother(arrivalId: ArrivalId, mode: Mode): Call =
    Call("GET", "#") //TODO should go to addAnother package page

  private type Request = SpecificDataRequestProvider1[PackageType]#SpecificDataRequest[_]

  private def packageType(implicit request: Request): PackageType = request.arg

  private def form(houseConsignmentIndex: Index, itemIndex: Index): Form[Boolean] =
    formProvider("houseConsignment.index.items.packages.removePackageTypeYesNo", houseConsignmentIndex, itemIndex)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, packageIndex: Index, mode: Mode): Action[AnyContent] =
    actions
      .getStatus(arrivalId)
      .andThen(getMandatoryPage(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex))) {
        implicit request =>
          val quantity = request.userAnswers.get(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex)).map(_.toString())
          Ok(
            view(
              form(houseConsignmentIndex, itemIndex),
              request.userAnswers.mrn,
              arrivalId,
              houseConsignmentIndex,
              itemIndex,
              packageIndex,
              mode,
              insetText(quantity, packageType)
            )
          )
      }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, packageIndex: Index, mode: Mode): Action[AnyContent] =
    actions
      .getStatus(arrivalId)
      .andThen(getMandatoryPage(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex)))
      .async {
        implicit request =>
          val quantity = request.userAnswers.get(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex)).map(_.toString())
          form(houseConsignmentIndex, itemIndex)
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(formWithErrors,
                         request.userAnswers.mrn,
                         arrivalId,
                         houseConsignmentIndex,
                         itemIndex,
                         packageIndex,
                         mode,
                         insetText(quantity, packageType)
                    )
                  )
                ),
              value =>
                for {
                  updatedAnswers <-
                    if (value) {
                      Future.fromTry(request.userAnswers.removeExceptSequenceNumber(PackagingSection(houseConsignmentIndex, itemIndex, packageIndex)))
                    } else { Future.successful(request.userAnswers) }
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(addAnother(arrivalId, mode))
            )
      }

  private def insetText(quantity: Option[String], packageType: PackageType): Option[String] =
    quantity
      .map(
        value => s"$value ${packageType.toString}"
      )
      .orElse(Some(packageType.toString))
}
