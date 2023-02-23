package pages.sections

import play.api.libs.json.JsPath
import pages.QuestionPage

case object $pageSection$ extends QuestionPage[Nothing] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "$className;format="decap"$"
}
