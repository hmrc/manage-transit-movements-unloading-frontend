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

package connectors

import cats.data.NonEmptySet
import com.github.tomakehurst.wiremock.client.WireMock.*
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import connectors.ReferenceDataConnectorSpec.*
import itbase.{ItSpecBase, WireMockServerHandler}
import models.DocType.{Previous, Support, Transport}
import models.SecurityType
import models.reference.*
import models.reference.TransportMode.{BorderMode, InlandMode}
import org.scalacheck.Gen
import org.scalatest.{Assertion, EitherValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.cache.AsyncCacheApi
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends ItSpecBase with WireMockServerHandler with ScalaCheckPropertyChecks with EitherValues {

  private val baseUrl = "customs-reference-data/test-only"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.customs-reference-data.port" -> server.port())

  private lazy val asyncCacheApi: AsyncCacheApi      = app.injector.instanceOf[AsyncCacheApi]
  private lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    asyncCacheApi.removeAll().futureValue
  }

  "Reference Data" - {

    "getCountries" - {
      val url = s"/$baseUrl/lists/CountryCodesFullList"

      "should handle a 200 response" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(countryListResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          Country("GB", "United Kingdom"),
          Country("AD", "Andorra")
        )

        connector.getCountries().futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCountries())
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getCountries())
      }
    }

    "getCountry" - {
      val code = "GB"
      val url  = s"/$baseUrl/lists/CountryCodesFullList?data.code=$code"

      "should handle a 200 response" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(countryResponseJson))
        )

        val expectedResult = Country("GB", "United Kingdom")

        connector.getCountry(code).futureValue.value mustEqual expectedResult
        server.resetMappings()
        connector.getCountry(code).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCountry(code))
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getCountry(code))
      }
    }

    "getSecurityType" - {
      val code = "GB"
      val url  = s"/$baseUrl/lists/DeclarationTypeSecurity?data.code=$code"

      "should handle a 200 response" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(securityTypeResponseJson))
        )

        val expectedResult = SecurityType("1", "description")

        connector.getSecurityType(code).futureValue.value mustEqual expectedResult
        server.resetMappings()
        connector.getSecurityType(code).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getSecurityType(code))
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getSecurityType(code))
      }
    }

    "getCustomsOffice" - {
      val code = "GB00001"
      val url  = s"/$baseUrl/lists/CustomsOffices?data.id=$code"

      "should handle a 200 response" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(customsOfficeResponseJson))
        )

        val expectedResult = CustomsOffice("ID1", "NAME001", "GB", Some("004412323232345"))

        connector.getCustomsOffice(code).futureValue.value mustEqual expectedResult
        server.resetMappings()
        connector.getCustomsOffice(code).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCustomsOffice(code))
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getCustomsOffice(code))
      }
    }

    "getCusCode" - {
      val cusCode = "0010001-6"
      val url     = s"/$baseUrl/lists/CUSCode?data.code=$cusCode"

      "must return CUSCode when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(cusCodeResponseJson))
        )

        val expectedResult: CUSCode = CUSCode(cusCode)

        connector.getCUSCode(cusCode).futureValue.value mustEqual expectedResult
        server.resetMappings()
        connector.getCUSCode(cusCode).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCUSCode(cusCode))
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getCUSCode(cusCode))
      }
    }

    "getPackageTypes" - {
      val url = s"/$baseUrl/lists/KindOfPackages"

      "should handle a 200 response" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(packageListResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          PackageType("1A", "Drum, aluminum"),
          PackageType("1B", "Drum, plywood")
        )

        connector.getPackageTypes.futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getPackageTypes)
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getPackageTypes)
      }
    }

    "getSupportingDocument" - {
      val typeValue = "C641"
      val url       = s"/$baseUrl/lists/SupportingDocumentType?data.code=$typeValue"

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(supportingDocumentResponseJson))
        )

        val expectedResult: DocumentType = DocumentType(Support, typeValue, "Dissostichus - catch document import")

        connector.getSupportingDocument(typeValue).futureValue.value mustEqual expectedResult
        server.resetMappings()
        connector.getSupportingDocument(typeValue).futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getSupportingDocument(typeValue))
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getSupportingDocument(typeValue))
      }
    }

    "getPreviousDocument" - {
      val typeValue = "C512"
      val url       = s"/$baseUrl/lists/PreviousDocumentType?data.code=$typeValue"

      "must return previous document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(previousDocumentResponseJson))
        )

        val expectedResult: DocumentType =
          DocumentType(Previous, typeValue, "SDE - Authorisation to use simplified declaration (Column 7a, Annex A of Delegated Regulation (EU) 2015/2446)")

        connector.getPreviousDocument(typeValue).futureValue.value mustEqual expectedResult
        server.resetMappings()
        connector.getPreviousDocument(typeValue).futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getPreviousDocument(typeValue))
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getPreviousDocument(typeValue))
      }
    }

    "getPreviousDocumentExport" - {
      val typeValue = "C512"
      val url       = s"/$baseUrl/lists/PreviousDocumentExportType?data.code=$typeValue"

      "must return previous document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(previousDocumentResponseJson))
        )

        val expectedResult: DocumentType =
          DocumentType(Previous, typeValue, "SDE - Authorisation to use simplified declaration (Column 7a, Annex A of Delegated Regulation (EU) 2015/2446)")

        connector.getPreviousDocumentExport(typeValue).futureValue.value mustEqual expectedResult
        server.resetMappings()
        connector.getPreviousDocumentExport(typeValue).futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getPreviousDocumentExport(typeValue))
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getPreviousDocumentExport(typeValue))
      }
    }

    "getAdditionalReferences" - {
      val url = s"/$baseUrl/lists/AdditionalReference"

      "must return Seq of AdditionalReference when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(additionalReferenceJson))
        )

        val expectedResult: NonEmptySet[AdditionalReferenceType] = NonEmptySet.of(
          AdditionalReferenceType("documentType1", "desc1"),
          AdditionalReferenceType("documentType2", "desc2")
        )

        connector.getAdditionalReferences().futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getAdditionalReferences())
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getAdditionalReferences())
      }
    }

    "getPackageType" - {
      val documentType = "1A"
      val url          = s"/$baseUrl/lists/KindOfPackages?data.code=$documentType"

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(packageTypeResponseJson))
        )

        val expectedResult: PackageType = PackageType(documentType, "Drum, steel")

        connector.getPackageType(documentType).futureValue.value mustEqual expectedResult
        server.resetMappings()
        connector.getPackageType(documentType).futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getPackageType(documentType))
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getPackageType(documentType))
      }
    }

    "getIncidentType" - {
      val code = "1"
      val url  = s"/$baseUrl/lists/IncidentCode?data.code=$code"

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(incidentResponseJson))
        )

        val expectedResult: Incident = Incident(code, "The carrier is obligated to deviate from the…")

        connector.getIncidentType(code).futureValue.value mustEqual expectedResult
        server.resetMappings()
        connector.getIncidentType(code).futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getIncidentType(code))
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getIncidentType(code))
      }
    }

    "getAdditionalReferenceType" - {
      val documentType = "Y023"
      val url          = s"/$baseUrl/lists/AdditionalReference?data.documentType=$documentType"

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(additionalReferenceResponseJson))
        )

        val expectedResult: AdditionalReferenceType = AdditionalReferenceType(documentType, "Consignee (AEO certificate number)")

        connector.getAdditionalReference(documentType).futureValue.value mustEqual expectedResult
        server.resetMappings()
        connector.getAdditionalReference(documentType).futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getAdditionalReference(documentType))
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getAdditionalReference(documentType))
      }
    }

    "getAdditionalInformationType" - {
      val code = "20300"
      val url  = s"/$baseUrl/lists/AdditionalInformation?data.code=$code"

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(additionalInformationJson))
        )

        val expectedResult: AdditionalInformationCode = AdditionalInformationCode(code, "Export")

        connector.getAdditionalInformationCode(code).futureValue.value mustEqual expectedResult
        server.resetMappings()
        connector.getAdditionalInformationCode(code).futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getAdditionalInformationCode(code))
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getAdditionalInformationCode(code))
      }
    }

    "getQualifierOfIdentificationIncident" - {
      val qualifier = "U"
      val url       = s"/$baseUrl/lists/QualifierOfIdentificationIncident?data.qualifier=$qualifier"

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(qualifierOfIdentificationResponseJson))
        )

        val expectedResult: QualifierOfIdentification = QualifierOfIdentification(qualifier, "UN/LOCODE")

        connector.getQualifierOfIdentificationIncident(qualifier).futureValue.value mustEqual expectedResult
        server.resetMappings()
        connector.getQualifierOfIdentificationIncident(qualifier).futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getQualifierOfIdentificationIncident(qualifier))
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getQualifierOfIdentificationIncident(qualifier))
      }
    }

    "getTransportDocument" - {
      val typeValue = "N235"
      val url       = s"/$baseUrl/lists/TransportDocumentType?data.code=$typeValue"

      "must return transport document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(transportDocumentResponseJson))
        )

        val expectedResult: DocumentType = DocumentType(Transport, typeValue, "Container list")

        connector.getTransportDocument(typeValue).futureValue.value mustEqual expectedResult
        server.resetMappings()
        connector.getTransportDocument(typeValue).futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getTransportDocument(typeValue))
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getTransportDocument(typeValue))
      }
    }

    "getTransportDocuments" - {
      val url = s"/$baseUrl/lists/TransportDocumentType"

      "must return list of documents when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(documentsJson("TransportDocumentType")))
        )

        val expectResult = NonEmptySet.of(
          DocumentType(Transport, "1", "Document 1"),
          DocumentType(Transport, "4", "Document 2")
        )

        connector.getTransportDocuments().futureValue.value mustEqual expectResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getTransportDocuments())
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getTransportDocuments())
      }
    }

    "getSupportingDocuments" - {
      val url = s"/$baseUrl/lists/SupportingDocumentType"

      "must return list of documents when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(documentsJson("SupportingDocumentType")))
        )

        val expectResult = NonEmptySet.of(
          DocumentType(Support, "1", "Document 1"),
          DocumentType(Support, "4", "Document 2")
        )

        connector.getSupportingDocuments().futureValue.value mustEqual expectResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getSupportingDocuments())
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getSupportingDocuments())
      }
    }

    "getTransportModeCodes" - {
      val url: String = s"/$baseUrl/lists/TransportModeCode"

      "when inland modes" - {

        "must return Seq of inland modes when successful" in {
          val responseJson: String =
            """
              |{
              |  "_links": {
              |    "self": {
              |      "href": "/customs-reference-data/lists/TransportModeCode"
              |    }
              |  },
              |  "meta": {
              |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
              |    "snapshotDate": "2023-01-01"
              |  },
              |  "id": "TransportModeCode",
              |  "data": [
              |    {
              |      "code": "1",
              |      "description": "Maritime Transport"
              |    },
              |    {
              |      "code": "2",
              |      "description": "Rail Transport"
              |    }
              |  ]
              |}
              |""".stripMargin

          server.stubFor(
            get(urlEqualTo(url))
              .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
              .willReturn(okJson(responseJson))
          )

          val expectedResult = NonEmptySet.of(
            InlandMode("1", "Maritime Transport"),
            InlandMode("2", "Rail Transport")
          )

          connector.getTransportModeCodes[InlandMode].futureValue.value mustEqual expectedResult
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          checkNoReferenceDataFoundResponse(url, connector.getTransportModeCodes[InlandMode])
        }

        "should handle client and server errors" in {
          checkErrorResponse(url, connector.getTransportModeCodes[InlandMode])
        }
      }

      "when border modes" - {

        "must return Seq of border modes when successful" in {
          val responseJson: String =
            """
              |{
              |  "_links": {
              |    "self": {
              |      "href": "/customs-reference-data/lists/TransportModeCode"
              |    }
              |  },
              |  "meta": {
              |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
              |    "snapshotDate": "2023-01-01"
              |  },
              |  "id": "TransportModeCode",
              |  "data": [
              |    {
              |      "code": "1",
              |      "description": "Maritime Transport"
              |    },
              |    {
              |      "code": "1",
              |      "description": "Maritime Transport"
              |    },
              |    {
              |      "code": "2",
              |      "description": "Rail Transport"
              |    }
              |  ]
              |}
              |""".stripMargin

          server.stubFor(
            get(urlEqualTo(url))
              .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
              .willReturn(okJson(responseJson))
          )

          val expectedResult = NonEmptySet.of(
            BorderMode("1", "Maritime Transport"),
            BorderMode("2", "Rail Transport")
          )

          connector.getTransportModeCodes[BorderMode].futureValue.value mustEqual expectedResult
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          checkNoReferenceDataFoundResponse(url, connector.getTransportModeCodes[BorderMode])
        }

        "should handle client and server errors" in {
          checkErrorResponse(url, connector.getTransportModeCodes[BorderMode])
        }
      }
    }

    "getTransportModeCode" - {
      val code        = "1"
      val url: String = s"/$baseUrl/lists/TransportModeCode?data.code=$code"

      "must return transport mode code when successful" in {
        val responseJson: String =
          """
            |{
            |  "_links": {
            |    "self": {
            |      "href": "/customs-reference-data/lists/TransportModeCode"
            |    }
            |  },
            |  "meta": {
            |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
            |    "snapshotDate": "2023-01-01"
            |  },
            |  "id": "TransportModeCode",
            |  "data": [
            |    {
            |      "code": "1",
            |      "description": "Maritime Transport"
            |    },
            |    {
            |      "code": "2",
            |      "description": "Rail Transport"
            |    }
            |  ]
            |}
            |""".stripMargin

        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(responseJson))
        )

        val expectedResult = InlandMode("1", "Maritime Transport")

        connector.getTransportModeCode(code).futureValue.value mustEqual expectedResult
        server.resetMappings()
        connector.getTransportModeCode(code).futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getTransportModeCode(code))
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getTransportModeCode(code))
      }

    }

    "getPreviousDocuments" - {
      val url = s"/$baseUrl/lists/PreviousDocumentType"

      "must return list of documents when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(documentsJson("PreviousDocumentType")))
        )

        val expectResult = NonEmptySet.of(
          DocumentType(Previous, "1", "Document 1"),
          DocumentType(Previous, "4", "Document 2")
        )

        connector.getPreviousDocuments().futureValue.value mustEqual expectResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getPreviousDocuments())
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getPreviousDocuments())
      }
    }

    "getDocumentTypeExcise" - {
      val code = "C651"
      val url  = s"/$baseUrl/lists/DocumentTypeExcise?data.code=$code"

      "must return DocumentTypeExcise when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(documentTypeExciseJson))
        )

        val expectedResult = DocTypeExcise("C651", "AAD - Administrative Accompanying Document (EMCS)")

        connector.getDocumentTypeExcise(code).futureValue.value mustEqual expectedResult
        server.resetMappings()
        connector.getDocumentTypeExcise(code).futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getDocumentTypeExcise(code))
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getDocumentTypeExcise(code))
      }
    }
  }

  private def checkNoReferenceDataFoundResponse(url: String, result: => Future[Either[Exception, ?]]): Assertion = {
    server.stubFor(
      get(urlEqualTo(url))
        .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
        .willReturn(okJson(emptyResponseJson))
    )

    result.futureValue.left.value mustBe a[NoReferenceDataFoundException]
  }

  private def checkErrorResponse(url: String, result: => Future[Either[Exception, ?]]): Assertion = {
    val errorResponses: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)

    forAll(errorResponses) {
      errorResponse =>
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(
              aResponse()
                .withStatus(errorResponse)
            )
        )

        result.futureValue.left.value mustBe an[Exception]
    }
  }
}

