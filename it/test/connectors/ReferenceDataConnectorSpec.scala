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
import play.api.test.Helpers.running

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends ItSpecBase with WireMockServerHandler with ScalaCheckPropertyChecks with EitherValues {

  private val baseUrl = "customs-reference-data/test-only"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.customs-reference-data.port" -> server.port())

  private lazy val phase5App: GuiceApplicationBuilder => GuiceApplicationBuilder =
    _ => guiceApplicationBuilder().configure("feature-flags.phase-6-enabled" -> false)

  private lazy val phase6App: GuiceApplicationBuilder => GuiceApplicationBuilder =
    _ => guiceApplicationBuilder().configure("feature-flags.phase-6-enabled" -> true)

  private lazy val asyncCacheApi: AsyncCacheApi = app.injector.instanceOf[AsyncCacheApi]

  override def beforeEach(): Unit = {
    super.beforeEach()
    asyncCacheApi.removeAll().futureValue
  }

  "Reference Data" - {

    "getCountries" - {
      val url = s"/$baseUrl/lists/CountryCodesFullList"

      "when phase 5" - {

        val countryListResponseJson: String =
          """
            |{
            |  "data": [
            |    {
            |      "code":"GB",
            |      "state":"valid",
            |      "description":"United Kingdom"
            |    },
            |    {
            |      "code":"AD",
            |      "state":"valid",
            |      "description":"Andorra"
            |    }
            |  ]
            |}
            |""".stripMargin

        "should handle a 200 response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getCountries())
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getCountries())
          }
        }
      }

      "when phase 6" - {

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getCountries())
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getCountries())
          }
        }
      }
    }

    "getCountry" - {
      val code = "GB"

      "when phase 5" - {

        val url = s"/$baseUrl/lists/CountryCodesFullList?data.code=$code"

        val countryResponseJson: String =
          """
            |{
            |  "data": [
            |    {
            |      "code":"GB",
            |      "state":"valid",
            |      "description":"United Kingdom"
            |    }
            |  ]
            |}
            |""".stripMargin

        "should handle a 200 response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getCountry(code))
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getCountry(code))
          }
        }
      }

      "when phase 6" - {

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getCountry(code))
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getCountry(code))
          }
        }
      }
    }

    "getSecurityType" - {
      val code = "GB"

      "when phase 5" - {
        val url = s"/$baseUrl/lists/DeclarationTypeSecurity?data.code=$code"

        val securityTypeResponseJson: String =
          """
            |{
            |  "data": [
            |    {
            |      "code":"1",
            |      "state":"valid",
            |      "description":"description"
            |    }
            |  ]
            |}
            |""".stripMargin

        "should handle a 200 response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getSecurityType(code))
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getSecurityType(code))
          }
        }
      }

      "when phase 6" - {
        val url = s"/$baseUrl/lists/DeclarationTypeSecurity?keys=$code"

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getSecurityType(code))
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getSecurityType(code))
          }
        }
      }
    }

    "getCustomsOffice" - {
      val code = "GB00001"

      "when phase 5" - {
        val url = s"/$baseUrl/lists/CustomsOffices?data.id=$code"

        val customsOfficeResponseJson: String =
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

        "should handle a 200 response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getCustomsOffice(code))
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getCustomsOffice(code))
          }
        }
      }
    }

    "getCusCode" - {
      val cusCode = "0010001-6"

      "when phase 5" - {

        val url = s"/$baseUrl/lists/CUSCode?data.code=$cusCode"

        val cusCodeResponseJson: String =
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

        "must return CUSCode when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getCUSCode(cusCode))
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getCUSCode(cusCode))
          }
        }
      }

      "when phase 6" - {

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getCUSCode(cusCode))
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getCUSCode(cusCode))
          }
        }
      }
    }

    "getPackageTypes" - {
      val url = s"/$baseUrl/lists/KindOfPackages"

      "when phase 5" - {

        val packageListResponseJson: String =
          """
            |{
            |  "data": [
            |    {
            |      "code":"1A",
            |      "description":"Drum, aluminum"
            |    },
            |    {
            |      "code":"1B",
            |      "description":"Drum, plywood"
            |    }
            |  ]
            |}
            |""".stripMargin

        "should handle a 200 response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getPackageTypes)
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getPackageTypes)
          }
        }
      }

      "when phase 6" - {

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getPackageTypes)
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getPackageTypes)
          }
        }
      }
    }

    "getSupportingDocument" - {
      val typeValue = "C641"

      "when phase 5" - {

        val url = s"/$baseUrl/lists/SupportingDocumentType?data.code=$typeValue"

        val supportingDocumentResponseJson: String =
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

        "must return supporting document when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getSupportingDocument(typeValue))
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getSupportingDocument(typeValue))
          }
        }
      }

      "when phase 6" - {

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getSupportingDocument(typeValue))
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getSupportingDocument(typeValue))
          }
        }
      }
    }

    "getPreviousDocument" - {
      val typeValue = "C512"

      "when phase 5" - {

        val url = s"/$baseUrl/lists/PreviousDocumentType?data.code=$typeValue"

        val previousDocumentResponseJson: String =
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

        "must return previous document when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(previousDocumentResponseJson))
              )

              val expectedResult: DocumentType =
                DocumentType(Previous,
                             typeValue,
                             "SDE - Authorisation to use simplified declaration (Column 7a, Annex A of Delegated Regulation (EU) 2015/2446)"
                )

              connector.getPreviousDocument(typeValue).futureValue.value mustEqual expectedResult
              server.resetMappings()
              connector.getPreviousDocument(typeValue).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getPreviousDocument(typeValue))
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getPreviousDocument(typeValue))
          }
        }
      }

      "when phase 6" - {

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(previousDocumentResponseJson))
              )

              val expectedResult: DocumentType =
                DocumentType(Previous,
                             typeValue,
                             "SDE - Authorisation to use simplified declaration (Column 7a, Annex A of Delegated Regulation (EU) 2015/2446)"
                )

              connector.getPreviousDocument(typeValue).futureValue.value mustEqual expectedResult
              server.resetMappings()
              connector.getPreviousDocument(typeValue).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getPreviousDocument(typeValue))
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getPreviousDocument(typeValue))
          }
        }
      }
    }

    "getPreviousDocumentExport" - {
      val typeValue = "C512"

      "when phase 5" - {

        val url = s"/$baseUrl/lists/PreviousDocumentExportType?data.code=$typeValue"

        val previousDocumentResponseJson: String =
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

        "must return previous document when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(previousDocumentResponseJson))
              )

              val expectedResult: DocumentType =
                DocumentType(Previous,
                             typeValue,
                             "SDE - Authorisation to use simplified declaration (Column 7a, Annex A of Delegated Regulation (EU) 2015/2446)"
                )

              connector.getPreviousDocumentExport(typeValue).futureValue.value mustEqual expectedResult
              server.resetMappings()
              connector.getPreviousDocumentExport(typeValue).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getPreviousDocumentExport(typeValue))
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getPreviousDocumentExport(typeValue))
          }
        }
      }

      "when phase 6" - {

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(previousDocumentResponseJson))
              )

              val expectedResult: DocumentType =
                DocumentType(Previous,
                             typeValue,
                             "SDE - Authorisation to use simplified declaration (Column 7a, Annex A of Delegated Regulation (EU) 2015/2446)"
                )

              connector.getPreviousDocumentExport(typeValue).futureValue.value mustEqual expectedResult
              server.resetMappings()
              connector.getPreviousDocumentExport(typeValue).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getPreviousDocumentExport(typeValue))
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getPreviousDocumentExport(typeValue))
          }
        }
      }
    }

    "getAdditionalReferences" - {
      val url = s"/$baseUrl/lists/AdditionalReference"

      "when phase 5" - {

        val additionalReferenceJson: String =
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
            |    {
            |      "documentType": "documentType1",
            |      "description": "desc1"
            |    },
            |    {
            |      "documentType": "documentType2",
            |      "description": "desc2"
            |    }
            |  ]
            |}
            |""".stripMargin

        "must return Seq of AdditionalReference when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getAdditionalReferences())
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getAdditionalReferences())
          }
        }
      }

      "when phase 6" - {

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getAdditionalReferences())
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getAdditionalReferences())
          }
        }
      }
    }

    "getPackageType" - {
      val documentType = "1A"

      "when phase 5" - {

        val url = s"/$baseUrl/lists/KindOfPackages?data.code=$documentType"

        val packageTypeResponseJson: String =
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

        "must return supporting document when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getPackageType(documentType))
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getPackageType(documentType))
          }
        }
      }

      "when phase 6" - {

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getPackageType(documentType))
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getPackageType(documentType))
          }
        }
      }
    }

    "getIncidentType" - {
      val code = "1"

      "when phase 5" - {
        val url = s"/$baseUrl/lists/IncidentCode?data.code=$code"

        val incidentResponseJson: String =
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

        "must return supporting document when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getIncidentType(code))
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getIncidentType(code))
          }
        }
      }

      "when phase 6" - {
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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getIncidentType(code))
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getIncidentType(code))
          }
        }
      }
    }

    "getAdditionalReferenceType" - {
      val documentType = "Y023"

      "when phase 5" - {
        val url = s"/$baseUrl/lists/AdditionalReference?data.documentType=$documentType"

        val additionalReferenceResponseJson: String =
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

        "must return supporting document when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getAdditionalReference(documentType))
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getAdditionalReference(documentType))
          }
        }
      }

      "when phase 6" - {
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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getAdditionalReference(documentType))
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getAdditionalReference(documentType))
          }
        }
      }
    }

    "getAdditionalInformationType" - {
      val code = "20300"

      "when phase 5" - {
        val url = s"/$baseUrl/lists/AdditionalInformation?data.code=$code"

        val additionalInformationJson: String =
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
            |    {
            |      "code": "20300",
            |      "description": "Export"
            |    },
            |    {
            |      "code": "30600",
            |      "description": "In EXS, where negotiable bills of lading 'to order blank endorsed' are concerned and the consignee particulars are unknown."
            |    }
            |  ]
            |}
            |""".stripMargin

        "must return supporting document when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getAdditionalInformationCode(code))
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getAdditionalInformationCode(code))
          }
        }
      }

      "when phase 6" - {
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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getAdditionalInformationCode(code))
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getAdditionalInformationCode(code))
          }
        }
      }
    }

    "getQualifierOfIdentificationIncident" - {
      val qualifier = "U"

      "when phase 5" - {
        val url = s"/$baseUrl/lists/QualifierOfIdentificationIncident?data.qualifier=$qualifier"

        val qualifierOfIdentificationResponseJson: String =
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

        "must return supporting document when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getQualifierOfIdentificationIncident(qualifier))
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getQualifierOfIdentificationIncident(qualifier))
          }
        }
      }

      "when phase 6" - {
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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getQualifierOfIdentificationIncident(qualifier))
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getQualifierOfIdentificationIncident(qualifier))
          }
        }
      }
    }

    "getTransportDocument" - {
      val typeValue = "N235"

      "when phase 5" - {
        val url = s"/$baseUrl/lists/TransportDocumentType?data.code=$typeValue"

        val transportDocumentResponseJson: String =
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

        "must return transport document when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getTransportDocument(typeValue))
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getTransportDocument(typeValue))
          }
        }
      }

      "when phase 6" - {
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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getTransportDocument(typeValue))
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getTransportDocument(typeValue))
          }
        }
      }
    }

    "getTransportDocuments" - {
      val url = s"/$baseUrl/lists/TransportDocumentType"

      "when phase 5" - {

        val documentsJson: String =
          s"""
             |{
             |"_links": {
             |    "self": {
             |      "href": "/customs-reference-data/lists/TransportDocumentType"
             |    }
             |  },
             |  "meta": {
             |    "version": "410157ad-bc37-4e71-af2a-404d1ddad94c",
             |    "snapshotDate": "2023-01-01"
             |  },
             |  "id": "TransportDocumentType",
             |  "data": [
             |    {
             |      "code": "1",
             |      "description": "Document 1"
             |    },
             |    {
             |      "code": "4",
             |      "description": "Document 2"
             |    }
             |  ]
             |}
             |""".stripMargin

        "must return list of documents when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(documentsJson))
              )

              val expectResult = NonEmptySet.of(
                DocumentType(Transport, "1", "Document 1"),
                DocumentType(Transport, "4", "Document 2")
              )

              connector.getTransportDocuments().futureValue.value mustEqual expectResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getTransportDocuments())
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getTransportDocuments())
          }
        }
      }

      "when phase 6" - {

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getTransportDocuments())
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getTransportDocuments())
          }
        }
      }
    }

    "getSupportingDocuments" - {
      val url = s"/$baseUrl/lists/SupportingDocumentType"

      "when phase 5" - {
        val documentsJson: String =
          s"""
             |{
             |  "_links": {
             |    "self": {
             |      "href": "/customs-reference-data/lists/SupportingDocumentType"
             |    }
             |  },
             |  "meta": {
             |    "version": "410157ad-bc37-4e71-af2a-404d1ddad94c",
             |    "snapshotDate": "2023-01-01"
             |  },
             |  "id": "SupportingDocumentType",
             |  "data": [
             |    {
             |      "code": "1",
             |      "description": "Document 1"
             |    },
             |    {
             |      "code": "4",
             |      "description": "Document 2"
             |    }
             |  ]
             |}
             |""".stripMargin

        "must return list of documents when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(documentsJson))
              )

              val expectResult = NonEmptySet.of(
                DocumentType(Support, "1", "Document 1"),
                DocumentType(Support, "4", "Document 2")
              )

              connector.getSupportingDocuments().futureValue.value mustEqual expectResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getSupportingDocuments())
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getSupportingDocuments())
          }
        }
      }

      "when phase 6" - {
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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getSupportingDocuments())
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getSupportingDocuments())
          }
        }
      }
    }

    "getTransportModeCode" - {
      val code = "1"

      "when phase 5" - {
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

          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getTransportModeCode(code))
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getTransportModeCode(code))
          }
        }
      }

      "when phase 6" - {
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

          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getTransportModeCode(code))
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getTransportModeCode(code))
          }
        }
      }
    }

    "getPreviousDocuments" - {
      val url = s"/$baseUrl/lists/PreviousDocumentType"

      "when phase 5" - {

        val documentsJson: String =
          s"""
             |{
             |  "_links": {
             |    "self": {
             |      "href": "/customs-reference-data/lists/PreviousDocumentType"
             |    }
             |  },
             |  "meta": {
             |    "version": "410157ad-bc37-4e71-af2a-404d1ddad94c",
             |    "snapshotDate": "2023-01-01"
             |  },
             |  "id": "PreviousDocumentType",
             |  "data": [
             |    {
             |      "code": "1",
             |      "description": "Document 1"
             |    },
             |    {
             |      "code": "4",
             |      "description": "Document 2"
             |    }
             |  ]
             |}
             |""".stripMargin

        "must return list of documents when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(documentsJson))
              )

              val expectResult = NonEmptySet.of(
                DocumentType(Previous, "1", "Document 1"),
                DocumentType(Previous, "4", "Document 2")
              )

              connector.getPreviousDocuments().futureValue.value mustEqual expectResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getPreviousDocuments())
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getPreviousDocuments())
          }
        }
      }

      "when phase 6" - {

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getPreviousDocuments())
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getPreviousDocuments())
          }
        }
      }
    }

    "getDocumentTypeExcise" - {
      val code = "C651"

      "when phase 5" - {
        val url = s"/$baseUrl/lists/DocumentTypeExcise?data.code=$code"

        val documentTypeExciseJson: String =
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
            |    {
            |      "activeFrom": "2024-01-01",
            |      "code": "C651",
            |      "state": "valid",
            |      "description": "AAD - Administrative Accompanying Document (EMCS)"
            |    },
            |    {
            |      "activeFrom": "2024-01-01",
            |      "code": "C658",
            |      "state": "valid",
            |      "description": "FAD - Fallback e-AD (EMCS)"
            |    }
            |  ]
            |}
            |""".stripMargin

        "must return DocumentTypeExcise when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getDocumentTypeExcise(code))
          }
        }

        "should handle client and server errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getDocumentTypeExcise(code))
          }
        }
      }

      "when phase 6" - {
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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getDocumentTypeExcise(code))
          }
        }

        "should handle client and server errors" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getDocumentTypeExcise(code))
          }
        }
      }
    }
  }

  private def checkNoReferenceDataFoundResponse(url: String, json: String, result: => Future[Either[Exception, ?]]): Assertion = {
    server.stubFor(
      get(urlEqualTo(url))
        .willReturn(okJson(json))
    )

    result.futureValue.left.value mustBe a[NoReferenceDataFoundException]
  }

  private def checkErrorResponse(url: String, result: => Future[Either[Exception, ?]]): Assertion = {
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

        result.futureValue.left.value mustBe an[Exception]
    }
  }
}

object ReferenceDataConnectorSpec {

  private val emptyPhase5ResponseJson: String =
    """
      |{
      |  "data": []
      |}
      |""".stripMargin

  private val emptyPhase6ResponseJson: String =
    """
      |[]
      |""".stripMargin
}
