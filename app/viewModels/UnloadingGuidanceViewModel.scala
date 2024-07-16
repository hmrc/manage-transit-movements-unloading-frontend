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

case class UnloadingGuidanceViewModel(newAuth: Boolean, goodsTooLarge: Option[Boolean]) {

  val prefix = "unloadingGuidance"

  def title(): String = (newAuth, goodsTooLarge) match {
    case (false, _)          => s"$prefix.notNewAuth.title"
    case (true, Some(false)) => s"$prefix.newAuth.goodsTooLargeNo.title"
    case _                   => s"$prefix.newAuth.goodsTooLargeYes.title"
  }

  def heading(): String = (newAuth, goodsTooLarge) match {
    case (false, _)          => s"$prefix.notNewAuth.heading"
    case (true, Some(false)) => s"$prefix.newAuth.goodsTooLargeNo.heading"
    case _                   => s"$prefix.newAuth.goodsTooLargeYes.heading"
  }

  def preLinkText(): String = (newAuth, goodsTooLarge) match {
    case (true, Some(false)) => s"$prefix.preLinkText"
    case _                   => ""
  }

  def postLinkText(): String = (newAuth, goodsTooLarge) match {
    case (true, Some(false)) => s"$prefix.postLinkText"
    case _                   => ""
  }

  def para1(): String =
    if (newAuth && goodsTooLarge.get) {
      s"$prefix.para1"
    } else {
      ""
    }

  def para2(): String = (newAuth, goodsTooLarge) match {
    case (false, _)          => s"$prefix.para2.notNewAuth"
    case (true, Some(false)) => ""
    case _                   => s"$prefix.para2.newAuth.goodsTooLargeYes"
  }

  val para3preLinkText: String  = s"$prefix.para3.preLinkText"
  val para3linkText: String     = s"$prefix.para3.linkText"
  val para3postlinkText: String = s"$prefix.para3.postLinkText"
  val pdfLinkText: String       = s"$prefix.pdf.link"
}

object UnloadingGuidanceViewModel {

  class UnloadingGuidanceViewModelProvider @Inject() () {

    def apply(newAuth: Boolean, goodsTooLarge: Option[Boolean]): UnloadingGuidanceViewModel =
      new UnloadingGuidanceViewModel(newAuth, goodsTooLarge)
  }

}
