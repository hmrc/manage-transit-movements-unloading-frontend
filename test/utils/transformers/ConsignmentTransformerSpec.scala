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
import generated.ConsignmentType05
import generators.Generators
import models.reference.Country
import models.reference.TransportMode.InlandMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.countryOfDestination.CountryOfDestinationPage
import pages.inlandModeOfTransport.InlandModeOfTransportPage
import pages.sections.*
import pages.sections.additionalInformation.AdditionalInformationListSection
import pages.sections.additionalReference.AdditionalReferencesSection
import pages.sections.documents.DocumentsSection
import pages.sections.incidents.IncidentsSection
import pages.{GrossWeightPage, UniqueConsignmentReferencePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}
import services.ReferenceDataService

import scala.concurrent.Future

class ConsignmentTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[ConsignmentTransformer]

  private lazy val mockConsignorTransformer               = mock[ConsignorTransformer]
  private lazy val mockConsigneeTransformer               = mock[ConsigneeTransformer]
  private lazy val mockTransportEquipmentTransformer      = mock[TransportEquipmentTransformer]
  private lazy val mockDepartureTransportMeansTransformer = mock[DepartureTransportMeansTransformer]
  private lazy val mockCountriesOfRoutingTransformer      = mock[CountriesOfRoutingTransformer]
  private lazy val mockDocumentsTransformer               = mock[DocumentsTransformer]
  private lazy val mockHouseConsignmentsTransformer       = mock[HouseConsignmentsTransformer]
  private lazy val mockAdditionalReferencesTransformer    = mock[AdditionalReferencesTransformer]
  private lazy val mockIncidentsTransformer               = mock[IncidentsTransformer]
  private lazy val mockAdditionalInformationTransformer   = mock[AdditionalInformationTransformer]

  private lazy val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ConsignorTransformer].toInstance(mockConsignorTransformer),
        bind[ConsigneeTransformer].toInstance(mockConsigneeTransformer),
        bind[TransportEquipmentTransformer].toInstance(mockTransportEquipmentTransformer),
        bind[DepartureTransportMeansTransformer].toInstance(mockDepartureTransportMeansTransformer),
        bind[CountriesOfRoutingTransformer].toInstance(mockCountriesOfRoutingTransformer),
        bind[DocumentsTransformer].toInstance(mockDocumentsTransformer),
        bind[HouseConsignmentsTransformer].toInstance(mockHouseConsignmentsTransformer),
        bind[AdditionalReferencesTransformer].toInstance(mockAdditionalReferencesTransformer),
        bind[IncidentsTransformer].toInstance(mockIncidentsTransformer),
        bind[AdditionalInformationTransformer].toInstance(mockAdditionalInformationTransformer),
        bind[ReferenceDataService].toInstance(mockReferenceDataService)
      )

  "must transform data" - {
    "when consignment defined" in {
      forAll(arbitrary[ConsignmentType05]) {
        consignment =>
          when(mockConsignorTransformer.transform(any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(ConsignorSection, Json.obj("foo" -> "bar")))
            }

          when(mockConsigneeTransformer.transform(any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(ConsigneeSection, Json.obj("foo" -> "bar")))
            }

          when(mockTransportEquipmentTransformer.transform(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(TransportEquipmentListSection, JsArray(Seq(Json.obj("foo" -> "bar")))))
            }

          when(mockDepartureTransportMeansTransformer.transform(any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(TransportMeansListSection, JsArray(Seq(Json.obj("foo" -> "bar")))))
            }

          when(mockCountriesOfRoutingTransformer.transform(any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(CountriesOfRoutingSection, JsArray(Seq(Json.obj("foo" -> "bar")))))
            }

          when(mockDocumentsTransformer.transform(any(), any(), any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(DocumentsSection, JsArray(Seq(Json.obj("foo" -> "bar")))))
            }

          when(mockHouseConsignmentsTransformer.transform(any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(HouseConsignmentsSection, JsArray(Seq(Json.obj("foo" -> "bar")))))
            }
          when(mockAdditionalReferencesTransformer.transform(any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(AdditionalReferencesSection, JsArray(Seq(Json.obj("foo" -> "bar")))))
            }

          when(mockAdditionalInformationTransformer.transform(any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(AdditionalInformationListSection, JsArray(Seq(Json.obj("foo" -> "bar")))))
            }

          when(mockIncidentsTransformer.transform(any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(IncidentsSection, JsArray(Seq(Json.obj("foo" -> "bar")))))
            }

          val country = Country("GB", "country")
          when(mockReferenceDataService.getCountry(any())(any()))
            .thenReturn(Future.successful(country))

          val inlandMode = InlandMode("1", "mode")
          when(mockReferenceDataService.getTransportModeCode(any())(any()))
            .thenReturn(Future.successful(inlandMode))

          val updatedConsignment = consignment.copy(
            countryOfDestination = Some("country - GB"),
            inlandModeOfTransport = Some("mode"),
            grossMass = Some(2),
            referenceNumberUCR = Some("ucr")
          )

          val result = transformer.transform(Some(updatedConsignment))(hc).apply(emptyUserAnswers).futureValue

          result.getValue(ConsignorSection) mustEqual Json.obj("foo" -> "bar")
          result.getValue(ConsigneeSection) mustEqual Json.obj("foo" -> "bar")
          result.getValue(TransportEquipmentListSection) mustEqual JsArray(Seq(Json.obj("foo" -> "bar")))
          result.getValue(TransportMeansListSection) mustEqual JsArray(Seq(Json.obj("foo" -> "bar")))
          result.getValue(CountriesOfRoutingSection) mustEqual JsArray(Seq(Json.obj("foo" -> "bar")))
          result.getValue(DocumentsSection) mustEqual JsArray(Seq(Json.obj("foo" -> "bar")))
          result.getValue(HouseConsignmentsSection) mustEqual JsArray(Seq(Json.obj("foo" -> "bar")))
          result.getValue(AdditionalReferencesSection) mustEqual JsArray(Seq(Json.obj("foo" -> "bar")))
          result.getValue(AdditionalInformationListSection) mustEqual JsArray(Seq(Json.obj("foo" -> "bar")))
          result.get(GrossWeightPage) mustEqual updatedConsignment.grossMass
          result.get(UniqueConsignmentReferencePage) mustEqual updatedConsignment.referenceNumberUCR
          result.getValue(IncidentsSection) mustEqual JsArray(Seq(Json.obj("foo" -> "bar")))
          result.getValue(CountryOfDestinationPage) mustEqual country
          result.getValue(InlandModeOfTransportPage) mustEqual inlandMode
      }
    }

    "when consignment undefined" in {
      val result = transformer.transform(None)(hc).apply(emptyUserAnswers).futureValue

      result.get(ConsignorSection) must not be defined
      result.get(ConsigneeSection) must not be defined
      result.get(TransportEquipmentListSection) must not be defined
      result.get(TransportMeansListSection) must not be defined
      result.get(CountriesOfRoutingSection) must not be defined
      result.get(DocumentsSection) must not be defined
      result.get(HouseConsignmentsSection) must not be defined
      result.get(AdditionalReferencesSection) must not be defined
      result.get(AdditionalInformationListSection) must not be defined
      result.get(GrossWeightPage) must not be defined
      result.get(IncidentsSection) must not be defined
      result.get(CountryOfDestinationPage) must not be defined
      result.get(InlandModeOfTransportPage) must not be defined
    }
  }
}
