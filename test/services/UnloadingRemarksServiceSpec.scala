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

package services

import java.time.LocalDate

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.UnloadingConnector
import generators.MessagesModelGenerators
import models.UnloadingPermission
import models.messages.{InterchangeControlReference, _}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{when, _}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.http.Status._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import repositories.InterchangeControlReferenceIdRepository
import UnloadingRemarksRequestServiceSpec.header
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

class UnloadingRemarksServiceSpec extends SpecBase with AppWithDefaultMockFixtures with MessagesModelGenerators with ScalaCheckPropertyChecks {

  private val mockRemarksService                                                 = mock[RemarksService]
  private val mockMetaService                                                    = mock[MetaService]
  private val mockUnloadingRemarksRequestService                                 = mock[UnloadingRemarksRequestService]
  private val mockInterchangeControlReferenceIdRepository                        = mock[InterchangeControlReferenceIdRepository]
  private val mockUnloadingConnector: UnloadingConnector                         = mock[UnloadingConnector]
  private val mockUnloadingRemarksMessageService: UnloadingRemarksMessageService = mock[UnloadingRemarksMessageService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[RemarksService].toInstance(mockRemarksService),
        bind[MetaService].toInstance(mockMetaService),
        bind[UnloadingRemarksRequestService].toInstance(mockUnloadingRemarksRequestService),
        bind[InterchangeControlReferenceIdRepository].toInstance(mockInterchangeControlReferenceIdRepository),
        bind[UnloadingConnector].toInstance(mockUnloadingConnector),
        bind[UnloadingRemarksMessageService].toInstance(mockUnloadingRemarksMessageService)
      )

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "UnloadingRemarksServiceSpec" - {

    "Unloading Remarks Submission" - {

      "should return 202 for successful submission" in {

        forAll(
          arbitrary[UnloadingPermission],
          arbitrary[Meta],
          arbitrary[RemarksConform],
          arbitrary[InterchangeControlReference],
          arbitrary[LocalDate]
        ) {
          (unloadingPermission, meta, unloadingRemarks, interchangeControlReference, localDate) =>
            val userAnswersUpdated =
              emptyUserAnswers
                .set(DateGoodsUnloadedPage, localDate)
                .success
                .value

            when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
              .thenReturn(Future.successful(interchangeControlReference))

            when(mockMetaService.build(interchangeControlReference))
              .thenReturn(meta)

            when(mockRemarksService.build(userAnswersUpdated, unloadingPermission))
              .thenReturn(Future.successful(unloadingRemarks))

            val unloadingRemarksRequest = UnloadingRemarksRequest(
              meta,
              header(unloadingPermission),
              unloadingPermission.traderAtDestination,
              unloadingPermission.presentationOffice,
              unloadingRemarks,
              Nil,
              seals = None
            )

            when(mockUnloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, userAnswersUpdated))
              .thenReturn(
                unloadingRemarksRequest
              )

            when(mockUnloadingConnector.post(any(), any())(any())).thenReturn(Future.successful(HttpResponse(ACCEPTED, "")))

            val arrivalNotificationService = app.injector.instanceOf[UnloadingRemarksService]
            arrivalNotificationService.submit(arrivalId, userAnswersUpdated, unloadingPermission).futureValue mustBe Some(ACCEPTED)

            reset(mockInterchangeControlReferenceIdRepository)
            reset(mockMetaService)
            reset(mockRemarksService)
            reset(mockUnloadingRemarksRequestService)
            reset(mockUnloadingConnector)
        }
      }

      //TODO: Do we need to be more specific for different connector failures?
      "should return 503 when connector fails" in {

        val unloadingPermission         = arbitrary[UnloadingPermission].sample.value
        val meta                        = arbitrary[Meta].sample.value
        val unloadingRemarks            = arbitrary[RemarksConform].sample.value
        val interchangeControlReference = arbitrary[InterchangeControlReference].sample.value
        val localDate                   = arbitrary[LocalDate].sample.value
        val userAnswersUpdated =
          emptyUserAnswers
            .set(DateGoodsUnloadedPage, localDate)
            .success
            .value

        when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
          .thenReturn(Future.successful(interchangeControlReference))

        when(mockMetaService.build(interchangeControlReference))
          .thenReturn(meta)

        when(mockRemarksService.build(userAnswersUpdated, unloadingPermission))
          .thenReturn(Future.successful(unloadingRemarks))

        val unloadingRemarksRequest = arbitrary[UnloadingRemarksRequest].sample.value

        when(mockUnloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, userAnswersUpdated))
          .thenReturn(
            unloadingRemarksRequest
          )

        when(mockUnloadingConnector.post(any(), any())(any())).thenReturn(Future.failed(new Throwable))

        val arrivalNotificationService = app.injector.instanceOf[UnloadingRemarksService]
        arrivalNotificationService.submit(arrivalId, userAnswersUpdated, unloadingPermission).futureValue mustBe Some(SERVICE_UNAVAILABLE)

        reset(mockInterchangeControlReferenceIdRepository)
        reset(mockMetaService)
        reset(mockRemarksService)
        reset(mockUnloadingRemarksRequestService)
        reset(mockUnloadingConnector)
      }

