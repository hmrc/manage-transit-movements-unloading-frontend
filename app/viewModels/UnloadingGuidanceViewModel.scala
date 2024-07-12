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

case class UnloadingGuidanceViewModel() {

  val prefix = "unloadingGuidance"

  def title(newAuth: Boolean, goodsTooLarge: Boolean): String = (newAuth, goodsTooLarge) match {
    case (false, _)    => s"$prefix.notNewAuth.title"
    case (true, false) => s"$prefix.newAuth.goodsTooLargeNo.title"
    case (true, true)  => s"$prefix.newAuth.goodsTooLargeYes.title"
  }

  def heading(newAuth: Boolean, goodsTooLarge: Boolean): String = (newAuth, goodsTooLarge) match {
    case (false, _)    => s"$prefix.notNewAuth.heading"
    case (true, false) => s"$prefix.newAuth.goodsTooLargeNo.heading"
    case (true, true)  => s"$prefix.newAuth.goodsTooLargeYes.heading"
  }

  def preLinkText(newAuth: Boolean, goodsTooLarge: Boolean): String = (newAuth, goodsTooLarge) match {
    case (true, false) => s"$prefix.preLinkText"
    case (false, _)    => ""
    case (true, true)  => ""
  }

  def postLinkText(newAuth: Boolean, goodsTooLarge: Boolean): String = (newAuth, goodsTooLarge) match {
    case (true, false) => s"$prefix.postLinkText"
    case (false, _)    => ""
    case (true, true)  => ""
  }

  def para1(newAuth: Boolean, goodsTooLarge: Boolean): String =
    if (newAuth && goodsTooLarge) {
      s"$prefix.para1"
    } else {
      ""
    }

  def para2(newAuth: Boolean, goodsTooLarge: Boolean): String = (newAuth, goodsTooLarge) match {
    case (false, _)    => s"$prefix.para2.notNewAuth"
    case (true, false) => ""
    case (true, true)  => s"$prefix.para2.newAuth.goodsTooLargeYes"
  }
}

object UnloadingGuidanceViewModel {

  class UnloadingGuidanceViewModelProvider @Inject() () {

    def apply(): UnloadingGuidanceViewModel =
      new UnloadingGuidanceViewModel()
  }

}
