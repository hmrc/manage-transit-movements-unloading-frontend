/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.EnrolmentStoreConnector
import controllers.routes
import models.EoriNumber
import models.requests.IdentifierRequest
import play.api.mvc.Results._
import play.api.mvc._
import renderer.Renderer
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.EmptyPredicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default,
  enrolmentStoreConnector: EnrolmentStoreConnector,
  renderer: Renderer
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(EmptyPredicate)
      .retrieve(Retrievals.allEnrolments and Retrievals.groupIdentifier) {
        case enrolments ~ maybeGroupId =>
          val newEnrolment: Option[Enrolment]    = enrolments.enrolments.filter(_.isActivated).find(_.key.equals(config.newEnrolmentKey))
          val legacyEnrolment: Option[Enrolment] = enrolments.enrolments.filter(_.isActivated).find(_.key.equals(config.legacyEnrolmentKey))

          val enrolment = newEnrolment orElse legacyEnrolment
          enrolment match {
            case Some(_) =>
              val newEnrolmentId: Option[EnrolmentIdentifier]    = newEnrolment.flatMap(_.getIdentifier(config.newEnrolmentIdentifierKey))
              val legacyEnrolmentId: Option[EnrolmentIdentifier] = legacyEnrolment.flatMap(_.getIdentifier(config.legacyEnrolmentIdentifierKey))

              val identifier = newEnrolmentId orElse legacyEnrolmentId
              identifier match {
                case Some(eoriNumber) =>
                  block(IdentifierRequest(request, EoriNumber(eoriNumber.value)))
                case _ => Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
              }
            case None => checkForGroupEnrolment(maybeGroupId, config)(hc, request)
          }
      }
  } recover {
    case _: NoActiveSession =>
      Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
    case _: AuthorisationException =>
      Redirect(routes.UnauthorisedController.onPageLoad())
  }

  private def checkForGroupEnrolment[A](maybeGroupId: Option[String], config: FrontendAppConfig)(implicit
    hc: HeaderCarrier,
    request: Request[A]
  ): Future[Result] =
    maybeGroupId match {
      case Some(groupId) =>
        val hasGroupEnrolment = for {
          newGroupEnrolment <- enrolmentStoreConnector.checkGroupEnrolments(groupId, config.newEnrolmentKey)
          legacyGroupEnrolment <-
            if (newGroupEnrolment) { Future.successful(newGroupEnrolment) }
            else { enrolmentStoreConnector.checkGroupEnrolments(groupId, config.legacyEnrolmentKey) }
        } yield newGroupEnrolment || legacyGroupEnrolment

        hasGroupEnrolment flatMap {
          case true  => renderer.render("unauthorisedWithGroupAccess.njk").map(Unauthorized(_))
          case false => Future.successful(Redirect(config.eccEnrolmentSplashPage))
        }
      case _ => Future.successful(Redirect(config.eccEnrolmentSplashPage))
    }
}
