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

import forms.mappings.Mappings
import play.api.data.Form
import services.DateTimeService
import utils.Format.cyaDateFormatter

import java.time.LocalDate
import javax.inject.Inject

class DateGoodsUnloadedFormProvider @Inject() (dateTimeService: DateTimeService) extends Mappings {

  def apply(dateOfPrep: LocalDate): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey = "dateGoodsUnloaded.error.invalid",
        requiredKey = "dateGoodsUnloaded.error.required"
      ).verifying(
        minDate(dateOfPrep, "dateGoodsUnloaded.error.min.date", dateOfPrep.format(cyaDateFormatter)),
        maxDate(dateTimeService.currentDate, "dateGoodsUnloaded.error.max.date")
      )
    )
}
