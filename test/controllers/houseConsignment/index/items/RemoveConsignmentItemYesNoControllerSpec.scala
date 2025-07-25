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

package controllers.houseConsignment.index.items

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import generators.Generators
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Gen
import pages.houseConsignment.index.items.{DeclarationGoodsItemNumberPage, ItemDescriptionPage}
import pages.sections.ItemSection
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.houseConsignment.index.items.RemoveConsignmentItemYesNoView

import scala.concurrent.Future

class RemoveConsignmentItemYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("houseConsignment.removeConsignmentItemYesNo", houseConsignmentIndex.display, itemIndex.display)
  private val mode         = NormalMode

  private lazy val removeConsignmentItemRoute =
    controllers.houseConsignment.index.items.routes.RemoveConsignmentItemYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode).url

  "RemoveConsignmentItemYesNoController " - {

    "must return OK and the correct view for a GET" in {

      forAll(Gen.alphaNumStr) {
        itemDescription =>
          val userAnswers = emptyUserAnswers.setValue(ItemDescriptionPage(houseConsignmentIndex, itemIndex), itemDescription)

          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, removeConsignmentItemRoute)

          val result = route(app, request).value

          val view = injector.instanceOf[RemoveConsignmentItemYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, mrn, arrivalId, houseConsignmentIndex, itemIndex, mode, Some(itemDescription))(request, messages).toString

      }
    }

    "when yes submitted" - {
      "must redirect to cross check page and remove consignment item at specified index" in {
        val userAnswers = emptyUserAnswers
          .setValue(ItemSection(houseConsignmentIndex, itemIndex), Json.obj("foo" -> "bar"))
          .setSequenceNumber(ItemSection(houseConsignmentIndex, itemIndex), 1)
          .setValue(DeclarationGoodsItemNumberPage(houseConsignmentIndex, itemIndex), BigInt(1))
          .setNotRemoved(ItemSection(houseConsignmentIndex, itemIndex))

        setExistingUserAnswers(userAnswers)
        when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

        val request = FakeRequest(POST, removeConsignmentItemRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.houseConsignment.index.items.routes.AddAnotherItemController.onPageLoad(arrivalId, houseConsignmentIndex, mode).url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(ItemSection(houseConsignmentIndex, itemIndex)).value mustEqual Json.parse("""
            |{
            |  "declarationGoodsItemNumber" : 1,
            |  "sequenceNumber" : 1,
            |  "removed" : true
            |}
            |""".stripMargin)
      }
    }

    "when no submitted" - {
      "must redirect to cross check page and not remove consignment item at specified index" in {
        when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)
        val userAnswers = emptyUserAnswers
          .setValue(ItemSection(houseConsignmentIndex, itemIndex), Json.obj("foo" -> "bar"))
          .setSequenceNumber(ItemSection(houseConsignmentIndex, itemIndex), 1)
          .setValue(DeclarationGoodsItemNumberPage(houseConsignmentIndex, itemIndex), BigInt(1))
          .setNotRemoved(ItemSection(houseConsignmentIndex, itemIndex))

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeConsignmentItemRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.houseConsignment.index.items.routes.AddAnotherItemController.onPageLoad(arrivalId, houseConsignmentIndex, mode).url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(ItemSection(houseConsignmentIndex, itemIndex)).value mustEqual Json.parse("""
            |{
            |  "declarationGoodsItemNumber" : 1,
            |  "sequenceNumber" : 1,
            |  "foo" : "bar",
            |  "removed" : false
            |}
            |""".stripMargin)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      forAll(Gen.alphaNumStr) {
        itemDescription =>
          val userAnswers = emptyUserAnswers.setValue(ItemDescriptionPage(houseConsignmentIndex, itemIndex), itemDescription)

          setExistingUserAnswers(userAnswers)

          val invalidAnswer = ""

          val request    = FakeRequest(POST, removeConsignmentItemRoute).withFormUrlEncodedBody(("value", ""))
          val filledForm = form.bind(Map("value" -> invalidAnswer))

          val result = route(app, request).value

          status(result) mustEqual BAD_REQUEST

          val view = injector.instanceOf[RemoveConsignmentItemYesNoView]

          contentAsString(result) mustEqual
            view(filledForm, mrn, arrivalId, houseConsignmentIndex, itemIndex, mode, Some(itemDescription))(request, messages).toString
      }

    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, removeConsignmentItemRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, removeConsignmentItemRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
