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

package utils.answersHelpers

import models.{ArrivalId, Index, UserAnswers}
import pages._
import pages.sections.Section
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.html.components.{Content, SummaryListRow}

class AnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) extends SummaryListRowHelper {

  def arrivalId: ArrivalId = userAnswers.id

  def sequenceNumber(jsValue: JsValue): Option[SummaryListRow] =
    jsValue.transform((__ \ "sequenceNumber").json.pick[JsString]).asOpt.map {
      case JsString(value) =>
        buildRowWithNoChangeLink(
          prefix = messages("unloadingFindings.sequenceNumber"),
          answer = value.toText
        )
    }

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

  def getAnswerAndBuildRowWithRemove[T](
    page: QuestionPage[T],
    formatAnswer: T => Content,
    prefix: String,
    id: String,
    change: Call,
    remove: Call,
    hiddenLink: String,
    href: String,
    args: Any*
  )(implicit rds: Reads[T]): SummaryListRow =
    (userAnswers.get(page) map {
      answer =>
        buildRemovableRow(
          prefix = prefix,
          answer = formatAnswer(answer),
          id = id,
          changeCall = change,
          removeCall = remove,
          args = args: _*
        )
    }).getOrElse(
      buildRow(
        prefix = prefix,
        answer = formatAsLink(messages(s"$hiddenLink.add.visuallyHidden"), href),
        id = None,
        call = None
      )
    )

  def buildRowWithNoChangeLink[T](
    data: Option[T],
    formatAnswer: T => Content,
    prefix: String,
    args: Any*
  ): Option[SummaryListRow] =
    data map {
      answer =>
        buildRow(
          prefix = prefix,
          answer = formatAnswer(answer),
          id = None,
          call = None,
          args = args: _*
        )
    }

  def getAnswerAndBuildRowWithoutKey[T](
    page: QuestionPage[T],
    formatAnswer: T => Content,
    prefix: String,
    id: Option[String],
    call: Option[Call],
    args: Any*
  )(implicit rds: Reads[T]): Option[SummaryListRow] =
    userAnswers.get(page) map {
      answer =>
        buildRowWithoutKey(
          prefix = prefix,
          answer = formatAnswer(answer),
          id = id,
          call = call,
          args = args: _*
        )
    }

  implicit class RichJsArray(arr: JsArray) {

    def zipWithIndex: List[(JsValue, Index)] = arr.value.toList.zipWithIndex.map(
      x => (x._1, Index(x._2))
    )

    def isEmpty: Boolean = arr.value.isEmpty
  }

  implicit class RichOptionalJsArray(arr: Option[JsArray]) {

    def mapWithIndex[T](f: (JsValue, Index) => T): Seq[T] =
      arr
        .map {
          _.zipWithIndex.map {
            case (value, i) => f(value, i)
          }
        }
        .getOrElse(Nil)

    def flatMapWithIndex[T](f: (JsValue, Index) => Option[T]): Seq[T] =
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
      .flatMapWithIndex {
        (_, index) => f(index)
      }
}
