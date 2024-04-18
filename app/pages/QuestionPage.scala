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

package pages

import play.api.libs.json.{__, Reads}
import queries.{Gettable, Settable}
import utils.transformers.SequenceNumber

trait QuestionPage[A] extends Page with Gettable[A] with Settable[A]

trait DiscrepancyQuestionPage[A, B, C] extends QuestionPage[A] {

  def valueInIE043(ie043: B, sequenceNumber: BigInt): Option[C]

  /** @param f converts from A (type in user answers) to C (type in IE043)
    * @param reads reads the value from user answers
    * @return a reads of a defined `Option` if there is a discrepancy and an undefined `Option` if there is not
    */
  def readNullable(f: A => C)(implicit reads: Reads[A]): B => Reads[Option[C]] = ie043 => {
    for {
      sequenceNumber   <- (__ \ SequenceNumber).readNullable[BigInt]
      userAnswersValue <- (__ \ this.toString).readNullable[A].map(_.map(f))
    } yield sequenceNumber match {
      case Some(sequenceNumber) =>
        val ie043Value = valueInIE043(ie043, sequenceNumber)
        (ie043Value, userAnswersValue) match {
          case (None, Some(_)) =>
            // the data item did not exist in IE043 and has been added as a discrepancy
            userAnswersValue
          case (Some(value1), Some(value2)) if value1 != value2 =>
            // the data item did exist in IE043 and has been changed as a discrepancy
            userAnswersValue
          case _ =>
            // no discrepancy to report for this data item
            None
        }
      case _ =>
        // a lack of sequence number implies the data item did not exist in IE043 and has been added as a discrepancy
        userAnswersValue
    }
  }
}
