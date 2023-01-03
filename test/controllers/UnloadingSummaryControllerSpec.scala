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
import generators.Generators
import models.{Index, NormalMode, Seal}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.{ChangesToReportPage, SealPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.UnloadingSummaryViewModel
import viewModels.sections.Section
import views.html.UnloadingSummaryView

class UnloadingSummaryControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val sampleSealsSection: Section                  = arbitrary[Section].sample.value
  private val sampleTransportAndItemSections: Seq[Section] = listWithMaxLength[Section]().sample.value

  private val mockViewModel = mock[UnloadingSummaryViewModel]

  private lazy val unloadingSummaryRoute = routes.UnloadingSummaryController.onPageLoad(arrivalId).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModel)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingSummaryViewModel].toInstance(mockViewModel))

  "UnloadingSummary Controller" - {

    "return OK and the correct view for a GET" - {

      "when ChangesToReportPage is not populated" in {
        checkArrivalStatus()

        val userAnswers = emptyUserAnswers
        setExistingUserAnswers(userAnswers)

        when(mockViewModel.sealsSection(any(), any())(any()))
          .thenReturn(sampleSealsSection)
        when(mockViewModel.transportAndItemSections(any(), any())(any()))
          .thenReturn(sampleTransportAndItemSections)

        val request = FakeRequest(GET, unloadingSummaryRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[UnloadingSummaryView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(mrn, arrivalId, sampleSealsSection, sampleTransportAndItemSections, 0, showAddCommentLink = true)(request, messages).toString

        verify(mockViewModel).sealsSection(eqTo(userAnswers), eqTo(NormalMode))(any())
        verify(mockViewModel).transportAndItemSections(eqTo(userAnswers), eqTo(NormalMode))(any())
      }

      "when ChangesToReportPage is populated" in {
        checkArrivalStatus()

        val userAnswers = emptyUserAnswers.setValue(ChangesToReportPage, arbitrary[String].sample.value)
        setExistingUserAnswers(userAnswers)

        when(mockViewModel.sealsSection(any(), any())(any()))
          .thenReturn(sampleSealsSection)
        when(mockViewModel.transportAndItemSections(any(), any())(any()))
          .thenReturn(sampleTransportAndItemSections)

        val request = FakeRequest(GET, unloadingSummaryRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[UnloadingSummaryView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(mrn, arrivalId, sampleSealsSection, sampleTransportAndItemSections, 0, showAddCommentLink = false)(request, messages).toString

        verify(mockViewModel).sealsSection(eqTo(userAnswers), eqTo(NormalMode))(any())
        verify(mockViewModel).transportAndItemSections(eqTo(userAnswers), eqTo(NormalMode))(any())
      }

      "when user answers contains seals" in {
        checkArrivalStatus()

        val userAnswers = emptyUserAnswers
          .setValue(SealPage(Index(0)), Seal("new seal value", removable = true))
          .setValue(SealPage(Index(1)), Seal("existing seal value", removable = false))
        setExistingUserAnswers(userAnswers)

        when(mockViewModel.sealsSection(any(), any())(any()))
          .thenReturn(sampleSealsSection)
        when(mockViewModel.transportAndItemSections(any(), any())(any()))
          .thenReturn(sampleTransportAndItemSections)

        val request = FakeRequest(GET, unloadingSummaryRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[UnloadingSummaryView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(mrn, arrivalId, sampleSealsSection, sampleTransportAndItemSections, 2, showAddCommentLink = true)(request, messages).toString

        verify(mockViewModel).sealsSection(eqTo(userAnswers), eqTo(NormalMode))(any())
        verify(mockViewModel).transportAndItemSections(eqTo(userAnswers), eqTo(NormalMode))(any())
      }
    }

    "must redirect to check your answers for a POST" in {
      checkArrivalStatus()
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, unloadingSummaryRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe routes.CheckYourAnswersController.onPageLoad(arrivalId).url
    }
  }
}
