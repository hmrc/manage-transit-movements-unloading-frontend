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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.{CUSTOM_ConsignmentType05, Number0, SealType04, TransportEquipmentType05}
import generators.Generators
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.verify
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.LargeUnsealedGoodsRecordDiscrepanciesYesNoPage
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.CannotUseRevisedUnloadingProcedureView

class CannotUseRevisedUnloadingProcedureControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {
  private val mode = NormalMode

  lazy val cannotUseRevisedUnloadingProcedureRoute: String = controllers.routes.CannotUseRevisedUnloadingProcedureController.onPageLoad(arrivalId).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "CannotUseRevisedUnloadingProcedureController" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, cannotUseRevisedUnloadingProcedureRoute)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[CannotUseRevisedUnloadingProcedureView]

      contentAsString(result) mustEqual
        view(mrn, arrivalId, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, cannotUseRevisedUnloadingProcedureRoute)

      val result = route(app, request).value

      status(result) `mustEqual` SEE_OTHER

      redirectLocation(result).value `mustEqual` routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to CanSealsBeRead page if number of ie043 seals is not zero" in {

      setExistingUserAnswers(
        emptyUserAnswers.copy(ie043Data =
          basicIe043.copy(Consignment =
            Some(
              CUSTOM_ConsignmentType05(containerIndicator = Number0,
                                       TransportEquipment = Seq(TransportEquipmentType05(sequenceNumber = 1, numberOfSeals = 1, Seal = Seq(SealType04(1, "1"))))
              )
            )
          )
        )
      )

      val request = FakeRequest(POST, cannotUseRevisedUnloadingProcedureRoute)

      val result = route(app, request).value

      status(result) `mustEqual` SEE_OTHER
      redirectLocation(result).value `mustEqual` controllers.routes.CanSealsBeReadController.onPageLoad(arrivalId, NormalMode).url
    }

    "must redirect to UnloadingFindings page and retain LargeUnsealedGoodsRecordDiscrepanciesYesNoPage if number of ie043 seals is zero" in {

      setExistingUserAnswers(
        emptyUserAnswers
          .copy(ie043Data =
            basicIe043.copy(Consignment =
              Some(
                CUSTOM_ConsignmentType05(containerIndicator = Number0,
                                         TransportEquipment = Seq(TransportEquipmentType05(sequenceNumber = 1, numberOfSeals = 0))
                )
              )
            )
          )
          .set(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage, true)
          .get
      )

      val request = FakeRequest(POST, cannotUseRevisedUnloadingProcedureRoute)

      val result = route(app, request).value

      status(result) `mustEqual` SEE_OTHER
      redirectLocation(result).value `mustEqual` controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId).url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue.get(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage) mustBe Some(true)
    }

  }
}
