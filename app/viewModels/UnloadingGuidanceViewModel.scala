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

import javax.inject.Inject

case class UnloadingGuidanceViewModel(title: String,
                                      heading: String,
                                      preLinkText: String,
                                      postLinkText: String,
                                      para1: Option[String],
                                      para2: String,
                                      para3: Option[Para3.type]
) {
  val prefix              = "unloadingGuidance"
  val pdfLinkText: String = s"$prefix.pdf.link"
}

case object Para3 {
  val prefix                    = "unloadingGuidance"
  val para3preLinkText: String  = s"$prefix.para3.preLinkText"
  val para3linkText: String     = s"$prefix.para3.linkText"
  val para3postlinkText: String = s"$prefix.para3.postLinkText"
}

object UnloadingGuidanceViewModel {

  class UnloadingGuidanceViewModelProvider @Inject() () {

    def apply(newAuth: Boolean, goodsTooLarge: Option[Boolean]): UnloadingGuidanceViewModel = {
      val prefix = "unloadingGuidance"

      def dynamicText(text: String): String = (newAuth, goodsTooLarge) match {
        case (false, _)          => s"$prefix.notNewAuth.$text"
        case (true, Some(false)) => s"$prefix.newAuth.goodsTooLargeNo.$text"
        case _                   => s"$prefix.newAuth.goodsTooLargeYes.$text"
      }
      val title   = dynamicText("title")
      val heading = dynamicText("heading")

      def preLinkText: String = (newAuth, goodsTooLarge) match {
        case (true, Some(false)) => s"$prefix.preLinkText"
        case _                   => ""
      }

      def postLinkText(): String = (newAuth, goodsTooLarge) match {
        case (true, Some(false)) => s"$prefix.postLinkText"
        case _                   => ""
      }

      def para1: Option[String] = Option.when(newAuth && goodsTooLarge.contains(false))(s"$prefix.para1")

      def para2: String = (newAuth, goodsTooLarge) match {
        case (false, _)          => s"$prefix.para2.notNewAuth"
        case (true, Some(false)) => ""
        case _                   => s"$prefix.para2.newAuth.goodsTooLargeYes"
      }

      def para3: Option[Para3.type] = Option.when(newAuth && goodsTooLarge.contains(false))(Para3)

      UnloadingGuidanceViewModel(title, heading, preLinkText, postLinkText(), para1, para2, para3)

    }

  }
}