object ReferenceDataConnectorSpec {

  private val countryListResponseJson: String =
    """
      |{
      | "data":
      | [
      |  {
      |    "code":"GB",
      |    "state":"valid",
      |    "description":"United Kingdom"
      |  },
      |  {
      |    "code":"AD",
      |    "state":"valid",
      |    "description":"Andorra"
      |  }
      | ]
      |}
      |""".stripMargin

  private val countryResponseJson: String =
    """
      |{
      | "data":
      | [
      |  {
      |    "code":"GB",
      |    "state":"valid",
      |    "description":"United Kingdom"
      |  }
      | ]
      |}
      |""".stripMargin

  private val securityTypeResponseJson: String =
    """
      |{
      | "data":
      | [
      |  {
      |    "code":"1",
      |    "state":"valid",
      |    "description":"description"
      |  }
      | ]
      |}
      |""".stripMargin

  private val customsOfficeResponseJson: String =
    """
      |{
      | "data" :
      | [
      |  {
      |    "id":"ID1",
      |    "name":"NAME001",
      |    "countryId":"GB",
      |    "phoneNumber":"004412323232345",
      |    "languageCode": "EN"
      |  }
      | ]
      |}
      |""".stripMargin

  val countryCodeResponseJson: String =
    """
      |{
      | "data":
      | [
      |  {
      |    "code":"GB",
      |    "state":"valid",
      |    "description":"United Kingdom"
      |  }
      | ]
      |}
      |""".stripMargin

