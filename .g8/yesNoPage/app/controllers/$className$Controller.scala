package controllers

import controllers.actions._
import forms.$className$FormProvider
import javax.inject.Inject
import models.{Mode, MovementReferenceNumber, ArrivalId}
import navigation.Navigator
import pages.$className$Page
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.{ExecutionContext, Future}

class $className;format="cap"$Controller @Inject()(
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalActionProvider,
    requireData: DataRequiredAction,
    formProvider: $className$FormProvider,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = (identify andThen getData(arrivalId) andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get($className$Page) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val json = Json.obj(
        "form"   -> preparedForm,
        "mode"   -> mode,
        "mrn"    -> request.userAnswers.mrn,
        "arrivalId" -> arrivalId,
        "radios" -> Radios.yesNo(form("value"))
      )

      renderer.render("$className;format="decap"$.njk", json).map(Ok(_))
  }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = (identify andThen getData(arrivalId) andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => {

          val json = Json.obj(
            "form"   -> formWithErrors,
            "mode"   -> mode,
            "mrn"    -> request.userAnswers.mrn,
            "arrivalId" -> arrivalId,
            "radios" -> Radios.yesNo(form("value"))
          )
    
          renderer.render("$className;format="decap"$.njk", json).map(BadRequest(_))
        },
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set($className$Page, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage($className$Page, mode, updatedAnswers))
      )
  }
}