      "should return None when unloading remarks returns FailedToFindUnloadingDate" in {

        forAll(arbitrary[UnloadingPermission], arbitrary[Meta], arbitrary[InterchangeControlReference]) {
          (unloadingPermission, meta, interchangeControlReference) =>
            when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
              .thenReturn(Future.successful(interchangeControlReference))

            when(mockMetaService.build(interchangeControlReference))
              .thenReturn(meta)

            when(mockRemarksService.build(emptyUserAnswers, unloadingPermission))
              .thenReturn(Future.failed(new Throwable))

            val arrivalNotificationService = app.injector.instanceOf[UnloadingRemarksService]
            arrivalNotificationService.submit(arrivalId, emptyUserAnswers, unloadingPermission).futureValue mustBe None

            reset(mockInterchangeControlReferenceIdRepository)
            reset(mockMetaService)
            reset(mockRemarksService)

        }
      }

      "should return None when failed to generate InterchangeControlReference" in {

        forAll(arbitrary[UnloadingPermission]) {
          unloadingPermission =>
            when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
              .thenReturn(Future.failed(new Exception("failed to get InterchangeControlReference")))

            val arrivalNotificationService = app.injector.instanceOf[UnloadingRemarksService]
            arrivalNotificationService.submit(arrivalId, emptyUserAnswers, unloadingPermission).futureValue mustBe None

            reset(mockInterchangeControlReferenceIdRepository)
        }
      }
    }

    "Unloading Remarks Re-submission" - {

      "should return 202 for successful submission" in {
        val unloadingRemarksRequest     = arbitrary[UnloadingRemarksRequest].sample.value
        val meta                        = arbitrary[Meta].sample.value
        val interchangeControlReference = arbitrary[InterchangeControlReference].sample.value

        when(mockUnloadingRemarksMessageService.unloadingRemarksMessage(any())(any(), any())) thenReturn Future.successful(Some(unloadingRemarksRequest))
        when(mockUnloadingConnector.post(any(), any())(any())) thenReturn Future.successful(HttpResponse(ACCEPTED, ""))
        when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
          .thenReturn(Future.successful(interchangeControlReference))
        when(mockMetaService.build(interchangeControlReference))
          .thenReturn(meta)

        val userAnswers = emptyUserAnswers.set(VehicleNameRegistrationReferencePage, "new registration").get

        val arrivalNotificationService = app.injector.instanceOf[UnloadingRemarksService]
        val result                     = arrivalNotificationService.resubmit(arrivalId, userAnswers)
        result.futureValue.value mustBe ACCEPTED
        verify(mockUnloadingRemarksMessageService).unloadingRemarksMessage(any())(any(), any())

        reset(mockUnloadingRemarksMessageService)
        reset(mockUnloadingConnector)
      }

      "should return None if service return unloading remarks value as None" in {
        when(mockUnloadingRemarksMessageService.unloadingRemarksMessage(any())(any(), any())) thenReturn Future.successful(None)

        val arrivalNotificationService = app.injector.instanceOf[UnloadingRemarksService]
        val result                     = arrivalNotificationService.resubmit(arrivalId, emptyUserAnswers)
        result.futureValue mustBe None

        reset(mockUnloadingRemarksMessageService)
      }

      "getUpdatedUnloadingRemarkRequest" - {

        "must return updated UnloadingRemarksRequest for the input VehicleNameRegistrationReferencePage" in {
          val unloadingRemarksRequest     = arbitrary[UnloadingRemarksRequest].sample.value
          val meta                        = arbitrary[Meta].sample.value
          val interchangeControlReference = arbitrary[InterchangeControlReference].sample.value

          when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
            .thenReturn(Future.successful(interchangeControlReference))
          when(mockMetaService.build(interchangeControlReference))
            .thenReturn(meta)
          val userAnswers = emptyUserAnswers.set(VehicleNameRegistrationReferencePage, "new registration").get

          val expectedResultOfControl: Seq[ResultsOfControl] = unloadingRemarksRequest.resultOfControl.map {
            case y: ResultsOfControlDifferentValues if y.pointerToAttribute.pointer == TransportIdentity => y.copy(correctedValue = "new registration")
            case x                                                                                       => x
          }
          val arrivalNotificationService = app.injector.instanceOf[UnloadingRemarksService]
          val result                     = arrivalNotificationService.getUpdatedUnloadingRemarkRequest(unloadingRemarksRequest, userAnswers)
          result.futureValue.value.resultOfControl mustBe expectedResultOfControl

        }

        "must return updated results of control for the input TotalNumberOfPackagesPage" in {
          val unloadingRemarksRequest     = arbitrary[UnloadingRemarksRequest].sample.value
          val meta                        = arbitrary[Meta].sample.value
          val interchangeControlReference = arbitrary[InterchangeControlReference].sample.value

          when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
            .thenReturn(Future.successful(interchangeControlReference))
          when(mockMetaService.build(interchangeControlReference))
            .thenReturn(meta)
          val userAnswers = emptyUserAnswers.set(TotalNumberOfPackagesPage, 1234).success.value

          val expectedResultOfControl: Seq[ResultsOfControl] = unloadingRemarksRequest.resultOfControl.map {
            case y: ResultsOfControlDifferentValues if y.pointerToAttribute.pointer == NumberOfPackages => y.copy(correctedValue = "1234")
            case x                                                                                      => x
          }

          val arrivalNotificationService = app.injector.instanceOf[UnloadingRemarksService]
          val result                     = arrivalNotificationService.getUpdatedUnloadingRemarkRequest(unloadingRemarksRequest, userAnswers)
          result.futureValue.value.resultOfControl mustBe expectedResultOfControl
        }

        "must return updated results of control for the input TotalNumberOfItemsPage" in {
          val unloadingRemarksRequest     = arbitrary[UnloadingRemarksRequest].sample.value
          val meta                        = arbitrary[Meta].sample.value
          val interchangeControlReference = arbitrary[InterchangeControlReference].sample.value

          when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
            .thenReturn(Future.successful(interchangeControlReference))
          when(mockMetaService.build(interchangeControlReference))
            .thenReturn(meta)
          val userAnswers = emptyUserAnswers.set(TotalNumberOfItemsPage, 1234).success.value

          val expectedResultOfControl: Seq[ResultsOfControl] = unloadingRemarksRequest.resultOfControl.map {
            case y: ResultsOfControlDifferentValues if y.pointerToAttribute.pointer == NumberOfItems => y.copy(correctedValue = "1234")
            case x                                                                                   => x
          }

          val arrivalNotificationService = app.injector.instanceOf[UnloadingRemarksService]
          val result                     = arrivalNotificationService.getUpdatedUnloadingRemarkRequest(unloadingRemarksRequest, userAnswers)
          result.futureValue.value.resultOfControl mustBe expectedResultOfControl
        }

        "must return updated results of control for the input GrossMassAmountPage" in {
          val unloadingRemarksRequest     = arbitrary[UnloadingRemarksRequest].sample.value
          val meta                        = arbitrary[Meta].sample.value
          val interchangeControlReference = arbitrary[InterchangeControlReference].sample.value

          when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
            .thenReturn(Future.successful(interchangeControlReference))
          when(mockMetaService.build(interchangeControlReference))
            .thenReturn(meta)
          val userAnswers = emptyUserAnswers.set(GrossMassAmountPage, "1234").success.value

          val expectedResultOfControl: Seq[ResultsOfControl] = unloadingRemarksRequest.resultOfControl.map {
            case y: ResultsOfControlDifferentValues if y.pointerToAttribute.pointer == GrossMass => y.copy(correctedValue = "1234")
            case x                                                                               => x
          }

          val arrivalNotificationService = app.injector.instanceOf[UnloadingRemarksService]
          val result                     = arrivalNotificationService.getUpdatedUnloadingRemarkRequest(unloadingRemarksRequest, userAnswers)
          result.futureValue.value.resultOfControl mustBe expectedResultOfControl

        }

        "must return updated remarks for the input DateGoodsUnloadedPage " in {
          val unloadingRemarksRequest     = arbitrary[UnloadingRemarksRequest].sample.value
          val meta                        = arbitrary[Meta].sample.value
          val interchangeControlReference = arbitrary[InterchangeControlReference].sample.value
          val localDate                   = LocalDate.now
          when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
            .thenReturn(Future.successful(interchangeControlReference))
          when(mockMetaService.build(interchangeControlReference))
            .thenReturn(meta)
          val userAnswers = emptyUserAnswers.set(DateGoodsUnloadedPage, localDate).get

          val expectedUnloadingRemark: Remarks = unloadingRemarksRequest.unloadingRemark match {
            case y: RemarksNonConform => y.copy(unloadingDate = localDate)
            case x                    => x
          }

          val arrivalNotificationService = app.injector.instanceOf[UnloadingRemarksService]
          val result                     = arrivalNotificationService.getUpdatedUnloadingRemarkRequest(unloadingRemarksRequest, userAnswers)
          result.futureValue.value.unloadingRemark mustBe expectedUnloadingRemark

        }
      }
    }
  }
}
