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

import controllers.actions.*
import models.{Mode, UserAnswers}
import navigation.*
import navigation.SealNavigator.SealNavigatorProvider
import navigation.houseConsignment.index.HouseConsignmentDocumentNavigator.HouseConsignmentDocumentNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.{GuiceFakeApplicationFactory, GuiceOneAppPerSuite}
import play.api.Application
import play.api.cache.AsyncCacheApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import repositories.SessionRepository
import services.P5.UnloadingPermissionMessageService

import scala.concurrent.Future

trait AppWithDefaultMockFixtures extends BeforeAndAfterEach with GuiceOneAppPerSuite with GuiceFakeApplicationFactory with MockitoSugar {
  self: TestSuite =>

  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
    reset(mockDataRetrievalActionProvider)
    reset(mockUnloadingPermissionMessageService)

    when(mockUnloadingPermissionMessageService.canSubmitUnloadingRemarks(any())(any(), any()))
      .thenReturn(Future.successful(true))

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
    when(mockDataRetrievalActionProvider.apply(any())) `thenReturn` new FakeDataRetrievalAction(Some(answers))

  protected def setNoExistingUserAnswers(): Unit =
    when(mockDataRetrievalActionProvider.apply(any())) `thenReturn` new FakeDataRetrievalAction(None)

  protected val onwardRoute: Call = Call("GET", "/foo")

  protected val fakeNavigator: Navigator =
    new FakeNavigator(onwardRoute)

  protected val fakeNavigation: Navigation =
    new FakeNavigation(onwardRoute)

  object FakeConsignmentNavigators {
    import navigation.GoodsReferenceNavigator.GoodsReferenceNavigatorProvider

    val fakeConsignmentNavigator: ConsignmentNavigator =
      new FakeConsignmentNavigator(onwardRoute)

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

    val fakeCountryOfRoutingNavigator: CountryOfRoutingNavigator =
      new FakeCountryOfRoutingNavigator(onwardRoute)

    val fakeDepartureTransportMeansNavigator: navigation.DepartureTransportMeansNavigator =
      new FakeDepartureTransportMeansNavigator(onwardRoute)

    val fakeAdditionalReferenceNavigator: AdditionalReferenceNavigator =
      new FakeAdditionalReferenceNavigator(onwardRoute)
  }

  object FakeHouseConsignmentNavigators {

    import navigation.houseConsignment.index.*
    import navigation.houseConsignment.index.AdditionalReferenceNavigator.AdditionalReferenceNavigatorProvider
    import navigation.houseConsignment.index.departureMeansOfTransport.DepartureTransportMeansNavigator
    import navigation.houseConsignment.index.departureMeansOfTransport.DepartureTransportMeansNavigator.DepartureTransportMeansNavigatorProvider

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

    import navigation.houseConsignment.index.items.*
    import navigation.houseConsignment.index.items.AdditionalReferenceNavigator.AdditionalReferenceNavigatorProvider
    import navigation.houseConsignment.index.items.DocumentNavigator.DocumentNavigatorProvider
    import navigation.houseConsignment.index.items.PackagesNavigator.PackagesNavigatorProvider

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

  private def defaultApplicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[UnloadingPermissionActionProvider].toInstance(mockUnloadingPermissionActionProvider),
        bind[UnloadingPermissionMessageService].toInstance(mockUnloadingPermissionMessageService),
        bind[DataRetrievalActionProvider].toInstance(mockDataRetrievalActionProvider),
        bind[AsyncCacheApi].to[FakeAsyncCacheApi],
        bind[Call].qualifiedWith("onwardRoute").toInstance(onwardRoute)
      )

  protected def guiceApplicationBuilder(): GuiceApplicationBuilder =
    defaultApplicationBuilder()

  def phase5App: GuiceApplicationBuilder => GuiceApplicationBuilder =
    _ => guiceApplicationBuilder().configure("feature-flags.phase-6-enabled" -> false)

  def phase6App: GuiceApplicationBuilder => GuiceApplicationBuilder =
    _ => guiceApplicationBuilder().configure("feature-flags.phase-6-enabled" -> true)
}
