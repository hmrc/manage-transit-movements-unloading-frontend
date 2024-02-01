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

import config.Constants.maxPackageShippingMarkLength
import forms.behaviours.StringFieldBehaviours
import models.messages.UnloadingRemarksRequest.alphaNumericRegex
import models.{CheckMode, Index, NormalMode}
import org.scalacheck.Gen
import play.api.Application
import play.api.data.FormError
import play.api.i18n.Lang.defaultLang
import play.api.i18n.{Messages, MessagesApi, MessagesImpl}
import play.api.inject.guice.GuiceApplicationBuilder

class PackageShippingMarkFormProviderSpec extends StringFieldBehaviours {

  private val invalidKey = "houseConsignment.item.packageShippingMark.error.invalid"
  private val lengthKey  = "houseConsignment.item.packageShippingMark.error.length"

  def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure()
      .build()
  def messagesApi: MessagesApi    = fakeApplication.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(defaultLang, messagesApi)

  private val hcIndex   = Index(0)
  private val itemIndex = Index(0)
  val mode              = Gen.oneOf(NormalMode, CheckMode)

  val overLength: Gen[String] = stringsLongerThan(maxPackageShippingMarkLength)

  "In Normal Mode" - {

    val form      = new PackageShippingMarkFormProvider()(NormalMode, hcIndex, itemIndex)(messages)
    val fieldName = "value"

    ".value" - {
      val requiredKey = "houseConsignment.item.packageShippingMark.normalMode.error.required"

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        Gen.chooseNum(0, maxPackageShippingMarkLength).toString
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, messages(requiredKey, hcIndex.display.toString, itemIndex.display.toString))
      )
    }

    behave like fieldWithInvalidCharacters(
      form,
      fieldName,
      error = FormError(fieldName, invalidKey, Seq(alphaNumericRegex)),
      maxPackageShippingMarkLength
    )
  }

  "In Check Mode" - {

    val requiredKey = "houseConsignment.item.packageShippingMark.checkMode.error.required"

    val form      = new PackageShippingMarkFormProvider()(CheckMode, hcIndex, itemIndex)(messages)
    val fieldName = "value"

    ".value" - {

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        Gen.chooseNum(0, maxPackageShippingMarkLength).toString
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, messages(requiredKey, hcIndex.display, itemIndex.display))
      )
    }

    behave like fieldWithInvalidCharacters(
      form,
      fieldName,
      error = FormError(fieldName, invalidKey, Seq(alphaNumericRegex)),
      maxPackageShippingMarkLength
    )
  }
}
