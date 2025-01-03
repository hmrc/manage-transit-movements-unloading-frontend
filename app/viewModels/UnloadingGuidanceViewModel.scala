/*
 * Copyright 2024 HM Revenue & Customs
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

import models.Procedure.RevisedAndGoodsTooLarge
import models.{Procedure, UserAnswers}

import javax.inject.Inject

case class UnloadingGuidanceViewModel(
  title: String,
  heading: String,
  preLinkText: Option[String],
  linkText: String,
  postLinkText: Option[String],
  para1: Option[String],
  para2: Option[String],
  para3: Option[Para3]
)

case class Para3(preLinkText: String, linkText: String, postLinkText: String)

object Para3 {
  def apply(prefix: String): Para3 = new Para3(s"$prefix.para3.preLinkText", s"$prefix.para3.linkText", s"$prefix.para3.postLinkText")
}

object UnloadingGuidanceViewModel {

  class UnloadingGuidanceViewModelProvider @Inject() {

    def apply(userAnswers: UserAnswers): UnloadingGuidanceViewModel = {
      val procedure = Procedure(userAnswers)
      val prefix    = "unloadingGuidance"

      def dynamicText(text: String): String = procedure match {
        case Procedure.RevisedAndGoodsNotTooLarge => s"$prefix.newAuth.goodsTooLargeNo.$text"
        case Procedure.RevisedAndGoodsTooLarge    => s"$prefix.newAuth.goodsTooLargeYes.$text"
        case _                                    => s"$prefix.notNewAuth.$text"
      }

      val title   = dynamicText("title")
      val heading = dynamicText("heading")

      val preLinkText: Option[String] = procedure match {
        case Procedure.RevisedAndGoodsNotTooLarge => Some(s"$prefix.preLinkText")
        case _                                    => None
      }

      val linkText: String = procedure match {
        case Procedure.RevisedAndGoodsNotTooLarge => s"$prefix.pdf.midSentence.link"
        case _                                    => s"$prefix.pdf.link"
      }

      val postLinkText: Option[String] = procedure match {
        case Procedure.RevisedAndGoodsNotTooLarge => Some(s"$prefix.postLinkText")
        case _                                    => None
      }

      val para1: Option[String] = procedure match {
        case RevisedAndGoodsTooLarge => Some(s"$prefix.para1")
        case _                       => None
      }

      val para2: Option[String] = procedure match {
        case Procedure.RevisedAndGoodsNotTooLarge => None
        case Procedure.RevisedAndGoodsTooLarge    => Some(s"$prefix.para2.newAuth.goodsTooLargeYes")
        case _                                    => Some(s"$prefix.para2.notNewAuth")
      }

      val para3: Option[Para3] = procedure match {
        case Procedure.RevisedAndGoodsNotTooLarge => Some(Para3(prefix))
        case _                                    => None
      }

      UnloadingGuidanceViewModel(title, heading, preLinkText, linkText, postLinkText, para1, para2, para3)
    }
  }
}
