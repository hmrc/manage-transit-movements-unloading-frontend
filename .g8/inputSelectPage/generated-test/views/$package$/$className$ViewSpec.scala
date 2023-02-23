package views.$package$

import forms.$formProvider$
import views.behaviours.InputSelectViewBehaviours
import models.NormalMode
import models.reference.$referenceClass$
import models.$referenceListClass$
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.$package$.$className$View

class $className$ViewSpec extends InputSelectViewBehaviours[$referenceClass$] {

  override def form: Form[$referenceClass$] = new $formProvider$()(prefix, $referenceListClass$(values))

  override def applyView(form: Form[$referenceClass$]): HtmlFormat.Appendable =
    injector.instanceOf[$className$View].apply(form, lrn, values, NormalMode)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[$referenceClass$] = arbitrary$referenceClass$

  override val prefix: String = "$package$.$className;format="decap"$"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithSelect()

  behave like pageWithHint("$title$ hint")

  behave like pageWithContent("label", "$title$ label")

  behave like pageWithSubmitButton("Save and continue")
}
