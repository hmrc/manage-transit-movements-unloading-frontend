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

import play.twirl.api.HtmlFormat
import viewModels.sections.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import views.html.CheckYourAnswersView

class CheckYourAnswersViewSpec extends CheckYourAnswersViewBehaviours {

  override val prefix: String = "checkYourAnswers"

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable =
    injector.instanceOf[CheckYourAnswersView].apply(mrn, arrivalId, sections)(fakeRequest, messages)

  behave like pageWithTitle()

  behave like pageWithBackLink

  behave like pageWithCaption(mrn.toString)

  behave like pageWithHeading()

  behave like pageWithCheckYourAnswers()

  behave like pageWithContent("h2", "Now send your unloading remarks")

  behave like pageWithContent("p", "By sending this you are confirming that the details you are providing are correct, to the best of your knowledge.")

  behave like pageWithSubmitButton("Confirm and send")

}
