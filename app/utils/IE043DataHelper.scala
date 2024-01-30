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

package utils

import models.{ArrivalId, Index, UserAnswers}
import pages._
import pages.sections.Section
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, JsValue, Reads}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class IE043DataHelper(userAnswers: UserAnswers)(implicit messages: Messages) extends SummaryListRowHelper {

  def arrivalId: ArrivalId = userAnswers.id

  def getAnswerAndBuildRow[T](
    page: QuestionPage[T],
    formatAnswer: T => Content,
    prefix: String,
    id: Option[String],
    call: Option[Call],
    args: Any*
  )(implicit rds: Reads[T]): Option[SummaryListRow] =
    userAnswers.get(page) map {
      answer =>
        buildRow(
          prefix = prefix,
          answer = formatAnswer(answer),
          id = id,
          call = call,
          args = args: _*
        )
    }

  def parseArguments[T](args: Option[Seq[Any]], answer: T): Seq[Any] = args match {
    case None            => Seq(answer)
    case Some(arguments) => arguments.appended(answer)
  }

  def getAnswerAndBuildRowWithDynamicHiddenText[T](
    page: QuestionPage[T],
    formatAnswer: T => Content,
    prefix: String,
    id: Option[String],
    call: Option[Call],
    args: Option[Seq[Any]]
  )(implicit rds: Reads[T]): Option[SummaryListRow] =
    userAnswers.get(page) map {
      answer =>
        buildRow(
          prefix = prefix,
          answer = formatAnswer(answer),
          id = id,
          call = call,
          args = parseArguments(args, answer): _*
        )
    }

  def getAnswerAndBuildRemovableRowWithDynamicHiddenText[T](
    page: QuestionPage[T],
    formatAnswer: T => Content,
    prefix: String,
    id: String,
    changeCall: Call,
    removeCall: Call,
    args: Option[Seq[Any]]
  )(implicit rds: Reads[T]): Option[SummaryListRow] =
    userAnswers.get(page) map {
      answer =>
        buildRemovableRow(
          prefix = prefix,
          answer = formatAnswer(answer),
          id = id,
          changeCall = changeCall,
          removeCall = removeCall,
          args = parseArguments(args, answer): _*
        )
    }

  def getAnswerAndBuildRowWithDynamicPrefix[T](
    answerPath: QuestionPage[T],
    titlePath: QuestionPage[T],
    formatAnswer: T => Content,
    dynamicPrefix: T => String,
    id: Option[String],
    call: Option[Call],
    args: Any*
  )(implicit rds: Reads[T]): Option[SummaryListRow] =
    for {
      answer <- userAnswers.get(answerPath)
      title  <- userAnswers.get(titlePath)
    } yield buildRowFromPath(
      prefix = dynamicPrefix(title),
      answer = formatAnswer(answer),
      id = id,
      call = call,
      args = args: _*
    )

  def getAnswerAndBuildRemovableRow[T](
    page: QuestionPage[T],
    formatAnswer: T => Content,
    prefix: String,
    id: String,
    changeCall: Call,
    removeCall: Call,
    args: Any*
  )(implicit rds: Reads[T]): Option[SummaryListRow] =
    userAnswers.get(page) map {
      answer =>
        buildRemovableRow(
          prefix = prefix,
          answer = formatAnswer(answer),
          id = id,
          changeCall = changeCall,
          removeCall = removeCall,
          args = args: _*
        )
    }

  implicit class RichJsArray(arr: JsArray) {

    def zipWithIndex: List[(JsValue, Index)] = arr.value.toList.zipWithIndex.map(
      x => (x._1, Index(x._2))
    )

    def filterWithIndex(f: (JsValue, Index) => Boolean): Seq[(JsValue, Index)] =
      arr.zipWithIndex.filter {
        case (value, i) => f(value, i)
      }

    def isEmpty: Boolean = arr.value.isEmpty
  }

  implicit class RichOptionalJsArray(arr: Option[JsArray]) {

    def mapWithIndex[T](f: (JsValue, Index) => Option[T]): Seq[T] =
      arr
        .map {
          _.zipWithIndex.flatMap {
            case (value, i) => f(value, i)
          }
        }
        .getOrElse(Nil)

    def validate[T](implicit rds: Reads[T]): Option[T] =
      arr.flatMap(_.validate[T].asOpt)

    def length: Int = arr.getOrElse(JsArray()).value.length

  }

  def getAnswersAndBuildSectionRows(section: Section[JsArray])(f: Index => Option[SummaryListRow]): Seq[SummaryListRow] =
    userAnswers
      .get(section)
      .mapWithIndex {
        (_, index) => f(index)
      }
}
