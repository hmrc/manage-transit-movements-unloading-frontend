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

package viewModels

import base.SpecBase
import matchers.JsonMatchers._
import controllers.routes
import generators.MessagesModelGenerators
import models.ErrorType.NotSupportedPosition
import models.{DefaultPointer, FunctionalError}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class UnloadingRemarksRejectionViewModelSpec extends SpecBase with MessagesModelGenerators with ScalaCheckPropertyChecks {

  "UnloadingRemarksRejectionViewModel" - {

    "must return single error page for one error" in {

      forAll(arbitrary[FunctionalError](arbitraryRejectionErrorNonDefaultPointer)) {
        error =>
          val data: UnloadingRemarksRejectionViewModel =
            UnloadingRemarksRejectionViewModel(Seq(error), arrivalId, "url")(messages).get

          data.page mustBe "unloadingRemarksRejection.njk"
      }
    }

    "must return multiple error page if there are more than one errors" in {

      val error  = arbitrary[FunctionalError](arbitraryRejectionError).sample.value
      val errors = Seq(error, error)

      val data: UnloadingRemarksRejectionViewModel =
        UnloadingRemarksRejectionViewModel(errors, arrivalId, "url")(messages).get

      val expectedJson =
        Json.obj(
          "errors"                     -> errors,
          "contactUrl"                 -> "url",
          "declareUnloadingRemarksUrl" -> routes.IndexController.onPageLoad(arrivalId).url
        )

      data.page mustBe "unloadingRemarksMultipleErrorsRejection.njk"
      data.json must containJson(expectedJson)
    }

    "must return error page when single DefaultPointer errors exist" in {

      val error  = arbitrary[FunctionalError](arbitraryRejectionError).sample.value.copy(pointer = DefaultPointer("error here"))
      val errors = Seq(error)

      val data: UnloadingRemarksRejectionViewModel =
        UnloadingRemarksRejectionViewModel(errors, arrivalId, "url")(messages).get

      val expectedJson =
        Json.obj(
          "errors"                     -> errors,
          "contactUrl"                 -> "url",
          "declareUnloadingRemarksUrl" -> routes.IndexController.onPageLoad(arrivalId).url
        )

      data.page mustBe "unloadingRemarksMultipleErrorsRejection.njk"
      data.json must containJson(expectedJson)
    }

    "must return error page when multiple DefaultPointer errors exist" in {

      val error  = arbitrary[FunctionalError](arbitraryRejectionError).sample.value.copy(pointer = DefaultPointer("error here"))
      val errors = Seq(error, error)

      val data: UnloadingRemarksRejectionViewModel =
        UnloadingRemarksRejectionViewModel(errors, arrivalId, "url")(messages).get

      val expectedJson =
        Json.obj(
          "errors"                     -> errors,
          "contactUrl"                 -> "url",
          "declareUnloadingRemarksUrl" -> routes.IndexController.onPageLoad(arrivalId).url
        )

      data.page mustBe "unloadingRemarksMultipleErrorsRejection.njk"
      data.json must containJson(expectedJson)
    }

    "must return error page when originalAttributeValue is None" in {

      val error  = FunctionalError(NotSupportedPosition, DefaultPointer("error"), Some("R206"), None)
      val errors = Seq(error)

      val data: UnloadingRemarksRejectionViewModel =
        UnloadingRemarksRejectionViewModel(errors, arrivalId, "url")(messages).get

      val expectedJson =
        Json.obj(
          "errors"                     -> errors,
          "contactUrl"                 -> "url",
          "declareUnloadingRemarksUrl" -> routes.IndexController.onPageLoad(arrivalId).url
        )

      data.page mustBe "unloadingRemarksMultipleErrorsRejection.njk"
      data.json must containJson(expectedJson)
    }

    "must not display any sections when error.originalAttributeValue is None" in {

      val error = arbitrary[FunctionalError](arbitraryRejectionError).sample.value.copy(originalAttributeValue = None)
      val result: Option[UnloadingRemarksRejectionViewModel] =
        UnloadingRemarksRejectionViewModel(Seq(error), arrivalId, "url")(messages)

      result mustBe None
    }
  }
}
