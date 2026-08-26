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
import models.reference.*
import models.reference.TransportMode.InlandMode
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

  private lazy val asyncCacheApi: AsyncCacheApi = app.injector.instanceOf[AsyncCacheApi]

  private lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    asyncCacheApi.removeAll().futureValue
  }

  "Reference Data" - {

    "getCountries" - {
      val url = s"/$baseUrl/lists/CountryCodesFullList"

      val countryListResponseJson: String =
        """
          |[
          |  {
          |    "key":"GB",
          |    "value":"United Kingdom"
          |  },
          |  {
          |    "key":"AD",
          |    "value":"Andorra"
          |  }
          |]
          |""".stripMargin

      "should handle a 200 response" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

      val url = s"/$baseUrl/lists/CountryCodesFullList?keys=$code"

      val countryResponseJson: String =
        """
          |[
          |  {
          |    "key":"GB",
          |    "value":"United Kingdom"
          |  }
          |]
          |""".stripMargin

      "should handle a 200 response" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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
      val url  = s"/$baseUrl/lists/DeclarationTypeSecurity?keys=$code"

      val securityTypeResponseJson: String =
        """
          |[
          |  {
          |    "key":"1",
          |    "value":"description"
          |  }
          |]
          |""".stripMargin

      "should handle a 200 response" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

      val url = s"/$baseUrl/lists/CustomsOffices?referenceNumbers=$code"

      val customsOfficeResponseJson: String =
        """
          |[
          |  {
          |    "referenceNumber":"ID1",
          |    "customsOfficeLsd": {
          |      "languageCode": "EN",
          |      "customsOfficeUsualName": "NAME001"
          |    },
          |    "countryCode":"GB",
          |    "phoneNumber":"004412323232345"
          |  }
          |]
          |""".stripMargin

      "should handle a 200 response" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

    "getCUSCode" - {
      val cusCode = "0010001-6"

      val url = s"/$baseUrl/lists/CUSCode?keys=$cusCode"

      val cusCodeResponseJson: String =
        """
          |[
          |  {
          |    "key": "0010001-6"
          |  }
          |]
          |""".stripMargin

      "must return CUSCode when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

    "getHSCode" - {
      val code = "010121"

      val url = s"/$baseUrl/lists/HScode?keys=$code"

      val json: String =
        """
          |[
          |  {
          |    "key": "010121"
          |  }
          |]
          |""".stripMargin

      "must return HSCode when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(json))
        )

        val expectedResult: HSCode = HSCode(code)

        connector.getHSCode(code).futureValue.value mustEqual expectedResult
        server.resetMappings()
        connector.getHSCode(code).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getHSCode(code))
      }

      "should handle client and server errors" in {
        checkErrorResponse(url, connector.getHSCode(code))
      }
    }

    "getPackageTypes" - {
      val url = s"/$baseUrl/lists/KindOfPackages"

      val packageListResponseJson: String =
        """
            |[
            |  {
            |    "key":"1A",
            |    "value":"Drum, aluminum"
            |  },
            |  {
            |    "key":"1B",
            |    "value":"Drum, plywood"
            |  }
            |]
            |""".stripMargin

      "should handle a 200 response" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

      val url = s"/$baseUrl/lists/SupportingDocumentType?keys=$typeValue"

      val supportingDocumentResponseJson: String =
        """
            |[
            |  {
            |    "key": "C641",
            |    "value": "Dissostichus - catch document import"
            |  }
            |]
            |""".stripMargin

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

      val url = s"/$baseUrl/lists/PreviousDocumentType?keys=$typeValue"

      val previousDocumentResponseJson: String =
        """
            |[
            |  {
            |    "key": "C512",
            |    "value": "SDE - Authorisation to use simplified declaration (Column 7a, Annex A of Delegated Regulation (EU) 2015/2446)"
            |  }
            |]
            |""".stripMargin

      "must return previous document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

      val url = s"/$baseUrl/lists/PreviousDocumentExportType?keys=$typeValue"

      val previousDocumentResponseJson: String =
        """
            |[
            |  {
            |    "key": "C512",
            |    "value": "SDE - Authorisation to use simplified declaration (Column 7a, Annex A of Delegated Regulation (EU) 2015/2446)"
            |  }
            |]
            |""".stripMargin

      "must return previous document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

      val additionalReferenceJson: String =
        """
            |[
            |  {
            |    "key": "documentType1",
            |    "value": "desc1"
            |  },
            |  {
            |    "key": "documentType2",
            |    "value": "desc2"
            |  }
            |]
            |""".stripMargin

      "must return Seq of AdditionalReference when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

      val url = s"/$baseUrl/lists/KindOfPackages?keys=$documentType"

      val packageTypeResponseJson: String =
        """
            |[
            |  {
            |    "key": "1A",
            |    "value": "Drum, steel"
            |  }
            |]
            |""".stripMargin

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

      val url = s"/$baseUrl/lists/IncidentCode?keys=$code"

      val incidentResponseJson: String =
        """
            |[
            |  {
            |    "key": "1",
            |    "value": "The carrier is obligated to deviate from the…"
            |  }
            |]
            |""".stripMargin

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

      val url = s"/$baseUrl/lists/AdditionalReference?keys=$documentType"

      val additionalReferenceResponseJson: String =
        """
            |[
            |  {
            |    "key": "Y023",
            |    "value": "Consignee (AEO certificate number)"
            |  }
            |]
            |""".stripMargin

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

      val url = s"/$baseUrl/lists/AdditionalInformation?keys=$code"

      val additionalInformationJson: String =
        """
            |[
            |  {
            |    "key": "20300",
            |    "value": "Export"
            |  },
            |  {
            |    "key": "30600",
            |    "value": "In EXS, where negotiable bills of lading 'to order blank endorsed' are concerned and the consignee particulars are unknown."
            |  }
            |]
            |""".stripMargin

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

      val url = s"/$baseUrl/lists/QualifierOfIdentificationIncident?keys=$qualifier"

      val qualifierOfIdentificationResponseJson: String =
        """
          |[
          |  {
          |    "key": "U",
          |    "value": "UN/LOCODE"
          |  }
          |]
          |""".stripMargin

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

      val url = s"/$baseUrl/lists/TransportDocumentType?keys=$typeValue"

      val transportDocumentResponseJson: String =
        """
          |[
          |  {
          |    "key": "N235",
          |    "value": "Container list"
          |  }
          |]
          |""".stripMargin

      "must return transport document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

      val documentsJson: String = s"""
           |[
           |  {
           |    "key": "1",
           |    "value": "Document 1"
           |  },
           |  {
           |    "key": "4",
           |    "value": "Document 2"
           |  }
           |]
           |""".stripMargin

      "must return list of documents when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(documentsJson))
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

      val documentsJson: String =
        s"""
           |[
           |  {
           |    "key": "1",
           |    "value": "Document 1"
           |  },
           |  {
           |    "key": "4",
           |    "value": "Document 2"
           |  }
           |]
           |""".stripMargin

      "must return list of documents when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(documentsJson))
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

    "getTransportModeCode" - {
      val code = "1"

      val url: String = s"/$baseUrl/lists/TransportModeCode?keys=$code"

      "must return transport mode code when successful" in {
        val responseJson: String =
          """
            |[
            |  {
            |    "key": "1",
            |    "value": "Maritime Transport"
            |  },
            |  {
            |    "key": "2",
            |    "value": "Rail Transport"
            |  }
            |]
            |""".stripMargin

        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

      val documentsJson: String =
        s"""
           |[
           |  {
           |    "key": "1",
           |    "value": "Document 1"
           |  },
           |  {
           |    "key": "4",
           |    "value": "Document 2"
           |  }
           |]
           |""".stripMargin

      "must return list of documents when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(documentsJson))
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

      val url = s"/$baseUrl/lists/DocumentTypeExcise?keys=$code"

      val documentTypeExciseJson: String =
        """
          |[
          |  {
          |    "key": "C651",
          |    "value": "AAD - Administrative Accompanying Document (EMCS)"
          |  },
          |  {
          |    "key": "C658",
          |    "value": "FAD - Fallback e-AD (EMCS)"
          |  }
          |]
          |""".stripMargin

      "must return DocumentTypeExcise when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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
        .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

  private val emptyResponseJson: String =
    """
      |[]
      |""".stripMargin
}
