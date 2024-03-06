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
import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import connectors.ReferenceDataConnectorSpec._
import itbase.{ItSpecBase, WireMockServerHandler}
import models.DocType.{Previous, Support, Transport}
import models.SecurityType
import models.reference._
import models.reference.transport.TransportMode.{BorderMode, InlandMode}
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends ItSpecBase with WireMockServerHandler with ScalaCheckPropertyChecks {

  private val baseUrl = "customs-reference-data/test-only"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.customs-reference-data.port" -> server.port())

  private lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]
  private val code                                   = "GB00001"
  private val countryCode                            = "GB"

  "Reference Data" - {

    "getCountries" - {
      val url = s"/$baseUrl/lists/CountryCodesFullList"

      "should handle a 200 response for countries" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countryListResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          Country("GB", "United Kingdom"),
          Country("AD", "Andorra")
        )

        connector.getCountries().futureValue mustBe expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCountries())
      }

      "should handle client and server errors for countries" in {
        checkErrorResponse(url, connector.getCountries())
      }
    }

    "getCountry" - {
      val code = "GB"
      val url  = s"/$baseUrl/lists/CountryCodesFullList?data.code=$code"

      "should handle a 200 response for countries" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countryResponseJson))
        )

        val expectedResult = Country("GB", "United Kingdom")

        connector.getCountry(code).futureValue mustBe expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCountry(code))
      }

      "should handle client and server errors for countries" in {
        checkErrorResponse(url, connector.getCountry(code))
      }
    }

    "getSecurityType" - {
      val code = "GB"
      val url  = s"/$baseUrl/lists/DeclarationTypeSecurity?data.code=$code"

      "should handle a 200 response for countries" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(securityTypeResponseJson))
        )

        val expectedResult = SecurityType("1", "description")

        connector.getSecurityType(code).futureValue mustBe expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getSecurityType(code))
      }

      "should handle client and server errors for countries" in {
        checkErrorResponse(url, connector.getSecurityType(code))
      }
    }

    "getCustomsOffice" - {
      val url = s"/$baseUrl/lists/CustomsOffices?data.id=$code"

      "should handle a 200 response for customs office with code end point with valid phone number" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(customsOfficeResponseJsonWithPhone))
        )

        val expectedResult = NonEmptySet.of(CustomsOffice("ID1", "NAME001", "GB", Some("004412323232345")))

        connector.getCustomsOffice(code).futureValue mustBe expectedResult
      }

      "should handle a 200 response for customs office with code end point with no phone number" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(customsOfficeResponseJsonWithOutPhone))
        )

        val expectedResult = NonEmptySet.of(CustomsOffice("ID1", "NAME001", "GB", None))

        connector.getCustomsOffice(code).futureValue mustBe expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCustomsOffice(code))
      }

      "should handle client and server errors for customs office end point" in {
        checkErrorResponse(url, connector.getCustomsOffice(code))
      }
    }

    "getCusCode" - {
      val cusCode = "0010001-6"
      val url     = s"/$baseUrl/lists/CUSCode?data.code=$cusCode"

      "must return CUSCode when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(cusCodeResponseJson))
        )

        val expectedResult: NonEmptySet[CUSCode] = NonEmptySet.of(
          CUSCode(cusCode)
        )

        connector.getCUSCode(cusCode).futureValue mustEqual expectedResult
      }
    }

    "getCountryByCode" - {
      val url = s"/$baseUrl/lists/CountryCodesFullList?data.code=$countryCode"

      "should handle a 200 response" in {

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countryCodeResponseJson))
        )

        val expectedResult = NonEmptySet.of(Country(countryCode, "United Kingdom"))

        connector.getCountryNameByCode(countryCode).futureValue mustBe expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCountryNameByCode(countryCode))
      }

      "should handle a error response" in {
        checkErrorResponse(url, connector.getCountryNameByCode(countryCode))
      }
    }

    "getPackageTypes" - {
      val url = s"/$baseUrl/lists/KindOfPackages"

      "should handle a 200 response for countries" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(packageListResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          PackageType("1A", Some("Drum, aluminum")),
          PackageType("1B", Some("Drum, plywood"))
        )

        connector.getPackageTypes.futureValue mustBe expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getPackageTypes)
      }

      "should handle client and server errors for countries" in {
        checkErrorResponse(url, connector.getPackageTypes)
      }
    }

    "getSupportingDocument" - {
      val typeValue = "C641"
      val url       = s"/$baseUrl/lists/SupportingDocumentType?data.code=$typeValue"

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(supportingDocumentResponseJson))
        )

        val expectedResult: DocumentType = DocumentType(Support, typeValue, "Dissostichus - catch document import")

        connector.getSupportingDocument(typeValue).futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getSupportingDocument(typeValue))
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getSupportingDocument(typeValue))
      }
    }

    "getPreviousDocument" - {
      val typeValue = "C512"
      val url       = s"/$baseUrl/lists/PreviousDocumentType?data.code=$typeValue"

      "must return previous document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(previousDocumentResponseJson))
        )

        val expectedResult: DocumentType =
          DocumentType(Previous, typeValue, "SDE - Authorisation to use simplified declaration (Column 7a, Annex A of Delegated Regulation (EU) 2015/2446)")

        connector.getPreviousDocument(typeValue).futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getPreviousDocument(typeValue))
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getPreviousDocument(typeValue))
      }
    }

    "getAdditionalReferences" - {
      val url = s"/$baseUrl/lists/AdditionalReference"

      "must return Seq of AdditionalReference when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(additionalReferenceJson))
        )

        val expectedResult: NonEmptySet[AdditionalReferenceType] = NonEmptySet.of(
          AdditionalReferenceType("documentType1", "desc1"),
          AdditionalReferenceType("documentType2", "desc2")
        )

        connector.getAdditionalReferences().futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getAdditionalReferences())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getAdditionalReferences())
      }
    }

    "getPackageType" - {
      val documentType = "1A"
      val url          = s"/$baseUrl/lists/KindOfPackages?data.code=$documentType"

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(packageTypeResponseJson))
        )

        val expectedResult: PackageType = PackageType(documentType, Some("Drum, steel"))

        connector.getPackageType(documentType).futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getPackageType(documentType))
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getPackageType(documentType))
      }
    }

    "getIncidentType" - {
      val code = "1"
      val url  = s"/$baseUrl/lists/IncidentCode?data.code=$code"

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(incidentResponseJson))
        )

        val expectedResult: Incident = Incident(code, "The carrier is obligated to deviate from the…")

        connector.getIncidentType(code).futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getIncidentType(code))
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getIncidentType(code))
      }
    }

    "getAdditionalReferenceType" - {
      val documentType = "Y023"
      val url          = s"/$baseUrl/lists/AdditionalReference?data.documentType=$documentType"

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(additionalReferenceResponseJson))
        )

        val expectedResult: AdditionalReferenceType = AdditionalReferenceType(documentType, "Consignee (AEO certificate number)")

        connector.getAdditionalReferenceType(documentType).futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getAdditionalReferenceType(documentType))
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getAdditionalReferenceType(documentType))
      }
    }

    "getAdditionalInformationType" - {
      val code = "20300"
      val url  = s"/$baseUrl/lists/AdditionalInformation?data.code=$code"

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(additionalInformationJson))
        )

        val expectedResult: AdditionalInformationCode = AdditionalInformationCode(code, "Export")

        connector.getAdditionalInformationCode(code).futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getAdditionalInformationCode(code))
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getAdditionalInformationCode(code))
      }
    }

    "getQualifierOfIdentificationIncident" - {
      val qualifier = "U"
      val url       = s"/$baseUrl/lists/QualifierOfIdentificationIncident?data.qualifier=$qualifier"

      "must return supporting document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(qualifierOfIdentificationResponseJson))
        )

        val expectedResult: QualifierOfIdentification = QualifierOfIdentification(qualifier, "UN/LOCODE")

        connector.getQualifierOfIdentificationIncident(qualifier).futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getQualifierOfIdentificationIncident(qualifier))
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getQualifierOfIdentificationIncident(qualifier))
      }
    }

    "getTransportDocument" - {
      val typeValue = "N235"
      val url       = s"/$baseUrl/lists/TransportDocumentType?data.code=$typeValue"

      "must return transport document when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(transportDocumentResponseJson))
        )

        val expectedResult: DocumentType = DocumentType(Transport, typeValue, "Container list")

        connector.getTransportDocument(typeValue).futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getTransportDocument(typeValue))
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getTransportDocument(typeValue))
      }
    }

    "getTransportDocuments" - {
      val url = s"/$baseUrl/lists/TransportDocumentType"

      "must return list of documents when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(documentsJson("TransportDocumentType")))
        )

        val expectResult = NonEmptySet.of(
          DocumentType(Transport, "1", "Document 1"),
          DocumentType(Transport, "4", "Document 2")
        )

        connector.getTransportDocuments().futureValue mustEqual expectResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getTransportDocuments())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getTransportDocuments())
      }
    }

    "getSupportingDocuments" - {
      val url = s"/$baseUrl/lists/SupportingDocumentType"

      "must return list of documents when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(documentsJson("SupportingDocumentType")))
        )

        val expectResult = NonEmptySet.of(
          DocumentType(Support, "1", "Document 1"),
          DocumentType(Support, "4", "Document 2")
        )

        connector.getSupportingDocuments().futureValue mustEqual expectResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getSupportingDocuments())
      }

      "must return an exception when an error response is returned" in {
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
              .willReturn(okJson(responseJson))
          )

          val expectedResult = NonEmptySet.of(
            InlandMode("1", "Maritime Transport"),
            InlandMode("2", "Rail Transport")
          )

          connector.getTransportModeCodes[InlandMode].futureValue mustEqual expectedResult
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          checkNoReferenceDataFoundResponse(url, connector.getTransportModeCodes[InlandMode])
        }

        "must return an exception when an error response is returned" in {
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
              .willReturn(okJson(responseJson))
          )

          val expectedResult = NonEmptySet.of(
            BorderMode("1", "Maritime Transport"),
            BorderMode("2", "Rail Transport")
          )

          connector.getTransportModeCodes[BorderMode].futureValue mustEqual expectedResult
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          checkNoReferenceDataFoundResponse(url, connector.getTransportModeCodes[BorderMode])
        }

        "must return an exception when an error response is returned" in {
          checkErrorResponse(url, connector.getTransportModeCodes[BorderMode])
        }
      }
    }

    "getPreviousDocuments" - {
      val url = s"/$baseUrl/lists/PreviousDocumentType"

      "must return list of documents when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(documentsJson("PreviousDocumentType")))
        )

        val expectResult = NonEmptySet.of(
          DocumentType(Support, "1", "Document 1"),
          DocumentType(Support, "4", "Document 2")
        )

        connector.getPreviousDocuments().futureValue mustEqual expectResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getPreviousDocuments())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getPreviousDocuments())
      }
    }
  }

  private def checkNoReferenceDataFoundResponse(url: String, result: => Future[_]): Assertion = {
    server.stubFor(
      get(urlEqualTo(url))
        .willReturn(okJson(emptyResponseJson))
    )

    whenReady[Throwable, Assertion](result.failed) {
      _ mustBe a[NoReferenceDataFoundException]
    }
  }

  private def checkErrorResponse(url: String, result: => Future[_]): Assertion = {
    val errorResponses: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)

    forAll(errorResponses) {
      errorResponse =>
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(errorResponse)
            )
        )

        whenReady[Throwable, Assertion](result.failed) {
          _ mustBe an[Exception]
        }
    }
  }
}

object ReferenceDataConnectorSpec {

  private val additionalInformationResponseJson: String =
    """
      |{
      |  "data": [
      |    {
      |      "code": "10600",
      |      "description": "Negotiable Bill of lading 'to order blank endorsed'"
      |    }
      |  ]
      |}
      |""".stripMargin

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

  private val customsOfficeResponseJsonWithPhone: String =
    """
      |{
      | "data" :
      | [
      |  {
      |    "id":"ID1",
      |    "name":"NAME001",
      |    "countryId":"GB",
      |    "phoneNumber":"004412323232345"
      |  }
      | ]
      |}
      |""".stripMargin

  private val customsOfficeResponseJsonWithOutPhone: String =
    """
      |{
      | "data" :
      | [
      |  {
      |    "id":"ID1",
      |    "name":"NAME001",
      |    "countryId":"GB"
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

  private val emptyResponseJson: String =
    """
      |{
      |  "data": []
      |}
      |""".stripMargin
}
