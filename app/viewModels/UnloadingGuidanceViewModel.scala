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
                                      linkText: String,
                                      postLinkText: String,
                                      para1: Option[String],
                                      para2: Option[String],
                                      para3: Option[Para3]
) {
  val prefix = "unloadingGuidance"
}

case class Para3(preLinkText: String, linkText: String, postLinkText: String)

object Para3 {
  def apply(prefix: String): Para3 = new Para3(s"$prefix.para3.preLinkText", s"$prefix.para3.linkText", s"$prefix.para3.postLinkText")
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

      val preLinkText: String = s"$prefix.preLinkText"

      val linkText: String = (newAuth, goodsTooLarge) match {
        case (true, Some(false)) => s"$prefix.pdf.midSentence.link"
        case _                   => s"$prefix.pdf.link"
      }

      val postLinkText: String = s"$prefix.postLinkText"

      val para1: Option[String] = Option.when(newAuth && goodsTooLarge.contains(false))(s"$prefix.para1")

      val para2: Option[String] = (newAuth, goodsTooLarge) match {
        case (false, _)          => Some(s"$prefix.para2.notNewAuth")
        case (true, Some(false)) => None
        case _                   => Some(s"$prefix.para2.newAuth.goodsTooLargeYes")
      }

      val para3: Option[Para3] = Option.when(newAuth && goodsTooLarge.contains(false))(Para3(prefix))

      UnloadingGuidanceViewModel(title, heading, preLinkText, linkText, postLinkText, para1, para2, para3)

    }

  }
}
