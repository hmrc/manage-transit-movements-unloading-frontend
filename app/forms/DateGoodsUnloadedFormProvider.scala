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

import java.time.{Clock, LocalDate}
import forms.mappings.Mappings

import javax.inject.Inject
import play.api.data.Form
import utils.Format.cyaDateFormatter

class DateGoodsUnloadedFormProvider @Inject() (clock: Clock) extends Mappings {

  def today: LocalDate = LocalDate.now(clock)

  def apply(dateOfPrep: LocalDate): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey = "dateGoodsUnloaded.error.invalid",
        allRequiredKey = "dateGoodsUnloaded.error.required.all",
        twoRequiredKey = "dateGoodsUnloaded.error.required.two",
        requiredKey = "dateGoodsUnloaded.error.required"
      ).verifying(
        minDate(dateOfPrep, "dateGoodsUnloaded.error.min.date", dateOfPrep.format(cyaDateFormatter)),
        maxDate(today, "dateGoodsUnloaded.error.max.date")
      )
    )
}
