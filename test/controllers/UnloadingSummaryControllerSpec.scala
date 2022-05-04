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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.{Generators, ViewModelGenerators}
import models.{Index, NormalMode}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.{ChangesToReportPage, NewSealNumberPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.UnloadingSummaryViewModel
import viewModels.sections.Section
import views.html.UnloadingSummaryView

class UnloadingSummaryControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ViewModelGenerators {

  private val sampleSealsSection: Section                  = arbitrary[Section].sample.value
  private val sampleTransportAndItemSections: Seq[Section] = listWithMaxLength[Section]().sample.value

  private val mockViewModel = mock[UnloadingSummaryViewModel]

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

        val request = FakeRequest(GET, routes.UnloadingSummaryController.onPageLoad(arrivalId).url)

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

        val request = FakeRequest(GET, routes.UnloadingSummaryController.onPageLoad(arrivalId).url)

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
          .setValue(NewSealNumberPage(Index(0)), "new seal value 1")
          .setValue(NewSealNumberPage(Index(1)), "new seal value 2")
        setExistingUserAnswers(userAnswers)

        when(mockViewModel.sealsSection(any(), any())(any()))
          .thenReturn(sampleSealsSection)
        when(mockViewModel.transportAndItemSections(any(), any())(any()))
          .thenReturn(sampleTransportAndItemSections)

        val request = FakeRequest(GET, routes.UnloadingSummaryController.onPageLoad(arrivalId).url)

        val result = route(app, request).value

        val view = injector.instanceOf[UnloadingSummaryView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(mrn, arrivalId, sampleSealsSection, sampleTransportAndItemSections, 2, showAddCommentLink = true)(request, messages).toString

        verify(mockViewModel).sealsSection(eqTo(userAnswers), eqTo(NormalMode))(any())
        verify(mockViewModel).transportAndItemSections(eqTo(userAnswers), eqTo(NormalMode))(any())
      }
    }
  }
}