  private val cusCodeResponseJson: String =
    """
      |{
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/CUSCode"
      |    }
      |  },
      |  "meta": {
      |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "CUSCode",
      |  "data": [
      |    {
      |      "code": "0010001-6"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val packageListResponseJson: String =
    """
      |{
      | "data":
      | [
      |  {
      |    "code":"1A",
      |    "description":"Drum, aluminum"
      |  },
      |  {
      |    "code":"1B",
      |    "description":"Drum, plywood"
      |  }
      | ]
      |}
      |""".stripMargin

  private val supportingDocumentResponseJson: String =
    """
      |{
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/SupportingDocumentType?data.code=C641"
      |    }
      |  },
      |  "meta": {
      |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "SupportingDocumentType",
      |  "data": [
      |    {
      |      "code": "C641",
      |      "description": "Dissostichus - catch document import"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val previousDocumentResponseJson: String =
    """
      |{
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/PreviousDocumentType?data.code=C512"
      |    }
      |  },
      |  "meta": {
      |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "PreviousDocumentType",
      |  "data": [
      |    {
      |      "code": "C512",
      |      "description": "SDE - Authorisation to use simplified declaration (Column 7a, Annex A of Delegated Regulation (EU) 2015/2446)"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val additionalReferenceJson: String =
    """
      |{
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/AdditionalReference"
      |    }
      |  },
      |  "meta": {
      |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "AdditionalReference",
      |  "data": [
      | {
      |    "documentType": "documentType1",
      |    "description": "desc1"
      |  },
      |  {
      |    "documentType": "documentType2",
      |    "description": "desc2"
      |  }
      |]
      |}
      |""".stripMargin

  private val additionalInformationJson: String =
    """
      |{
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/AdditionalInformation"
      |    }
      |  },
      |  "meta": {
      |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "AdditionalInformation",
      |  "data": [
      | {
      |    "code": "20300",
      |    "description": "Export"
      |  },
      |  {
      |    "code": "30600",
      |    "description": "In EXS, where negotiable bills of lading 'to order blank endorsed' are concerned and the consignee particulars are unknown."
      |  }
      |]
      |}
      |""".stripMargin

  private val additionalReferenceResponseJson: String =
    """
      |{
      |  "data": [
      |    {
      |      "documentType": "Y023",
      |      "description": "Consignee (AEO certificate number)"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val qualifierOfIdentificationResponseJson: String =
    """
      |{
      |  "data": [
      |    {
      |      "qualifier": "U",
      |      "description": "UN/LOCODE"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val packageTypeResponseJson: String =
    """
      |{
      |  "data": [
      |    {
      |      "code": "1A",
      |      "description": "Drum, steel"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val incidentResponseJson: String =
    """
      |{
      |  "data": [
      |    {
      |      "code": "1",
      |      "description": "The carrier is obligated to deviate from the…"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val transportDocumentResponseJson: String =
    """
      |{
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/TransportDocumentType?data.code=N235"
      |    }
      |  },
      |  "meta": {
      |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "TransportDocumentType",
      |  "data": [
      |    {
      |      "code": "N235",
      |      "description": "Container list"
      |    }
      |  ]
      |}
      |""".stripMargin

  private def documentsJson(docType: String): String =
    s"""
       |{
       |"_links": {
       |    "self": {
       |      "href": "/customs-reference-data/lists/$docType"
       |    }
       |  },
       |  "meta": {
       |    "version": "410157ad-bc37-4e71-af2a-404d1ddad94c",
       |    "snapshotDate": "2023-01-01"
       |  },
       |  "id": "$docType",
       |  "data": [
       |  {
       |    "code": "1",
       |    "description": "Document 1"
       |  },
       |  {
       |    "code": "4",
       |    "description": "Document 2"
       |  }
       |  ]
       |}
       |""".stripMargin

  private val documentTypeExciseJson: String =
    """
      |{
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/DocumentTypeExcise"
      |    }
      |  },
      |  "meta": {
      |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "DocumentTypeExcise",
      |  "data": [
      |  {
      |    "activeFrom": "2024-01-01",
      |    "code": "C651",
      |    "state": "valid",
      |    "description": "AAD - Administrative Accompanying Document (EMCS)"
      |  },
      |  {
      |    "activeFrom": "2024-01-01",
      |    "code": "C658",
      |    "state": "valid",
      |    "description": "FAD - Fallback e-AD (EMCS)"
      |  }
      |]
      |}
      |""".stripMargin

  private val emptyResponseJson: String =
    """
      |{
      |  "data": []
      |}
      |""".stripMargin
}
