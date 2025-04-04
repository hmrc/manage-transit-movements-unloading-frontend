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

import generators.Generators
import models.Procedure
import play.twirl.api.HtmlFormat
import viewModels.CheckYourAnswersViewModel
import viewModels.sections.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import views.html.CheckYourAnswersView

class CheckYourAnswersViewSpec extends CheckYourAnswersViewBehaviours with Generators {

  override val prefix: String = "checkYourAnswers"

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable = {
    val viewModel = new CheckYourAnswersViewModel(sections.head, sections.tail, false, Procedure.Unrevised)
    injector.instanceOf[CheckYourAnswersView].apply(mrn, arrivalId, viewModel)(fakeRequest, messages)
  }

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading()

  behave like pageWithCheckYourAnswers()

  behave like pageWithoutWarningText()

  behave like pageWithFormAction(controllers.routes.CheckYourAnswersController.onSubmit(arrivalId).url)

  behave like pageWithSubmitButton("Confirm and send")

  "must render link for discrepancies when showDiscrepanciesLink is true" - {

    val viewModel: CheckYourAnswersViewModel =
      new CheckYourAnswersViewModel(sections.head, sections.tail, true, Procedure.Unrevised)

    val view: HtmlFormat.Appendable =
      injector.instanceOf[CheckYourAnswersView].apply(mrn, arrivalId, viewModel)(fakeRequest, messages)

    val doc = parseView(view)

    behave like pageWithLink(
      doc,
      "unloadingFindings",
      "Back to discrepancies between the transit and unloading permission",
      controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId).url
    )
  }

  "must render correct content" - {

    "when procedure is Unrevised" - {
      val viewModel =
        new CheckYourAnswersViewModel(sections.head, sections.tail, false, Procedure.Unrevised)

      val view: HtmlFormat.Appendable =
        injector.instanceOf[CheckYourAnswersView].apply(mrn, arrivalId, viewModel)(fakeRequest, messages)

      val doc = parseView(view)

      behave like pageWithoutWarningText(doc)

      behave like pageWithContent(
        doc,
        "p",
        "By sending this, you are confirming that these details are correct to the best of your knowledge."
      )
    }

    "when procedure is CannotUseRevisedDueToDiscrepancies" - {
      val viewModel =
        new CheckYourAnswersViewModel(sections.head, sections.tail, false, Procedure.CannotUseRevisedDueToDiscrepancies)

      val view: HtmlFormat.Appendable =
        injector.instanceOf[CheckYourAnswersView].apply(mrn, arrivalId, viewModel)(fakeRequest, messages)

      val doc = parseView(view)

      behave like pageWithWarningText(
        doc,
        "Based on the answers you’ve given us, you cannot use the revised unloading procedure"
      )

      behave like pageWithContent(
        doc,
        "p",
        "By sending this, you are confirming that these details are correct to the best of your knowledge."
      )
    }

    "when procedure is CannotUseRevisedDueToConditions" - {
      val viewModel =
        new CheckYourAnswersViewModel(sections.head, sections.tail, false, Procedure.CannotUseRevisedDueToConditions)

      val view: HtmlFormat.Appendable =
        injector.instanceOf[CheckYourAnswersView].apply(mrn, arrivalId, viewModel)(fakeRequest, messages)

      val doc = parseView(view)

      behave like pageWithWarningText(
        doc,
        "Based on the answers you’ve given us, you cannot use the revised unloading procedure"
      )

      behave like pageWithContent(
        doc,
        "p",
        "By sending this, you are confirming that these details are correct to the best of your knowledge."
      )
    }

    "when procedure is RevisedAndGoodsTooLarge" - {
      val viewModel =
        new CheckYourAnswersViewModel(sections.head, sections.tail, false, Procedure.RevisedAndGoodsTooLarge)

      val view: HtmlFormat.Appendable =
        injector.instanceOf[CheckYourAnswersView].apply(mrn, arrivalId, viewModel)(fakeRequest, messages)

      val doc = parseView(view)

      behave like pageWithoutWarningText(doc)

      behave like pageWithContent(
        doc,
        "p",
        "By sending this, you are confirming:"
      )

      behave like pageWithList(
        doc,
        "govuk-list--bullet",
        "these details are correct to the best of your knowledge",
        "you performed visual checks of the goods",
        "there were no discrepancies between the transit and unloading permission"
      )
    }

    "when procedure is RevisedAndGoodsNotTooLarge" - {
      val viewModel =
        new CheckYourAnswersViewModel(sections.head, sections.tail, false, Procedure.RevisedAndGoodsNotTooLarge)

      val view: HtmlFormat.Appendable =
        injector.instanceOf[CheckYourAnswersView].apply(mrn, arrivalId, viewModel)(fakeRequest, messages)

      val doc = parseView(view)

      behave like pageWithContent(
        doc,
        "p",
        "By sending this, you are confirming:"
      )

      behave like pageWithoutWarningText(doc)

      behave like pageWithList(
        doc,
        "govuk-list--bullet",
        "these details are correct to the best of your knowledge",
        "you did not unload the goods",
        "you did not check the goods",
        "there was no evidence of discrepancies between the transit and unloading permission"
      )
    }
  }
}
