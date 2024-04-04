/*
 * Copyright 2024 HM Revenue & Customs
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

package utils.transformers

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.ReferenceDataConnector
import generated.ConsignmentType05
import generators.Generators
import models.reference.Country
import models.reference.transport.TransportMode
import models.reference.transport.TransportMode.InlandMode
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.matchers.must.Matchers.defined
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.QuestionPage
import pages.countryOfDestination.CountryOfDestinationPage
import pages.grossMass.GrossMassPage
import pages.inlandModeOfTransport.InlandModeOfTransportPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsPath, Json}

import scala.concurrent.Future

class ConsignmentTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[ConsignmentTransformer]

  private lazy val mockConsignorTransformer               = mock[ConsignorTransformer]
  private lazy val mockConsigneeTransformer               = mock[ConsigneeTransformer]
  private lazy val mockTransportEquipmentTransformer      = mock[TransportEquipmentTransformer]
  private lazy val mockDepartureTransportMeansTransformer = mock[DepartureTransportMeansTransformer]
  private lazy val mockDocumentsTransformer               = mock[DocumentsTransformer]
  private lazy val mockHouseConsignmentsTransformer       = mock[HouseConsignmentsTransformer]
  private lazy val mockAdditionalReferencesTransformer    = mock[AdditionalReferencesTransformer]
  private lazy val mockIncidentsTransformer               = mock[IncidentsTransformer]
  private lazy val mockAdditionalInformationTransformer   = mock[AdditionalInformationTransformer]
  private lazy val mockReferenceDataConnector             = mock[ReferenceDataConnector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ConsignorTransformer].toInstance(mockConsignorTransformer),
        bind[ConsigneeTransformer].toInstance(mockConsigneeTransformer),
        bind[TransportEquipmentTransformer].toInstance(mockTransportEquipmentTransformer),
        bind[DepartureTransportMeansTransformer].toInstance(mockDepartureTransportMeansTransformer),
        bind[DocumentsTransformer].toInstance(mockDocumentsTransformer),
        bind[HouseConsignmentsTransformer].toInstance(mockHouseConsignmentsTransformer),
        bind[AdditionalReferencesTransformer].toInstance(mockAdditionalReferencesTransformer),
        bind[IncidentsTransformer].toInstance(mockIncidentsTransformer),
        bind[AdditionalInformationTransformer].toInstance(mockAdditionalInformationTransformer),
        bind[ReferenceDataConnector].toInstance(mockReferenceDataConnector)
      )

  private case object FakeConsignorSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "consignor"
  }

  private case object FakeConsigneeSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "consignee"
  }

  private case object FakeTransportEquipmentSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "transportEquipment"
  }

  private case object FakeDepartureTransportMeansSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "departureTransportMeans"
  }

  private case object FakeDocumentsSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "documents"
  }

  private case object FakeHouseConsignmentSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "houseConsignment"
  }

  private case object FakeAdditionalReferenceSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "additionalReferenceSection"
  }

  private case object FakeAdditionalInformationSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "additionalInformationSection"
  }

  private case object FakeIncidentSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "incidentSection"
  }

  "must transform data" - {
    "when consignment defined" in {
      forAll(arbitrary[ConsignmentType05]) {
        consignment =>
          when(mockConsignorTransformer.transform(any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(FakeConsignorSection, Json.obj("foo" -> "bar")))
            }
          when(mockConsigneeTransformer.transform(any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(FakeConsigneeSection, Json.obj("foo" -> "bar")))
            }

          when(mockTransportEquipmentTransformer.transform(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(FakeTransportEquipmentSection, Json.obj("foo" -> "bar")))
            }

          when(mockDepartureTransportMeansTransformer.transform(any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(FakeDepartureTransportMeansSection, Json.obj("foo" -> "bar")))
            }

          when(mockDocumentsTransformer.transform(any(), any(), any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(FakeDocumentsSection, Json.obj("foo" -> "bar")))
            }

          when(mockHouseConsignmentsTransformer.transform(any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(FakeHouseConsignmentSection, Json.obj("foo" -> "bar")))
            }
          when(mockAdditionalReferencesTransformer.transform(any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(FakeAdditionalReferenceSection, Json.obj("foo" -> "bar")))
            }

          when(mockAdditionalInformationTransformer.transform(any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(FakeAdditionalInformationSection, Json.obj("foo" -> "bar")))
            }

          when(mockIncidentsTransformer.transform(any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(FakeIncidentSection, Json.obj("foo" -> "bar")))
            }

          val country = Country("GB", "country")
          when(mockReferenceDataConnector.getCountry(any())(any(), any()))
            .thenReturn(Future.successful(country))

          val inlandMode = InlandMode("1", "mode")
          when(mockReferenceDataConnector.getTransportModeCode(any())(any(), any()))
            .thenReturn(Future.successful(inlandMode))

          val updatedConsignment = consignment.copy(countryOfDestination = Some("country - GB"), inlandModeOfTransport = Some("mode"))

          val result = transformer.transform(Some(updatedConsignment))(hc).apply(emptyUserAnswers).futureValue

          result.getValue(FakeConsignorSection) mustBe Json.obj("foo" -> "bar")
          result.getValue(FakeConsigneeSection) mustBe Json.obj("foo" -> "bar")
          result.getValue(FakeTransportEquipmentSection) mustBe Json.obj("foo" -> "bar")
          result.getValue(FakeDepartureTransportMeansSection) mustBe Json.obj("foo" -> "bar")
          result.getValue(FakeDocumentsSection) mustBe Json.obj("foo" -> "bar")
          result.getValue(FakeHouseConsignmentSection) mustBe Json.obj("foo" -> "bar")
          result.getValue(FakeAdditionalReferenceSection) mustBe Json.obj("foo" -> "bar")
          result.getValue(FakeAdditionalInformationSection) mustBe Json.obj("foo" -> "bar")
          result.get(GrossMassPage) mustBe updatedConsignment.grossMass
          result.getValue(FakeIncidentSection) mustBe Json.obj("foo" -> "bar")
          result.getValue(CountryOfDestinationPage) mustBe country
          result.getValue(InlandModeOfTransportPage) mustBe inlandMode
      }
    }

    "when consignment undefined" in {
      val result = transformer.transform(None)(hc).apply(emptyUserAnswers).futureValue

      result.get(FakeConsignorSection) must not be defined
      result.get(FakeConsigneeSection) must not be defined
      result.get(FakeTransportEquipmentSection) must not be defined
      result.get(FakeDepartureTransportMeansSection) must not be defined
      result.get(FakeDocumentsSection) must not be defined
      result.get(FakeHouseConsignmentSection) must not be defined
      result.get(FakeAdditionalReferenceSection) must not be defined
      result.get(FakeAdditionalInformationSection) must not be defined
      result.get(GrossMassPage) must not be defined
      result.get(FakeIncidentSection) must not be defined
      result.get(CountryOfDestinationPage) must not be defined
      result.get(InlandModeOfTransportPage) must not be defined
    }
  }
}
