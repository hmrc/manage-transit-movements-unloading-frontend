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

import com.google.inject.Inject
import controllers.actions.Actions
import logging.Logging
import models.ArrivalId
import models.AuditType.UnloadingRemarks
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.submission.{AuditService, MetricsService, SubmissionService}
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.CheckYourAnswersViewModel
import viewModels.CheckYourAnswersViewModel.CheckYourAnswersViewModelProvider
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class CheckYourAnswersController @Inject() (
                                             override val messagesApi: MessagesApi,
                                             actions: Actions,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: CheckYourAnswersView,
                                             viewModelProvider: CheckYourAnswersViewModelProvider,
                                             submissionService: SubmissionService,
                                             auditService: AuditService,
                                             metricsService: MetricsService
                                           )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val viewModel: CheckYourAnswersViewModel = viewModelProvider.apply(request.userAnswers)

        Ok(view(request.userAnswers.mrn, arrivalId, viewModel))
    }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        submissionService.submit(request.userAnswers, arrivalId).map {
          response =>
            val auditType = UnloadingRemarks
            metricsService.increment(auditType.name, response)
            response.status match {
              case x if is2xx(x) =>
                auditService.audit(auditType, request.userAnswers)
                Redirect(controllers.routes.UnloadingRemarksSentController.onPageLoad(arrivalId))
              case x =>
                logger.error(s"Error submitting IE044: $x")
                Redirect(routes.ErrorController.technicalDifficulties())
            }
        }
    }
}
