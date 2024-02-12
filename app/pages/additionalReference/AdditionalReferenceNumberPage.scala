package pages.additionalReference

import models.Index
import pages.QuestionPage
import play.api.libs.json.JsPath


case class AdditionalReferenceNumberPage(referenceIndex:Index) extends QuestionPage[String] {

    override def path: JsPath =  JsPath \ "Consignment" \ "AdditionalReference" \ referenceIndex.position \ toString

    override def toString: String = "referenceNumber"
  }