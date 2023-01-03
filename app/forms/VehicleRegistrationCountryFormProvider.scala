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

package forms

import javax.inject.Inject
import forms.mappings.Mappings
import models.reference.Country
import play.api.data.Form

class VehicleRegistrationCountryFormProvider @Inject() extends Mappings {

  def apply(countries: Seq[Country]): Form[Country] =
    Form(
      "value" -> text("vehicleRegistrationCountry.error.required")
        .verifying("vehicleRegistrationCountry.error.required", value => countries.exists(_.code == value))
        .transform[Country](value => countries.find(_.code == value).get, _.code)
    )
}
