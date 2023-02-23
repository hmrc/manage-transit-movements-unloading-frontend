package forms

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import models.reference.$referenceClass$
import models.$referenceListClass$

class $formProvider$ @Inject() extends Mappings {

  def apply(prefix: String, $referenceClassPlural;format="decap"$: $referenceListClass$): Form[$referenceClass$] =

    Form(
      "value" -> text(s"\$prefix.error.required")
        .verifying(s"\$prefix.error.required", value => $referenceClassPlural;format="decap"$.getAll.exists(_.id == value))
        .transform[$referenceClass$](value => $referenceClassPlural;format="decap"$.get$referenceClass$(value).get, _.id)
    )
}
