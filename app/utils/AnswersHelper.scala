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
import pages.QuestionPage
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, Reads}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.html.components._

class AnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) extends SummaryListRowHelper {

  def arrivalId: ArrivalId = userAnswers.id
  def itemsIndex: Index    = Index(0)

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

  def getAnswerAndBuildRowFromPath[T](
    path: JsPath,
    formatAnswer: T => Content,
    prefix: String,
    id: Option[String],
    call: Option[Call],
    args: Any*
  )(implicit rds: Reads[T]): Option[SummaryListRow] =
    userAnswers.getIE043(path) map {
      answer =>
        buildRowFromPath(
          prefix = prefix,
          answer = formatAnswer(answer),
          id = id,
          call = call,
          args = args: _*
        )
    }

  def getAnswerAndBuildRowFromPathWithDynamicPrefix[T](
    path: JsPath,
    formatAnswer: T => Content,
    dynamicPrefix: T => String,
    id: Option[String],
    call: Option[Call],
    args: Any*
  )(implicit rds: Reads[T]): Option[SummaryListRow] =
    userAnswers.getIE043(path) map {
      answer =>
        buildRowFromPath(
          prefix = dynamicPrefix(answer),
          answer = formatAnswer(answer),
          id = id,
          call = call,
          args = args: _*
        )
    }

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
}
