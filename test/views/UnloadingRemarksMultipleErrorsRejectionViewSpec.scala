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

import _root_.utils.UnloadingRemarksRejectionHelper._
import generators.Generators
import models.FunctionalError
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.behaviours.SummaryListViewBehaviours
import views.html.UnloadingRemarksMultipleErrorsRejectionView

class UnloadingRemarksMultipleErrorsRejectionViewSpec extends SummaryListViewBehaviours with Generators {

  private val contactUrl = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/new-computerised-transit-system-enquiries"

  override val prefix: String                        = "unloadingRemarksRejection"
  private val functionalErrors: Seq[FunctionalError] = listWithMaxLength[FunctionalError]()(arbitraryRejectionErrorNonDefaultPointer).sample.value

  override def summaryLists: Seq[SummaryList] = functionalErrors.map(_.toSummaryList)

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[UnloadingRemarksMultipleErrorsRejectionView].apply(arrivalId, functionalErrors)(fakeRequest, messages)

  behave like pageWithBackLink

  behave like pageWithHeading()

  behave like pageWithSummaryLists()

  behave like pageWithPartialContent("p", "You must review the errors and")
  behave like pageWithLink(
    "review",
    "send new unloading remarks with the right information",
    controllers.routes.IndexController.newUnloadingRemarks(arrivalId).url
  )

  behave like pageWithPartialContent("p", "You can ")
  behave like pageWithLink(
    "contact",
    "contact the New Computerised Transit System helpdesk if you need help understanding the errors (opens in a new tab)",
    contactUrl
  )
}
