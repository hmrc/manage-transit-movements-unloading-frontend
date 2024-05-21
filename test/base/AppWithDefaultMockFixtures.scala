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

package base

import controllers.actions._
import models.P5.ArrivalMessageType.UnloadingPermission
import models.P5._
import models.{Mode, UserAnswers}
import navigation.SealNavigator.SealNavigatorProvider
import navigation._
import navigation.houseConsignment.index.HouseConsignmentDocumentNavigator.HouseConsignmentDocumentNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.{GuiceFakeApplicationFactory, GuiceOneAppPerSuite}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import repositories.SessionRepository
import services.P5.UnloadingPermissionMessageService

import java.time.LocalDateTime
import scala.concurrent.Future

trait AppWithDefaultMockFixtures extends BeforeAndAfterEach with GuiceOneAppPerSuite with GuiceFakeApplicationFactory with MockitoSugar {
  self: TestSuite =>

  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
    reset(mockDataRetrievalActionProvider)
    reset(mockUnloadingPermissionMessageService)

    when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
      .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, "foo/bar"))))

    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
  }

  final val mockSessionRepository: SessionRepository = mock[SessionRepository]
  final val mockDataRetrievalActionProvider          = mock[DataRetrievalActionProvider]
  final val mockUnloadingPermissionMessageService    = mock[UnloadingPermissionMessageService]
  final val mockUnloadingPermissionActionProvider    = mock[UnloadingPermissionActionProvider]

  final override def fakeApplication(): Application =
    guiceApplicationBuilder()
      .build()

  protected def setExistingUserAnswers(answers: UserAnswers): Unit =
    when(mockDataRetrievalActionProvider.apply(any())) thenReturn new FakeDataRetrievalAction(Some(answers))

  protected def setNoExistingUserAnswers(): Unit =
    when(mockDataRetrievalActionProvider.apply(any())) thenReturn new FakeDataRetrievalAction(None)

  // TODO - delete?
  protected def checkArrivalStatus(): Unit = ()

  protected val onwardRoute: Call = Call("GET", "/foo")

  protected val fakeNavigator: Navigator =
    new FakeNavigator(onwardRoute)

  protected val fakeNavigation: Navigation =
    new FakeNavigation(onwardRoute)

  object FakeConsignmentNavigators {
    import navigation.GoodsReferenceNavigator.GoodsReferenceNavigatorProvider

    val fakeTransportEquipmentNavigator: TransportEquipmentNavigator =
      new FakeTransportEquipmentNavigator(onwardRoute)

    val fakeSealNavigatorProvider: SealNavigatorProvider = new SealNavigatorProvider {
      override def apply(equipmentMode: Mode): SealNavigator = new FakeSealNavigator(onwardRoute, equipmentMode)
    }

    val fakeGoodsReferenceNavigatorProvider: GoodsReferenceNavigatorProvider = new GoodsReferenceNavigatorProvider {
      override def apply(equipmentMode: Mode): GoodsReferenceNavigator = new FakeGoodsReferenceNavigator(onwardRoute, equipmentMode)
    }

    val fakeDocumentNavigator: DocumentNavigator =
      new FakeDocumentNavigator(onwardRoute)

    val fakeDepartureTransportMeansNavigator: navigation.DepartureTransportMeansNavigator =
      new FakeDepartureTransportMeansNavigator(onwardRoute)

    val fakeAdditionalReferenceNavigator: AdditionalReferenceNavigator =
      new FakeAdditionalReferenceNavigator(onwardRoute)
  }

  object FakeHouseConsignmentNavigators {
    import navigation.houseConsignment.index.AdditionalReferenceNavigator.AdditionalReferenceNavigatorProvider
    import navigation.houseConsignment.index._
    import navigation.houseConsignment.index.departureMeansOfTransport.DepartureTransportMeansNavigator
    import navigation.houseConsignment.index.departureMeansOfTransport.DepartureTransportMeansNavigator.DepartureTransportMeansNavigatorProvider

    val fakeHouseConsignmentNavigator: HouseConsignmentNavigator =
      new FakeHouseConsignmentNavigator(onwardRoute)

    val fakeAdditionalReferenceNavigatorProvider: AdditionalReferenceNavigatorProvider =
      new AdditionalReferenceNavigatorProvider {

        override def apply(houseConsignmentMode: Mode): AdditionalReferenceNavigator =
          new FakeHouseConsignmentAdditionalReferenceNavigator(onwardRoute, houseConsignmentMode)
      }

    val fakeDepartureTransportMeansNavigatorProvider: DepartureTransportMeansNavigatorProvider =
      new DepartureTransportMeansNavigatorProvider {

        override def apply(houseConsignmentMode: Mode): DepartureTransportMeansNavigator =
          new FakeHouseConsignmentDepartureTransportMeansNavigator(onwardRoute, houseConsignmentMode)
      }

    val fakeDocumentNavigatorProvider: HouseConsignmentDocumentNavigatorProvider =
      new HouseConsignmentDocumentNavigatorProvider {

        override def apply(houseConsignmentMode: Mode): HouseConsignmentDocumentNavigator =
          new FakeHouseConsignmentDocumentNavigator(onwardRoute, houseConsignmentMode)
      }
  }

  object FakeConsignmentItemNavigators {
    import navigation.houseConsignment.index.items.AdditionalReferenceNavigator.AdditionalReferenceNavigatorProvider
    import navigation.houseConsignment.index.items.DocumentNavigator.DocumentNavigatorProvider
    import navigation.houseConsignment.index.items.HouseConsignmentItemNavigator.HouseConsignmentItemNavigatorProvider
    import navigation.houseConsignment.index.items.PackagesNavigator.PackagesNavigatorProvider
    import navigation.houseConsignment.index.items._

    val fakeConsignmentItemNavigatorProvider: HouseConsignmentItemNavigatorProvider = new HouseConsignmentItemNavigatorProvider {
      override def apply(houseConsignmentMode: Mode): HouseConsignmentItemNavigator = new FakeHouseConsignmentItemNavigator(onwardRoute, houseConsignmentMode)
    }

    val fakeAdditionalReferenceNavigatorProvider: AdditionalReferenceNavigatorProvider =
      new AdditionalReferenceNavigatorProvider {

        override def apply(houseConsignmentMode: Mode, itemMode: Mode): AdditionalReferenceNavigator =
          new FakeConsignmentItemAdditionalReferenceNavigator(onwardRoute, houseConsignmentMode, itemMode)
      }

    val fakePackagesNavigatorProvider: PackagesNavigatorProvider =
      new PackagesNavigatorProvider {

        override def apply(houseConsignmentMode: Mode, itemMode: Mode): PackagesNavigator =
          new FakePackagesNavigator(onwardRoute, houseConsignmentMode, itemMode)
      }

    val fakeDocumentNavigatorProvider: DocumentNavigatorProvider =
      new DocumentNavigatorProvider {

        override def apply(houseConsignmentMode: Mode, itemMode: Mode): DocumentNavigator =
          new FakeItemDocumentNavigator(onwardRoute, houseConsignmentMode, itemMode)
      }
  }

  def guiceApplicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[UnloadingPermissionActionProvider].toInstance(mockUnloadingPermissionActionProvider),
        bind[UnloadingPermissionMessageService].toInstance(mockUnloadingPermissionMessageService),
        bind[DataRetrievalActionProvider].toInstance(mockDataRetrievalActionProvider)
      )
}
