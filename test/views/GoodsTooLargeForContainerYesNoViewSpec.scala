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

package views

import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.GoodsTooLargeForContainerYesNoView

class GoodsTooLargeForContainerYesNoViewSpec extends YesNoViewBehaviours {

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[GoodsTooLargeForContainerYesNoView].apply(form, mrn, arrivalId, messageId, NormalMode)(fakeRequest, messages)

  override val prefix: String = "goodsTooLargeForContainerYesNo"

  behave like pageWithTitle("Are the goods too large to fit into a container?")

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading()

  behave like pageWithContent(
    "p",
    "These could be goods on flatbed trucks that are too large to seal. Or vehicles that were driven to their destination, like motorised cranes or boats with sails."
  )

  behave like pageWithRadioItems()
}
