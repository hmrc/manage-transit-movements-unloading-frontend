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

package views

import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.UnloadingGuidanceView

class UnloadingGuidanceViewSpec extends ViewBehaviours {

  private val pdfUrl      = arbitrary[String].sample.value
  private val nextPageUrl = arbitrary[String].sample.value

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[UnloadingGuidanceView].apply(mrn, pdfUrl, nextPageUrl)(fakeRequest, messages)

  override val prefix: String = "unloadingGuidance"

  behave like pageWithBackLink

  behave like pageWithHeading()

  behave like pageWithContent("p", "You can:")

  behave like pageWithList(
    "govuk-list--bullet",
    "view and print the unloading permissions accompanying document",
    s"check that the unloaded goods match the information in the transit declaration for movement reference $mrn"
  )

  behave like pageWithLink(
    id = "download",
    expectedText = "Download the Unloading Permission PDF",
    expectedHref = pdfUrl
  )

  behave like pageWithButton("Continue") {
    button =>
      assertElementContainsId(button, "submit")
      assertElementContainsHref(button, nextPageUrl)
  }
}
