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

package viewModels

import cats.data.NonEmptyList
import models.reference.Country
import models.{GoodsItem, Index, UserAnswers}
import pages.{NewSealNumberPage, QuestionPage}
import uk.gov.hmrc.viewmodels.SummaryList.Row

object SummaryRow {

  type StandardRow    = Option[String] => Option[String] => (String => Row) => Seq[Row]
  type StandardRowInt = Option[Int] => Option[Int] => (Int => Row) => Seq[Row]
  type RowWithIndex   = Index => Option[String] => String => ((Index, String) => Row) => Row
  type SealRows       = Seq[String] => UserAnswers => ((Index, String) => Row) => Seq[Row]
  type GoodsItemRows  = NonEmptyList[GoodsItem] => UserAnswers => ((Index, String) => Row) => NonEmptyList[Row]
  type GoodsItemRow   = Index => Option[String] => GoodsItem => ((Index, String) => Row) => Row

  type UserAnswerString  = UserAnswers => QuestionPage[String] => Option[String]
  type UserAnswerInt     = UserAnswers => QuestionPage[Int] => Option[Int]
  type UserAnswerCountry = UserAnswers => QuestionPage[Country] => Option[String]
  type UserAnswerSeals   = UserAnswers => NewSealNumberPage.type => Option[String]

  val userAnswerString: UserAnswerString = {
    ua => page =>
      ua.get(page)
  }

  val userAnswerInt: UserAnswerInt = {
    ua => page =>
      ua.get(page)
  }

  val userAnswerCountry: UserAnswerCountry = {
    ua => page =>
      ua.get(page) match {
        case Some(x) => Some(x.description)
        case None    => None
      }
  }

  val userAnswerWithIndex: Index => UserAnswerSeals = {
    index => ua => page =>
      ua.get(page(index))
  }

  val row: StandardRow =
    userAnswer =>
      summaryValue =>
        buildRow => {
          (userAnswer, summaryValue) match {
            case (Some(x), _)    => Seq(buildRow(x))
            case (None, Some(x)) => Seq(buildRow(x))
            case (_, _)          => Nil
          }
        }

  val rowInt: StandardRowInt =
    userAnswer =>
      summaryValue =>
        buildRow => {
          (userAnswer, summaryValue) match {
            case (Some(x), _)    => Seq(buildRow(x))
            case (None, Some(x)) => Seq(buildRow(x))
            case (_, _)          => Nil
          }
        }

  val rowWithIndex: RowWithIndex =
    index =>
      userAnswer =>
        summaryValue =>
          buildRow => {
            (userAnswer, summaryValue) match {
              case (Some(x), _) => buildRow(index, x)
              case (None, x)    => buildRow(index, x)
            }
          }

  val rowSeals: SealRows =
    sequence =>
      userAnswers =>
        buildRow =>
          sequence.zipWithIndex.map {
            unloadingPermissionValue =>
              val sealAnswer = SummaryRow.userAnswerWithIndex(Index(unloadingPermissionValue._2))(userAnswers)(NewSealNumberPage)
              SummaryRow.rowWithIndex(Index(unloadingPermissionValue._2))(sealAnswer)(unloadingPermissionValue._1)(buildRow)
          }

  val rowGoodsItemWithIndex: GoodsItemRow =
    index =>
      userAnswer =>
        summaryValue =>
          buildRow => {
            (userAnswer, summaryValue) match {
              case (Some(x), _) => buildRow(index, x)
              case (None, x)    => buildRow(index, x.description)
            }
          }

  val rowGoodsItems: GoodsItemRows =
    sequence =>
      _ =>
        buildRow =>
          sequence.zipWithIndex.map {
            unloadingPermissionValue =>
              val answer = None //TODO: Call get on UserAnswers when this is available
              SummaryRow.rowGoodsItemWithIndex(Index(unloadingPermissionValue._2))(answer)(unloadingPermissionValue._1)(buildRow)
          }
}
