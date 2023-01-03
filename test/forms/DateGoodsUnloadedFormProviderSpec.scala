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

import java.time.{Clock, LocalDate, ZoneOffset}
import forms.behaviours.DateBehaviours
import play.api.data.FormError

class DateGoodsUnloadedFormProviderSpec extends DateBehaviours {

  val minDate         = LocalDate.of(2020, 12, 31)
  val maxDate         = LocalDate.now(ZoneOffset.UTC)
  val minDateAsString = "31 December 2020"
  val form            = new DateGoodsUnloadedFormProvider(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))(minDate)

  ".value" - {

    val validData = datesBetween(
      min = minDate,
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "dateGoodsUnloaded.error.required.all")

    behave like dateFieldWithMin(form, "value", min = minDate, FormError("value", "dateGoodsUnloaded.error.min.date", Seq(minDateAsString)))

    behave like dateFieldWithMax(form, "value", max = maxDate, FormError("value", "dateGoodsUnloaded.error.max.date"))

  }
}
