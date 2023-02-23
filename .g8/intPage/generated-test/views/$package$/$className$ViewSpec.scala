package views.$package$

import forms.$formProvider$
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import views.behaviours.InputTextViewBehaviours
import views.html.$package$.$className$View
import org.scalacheck.{Arbitrary, Gen}

class $className$ViewSpec extends InputTextViewBehaviours[Int] {

  override def form: Form[Int] = new $formProvider$()(prefix, 10)

  override def applyView(form: Form[Int]): HtmlFormat.Appendable =
    injector.instanceOf[$className$View].apply(form, lrn, NormalMode)(fakeRequest, messages)

  private val maxInt = $maximum$

  implicit override val arbitraryT: Arbitrary[Int] = Arbitrary(maxInt)

  override val prefix: String = "$package$.$className;format="decap"$"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithoutHint()

  behave like pageWithInputText(Some(InputSize.Width20))

  behave like pageWithSubmitButton("Save and continue")
}
