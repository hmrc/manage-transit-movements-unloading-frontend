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

import forms.behaviours.FieldBehaviours
import generators.Generators
import models.messages.UnloadingRemarksRequest
import models.{CheckMode, Index, NormalMode}
import org.scalacheck.Gen
import play.api.Application
import play.api.data.{Field, FormError}
import play.api.i18n.Lang.defaultLang
import play.api.i18n.{Messages, MessagesApi, MessagesImpl}
import play.api.inject.guice.GuiceApplicationBuilder

import scala.util.matching.Regex

class NumberOfPackagesFormProviderSpec extends FieldBehaviours with Generators {

  private val maxLength = UnloadingRemarksRequest.numberOfPackagesLength

  private val invalidKey = "numberOfPackages.error.nonNumeric"

  def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure()
      .build()
  def messagesApi: MessagesApi    = fakeApplication.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(defaultLang, messagesApi)

  private val hcIndex   = Index(0)
  private val itemIndex = Index(0)

  "In Normal Mode" - {

    val requiredKey = "numberOfPackages.normalMode.error.required"

    val form      = new NumberOfPackagesFormProvider()(NormalMode, hcIndex, itemIndex)(messages)
    val fieldName = "value"

    ".value" - {

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        Gen.chooseNum(0, maxLength).toString
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, messages(requiredKey, hcIndex.display.toString, itemIndex.display.toString))
      )
    }

    "must not bind strings that do not match regex" in {

      val expectedError = FormError(fieldName, invalidKey, Seq(hcIndex.display.toString, itemIndex.display.toString))
      val regex         = new Regex("[A-Za-z@~{}><!*&%$]{5}")

      forAll(stringsThatMatchRegex(regex)) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors should contain(expectedError)
      }

    }
  }

  "In Check Mode" - {

    val requiredKey = "numberOfPackages.checkMode.error.required"

    val form      = new NumberOfPackagesFormProvider()(CheckMode, hcIndex, itemIndex)(messages)
    val fieldName = "value"

    ".value" - {

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        Gen.chooseNum(0, maxLength).toString
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, messages(requiredKey))
      )
    }

    "must not bind strings that do not match regex" in {

      val expectedError = FormError(fieldName, invalidKey, Seq(hcIndex.display.toString, itemIndex.display.toString))
      val regex         = new Regex("[A-Za-z@~{}><!*&%$]{5}")

      forAll(stringsThatMatchRegex(regex)) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors should contain(expectedError)
      }

    }
  }

}
