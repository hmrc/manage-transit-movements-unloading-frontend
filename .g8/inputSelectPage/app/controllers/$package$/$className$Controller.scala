package controllers.$package$

import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.$formProvider$
import models.{Mode, LocalReferenceNumber}
import navigation.UserAnswersNavigator
import pages.$package$.$className$Page
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.$serviceName$
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.$package$.$className$View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class $className$Controller @Inject()(
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: $navRoute$NavigatorProvider,
  actions: Actions,
  formProvider: $formProvider$,
  service: $serviceName$,
  val controllerComponents: MessagesControllerComponents,
  view: $className$View
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.$lookupReferenceListMethod$.map {
        $referenceListClass;format="decap"$ =>
          val form = formProvider("$package$.$className;format="decap"$", $referenceListClass;format="decap"$)
          val preparedForm = request.userAnswers.get($className$Page) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, lrn, $referenceListClass;format="decap"$.$referenceClassPlural;format="decap"$, mode))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.$lookupReferenceListMethod$.flatMap {
        $referenceListClass;format="decap"$ =>
          val form = formProvider("$package$.$className;format="decap"$", $referenceListClass;format="decap"$)
          form.bindFromRequest().fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, $referenceListClass;format="decap"$.$referenceClassPlural;format="decap"$, mode))),
            value => {
              implicit val navigator: UserAnswersNavigator = navigatorProvider(mode)
              $className$Page.writeToUserAnswers(value).updateTask[$navRoute$Domain]().writeToSession().navigate()
            }
        )
      }
  }
}
