package views.$package$

import forms.DateFormProvider
import models.NormalMode
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.DateInputViewBehaviours
import views.html.$package$.$className$View

import java.time.LocalDate

class $className$ViewSpec extends DateInputViewBehaviours {

  private val minDate = arbitrary[LocalDate].sample.value
  private val maxDate = arbitrary[LocalDate].sample.value

  override def form: Form[LocalDate] = new DateFormProvider()(prefix, minDate, maxDate)

  override def applyView(form: Form[LocalDate]): HtmlFormat.Appendable =
    injector.instanceOf[$className$View].apply(form, lrn, NormalMode)(fakeRequest, messages)

  override val prefix: String = "$package$.$className;format="decap"$"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithDateInput

  behave like pageWithSubmitButton("Save and continue")
}
