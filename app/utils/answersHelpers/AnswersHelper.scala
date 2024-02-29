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

  def getAnswersAndBuildSectionRows(section: Section[JsArray])(f: (Index, Index) => Option[SummaryListRow]): Seq[SummaryListRow] =
    userAnswers
      .get(section)
      .mapWithIndex {
        (_, index, displayIndex) => f(index, displayIndex)
      }
      .flatten
}
