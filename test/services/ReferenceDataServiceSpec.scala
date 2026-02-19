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

package services

import base.SpecBase
import cats.data.NonEmptySet
import config.FrontendAppConfig
import connectors.ReferenceDataConnector
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import models.DocType.*
import models.SelectableList
import models.reference.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockFrontendAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  private val mockConnector: ReferenceDataConnector = mock[ReferenceDataConnector]

  private val service = new ReferenceDataService(mockFrontendAppConfig, mockConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  "ReferenceDataService" - {

    "countries" - {

      val uk = Country("GB", "United Kingdom")
      val andorra = Country("AD", "Andorra")
      val france = Country("FR", "France")

      val countries = NonEmptySet.of(uk, andorra, france)

      "getCountries should" - {
        "return a list of sorted countries" in {

          when(mockConnector.getCountries()(any(), any()))
            .thenReturn(Future.successful(Right(countries)))

          service.getCountries().futureValue mustEqual
            Seq(andorra, france, uk)

          verify(mockConnector).getCountries()(any(), any())
        }
      }

      "getCountry should" - {
        "return Country if country code exists" in {

          when(mockConnector.getCountry("GB"))
            .thenReturn(Future.successful(Right(uk)))

          service.getCountry("GB").futureValue mustEqual Country("GB", "United Kingdom")
        }
      }
    }

    "customs offices" - {

      val customsOffice = CustomsOffice("ID1", "NAME001", "GB", None)

      "getCustomsOffice should" - {
        "return a customsOffice" in {

          when(mockConnector.getCustomsOffice(any())(any(), any()))
            .thenReturn(Future.successful(Right(customsOffice)))

          service.getCustomsOffice("GB00001").futureValue mustEqual customsOffice

          verify(mockConnector).getCustomsOffice(any())(any(), any())
        }
      }
    }

    "CUS codes" - {

      val cusCode = CUSCode("0010001-6")

      "doesCUSCodeExist should" - {
        "return true when cusCode exists" in {
          when(mockFrontendAppConfig.disableCusCodeLookup).thenReturn(false)

          when(mockConnector.getCUSCode(any())(any(), any()))
            .thenReturn(Future.successful(Right(cusCode)))

          service.doesCUSCodeExist(cusCode.code).futureValue mustEqual true

          verify(mockConnector).getCUSCode(eqTo(cusCode.code))(any(), any())
        }

        "return true when lookup disabled and is the correct format" in {
          when(mockFrontendAppConfig.disableCusCodeLookup).thenReturn(true)
          service.doesCUSCodeExist(cusCode.code).futureValue mustEqual true
          verifyNoInteractions(mockConnector)
        }
        
        "return false when lookup disabled and is the incorrect format" in {
          when(mockFrontendAppConfig.disableCusCodeLookup).thenReturn(true)
          service.doesCUSCodeExist("InvalidValue").futureValue mustEqual false
          verifyNoInteractions(mockConnector)
        }

        "return false when cusCode does not exist" in {
          when(mockFrontendAppConfig.disableCusCodeLookup).thenReturn(false)
          
          when(mockConnector.getCUSCode(any())(any(), any()))
            .thenReturn(Future.successful(Left(new NoReferenceDataFoundException(""))))

          service.doesCUSCodeExist(cusCode.code).futureValue mustEqual false

          verify(mockConnector).getCUSCode(eqTo(cusCode.code))(any(), any())
        }
      }
    }

    "HS codes" - {

      val code = HSCode("010121")

      "doesHSCodeExist should" - {
        "return true when cusCode exists" in {

          when(mockConnector.getHSCode(any())(any(), any()))
            .thenReturn(Future.successful(Right(code)))

          service.doesHSCodeExist(code.code).futureValue mustEqual true

          verify(mockConnector).getHSCode(eqTo(code.code))(any(), any())
        }

        "return false when cusCode does not exist" in {

          when(mockConnector.getHSCode(any())(any(), any()))
            .thenReturn(Future.successful(Left(new NoReferenceDataFoundException(""))))

          service.doesHSCodeExist(code.code).futureValue mustEqual false

          verify(mockConnector).getHSCode(eqTo(code.code))(any(), any())
        }
      }
    }

    "AdditionalReferences" - {

      val additionalReference1 = AdditionalReferenceType(
        "Y015",
        "The rough diamonds are contained in tamper-resistant containers"
      )

      val additionalReference2 = AdditionalReferenceType(
        "C658",
        "Consignor / exporter (AEO certificate number)"
      )

      val additionalReference3 = AdditionalReferenceType(
        "Y016",
        "Electronic administrative document (e-AD), as referred to in Article 3(1) of Reg. (EC) No 684/2009"
      )

      val additionalReferences = NonEmptySet.of(additionalReference2, additionalReference1, additionalReference3)

      val docTypeExcise = DocTypeExcise(
        "C651",
        "AAD - Administrative Accompanying Document (EMCS)"
      )

      "getAdditionalReferences" - {
        "must return a list of sorted countries" in {

          when(mockConnector.getAdditionalReferences()(any(), any()))
            .thenReturn(Future.successful(Right(additionalReferences)))

          service.getAdditionalReferences().futureValue mustEqual
            SelectableList(Seq(additionalReference2, additionalReference3, additionalReference1))

          verify(mockConnector).getAdditionalReferences()(any(), any())
        }
      }

      "isDocumentTypeExcise should" - {
        "return true when docTypeExcise" in {

          when(mockConnector.getDocumentTypeExcise(any())(any(), any()))
            .thenReturn(Future.successful(Right(docTypeExcise)))

          service.isDocumentTypeExcise("C651").futureValue mustEqual true

          verify(mockConnector).getDocumentTypeExcise(any())(any(), any())
        }

        "return false when it is not a docTypeExcise" in {
          val docType = "C656"

          when(mockConnector.getDocumentTypeExcise(any())(any(), any()))
            .thenReturn(Future.successful(Left(new NoReferenceDataFoundException(""))))

          service.isDocumentTypeExcise(docType).futureValue mustEqual false

          verify(mockConnector).getDocumentTypeExcise(eqTo(docType))(any(), any())
        }
      }
    }

    "Documents" - {

      val transportDocument1 = DocumentType(Transport, "N235", "Container list")
      val transportDocument2 = DocumentType(Transport, "N741", "Master airwaybill")
      val supportingDocument1 = DocumentType(Support, "C673", "Catch certificate")
      val supportingDocument2 = DocumentType(Support, "N941", "Embargo permit")

      val transportDocuments = NonEmptySet.of(transportDocument1, transportDocument2)
      val supportingDocuments = NonEmptySet.of(supportingDocument1, supportingDocument2)

      "getDocuments" - {
        "returns a list of transport and supporting documents" in {
          when(mockConnector.getTransportDocuments()(any(), any()))
            .thenReturn(Future.successful(Right(transportDocuments)))
          when(mockConnector.getSupportingDocuments()(any(), any()))
            .thenReturn(Future.successful(Right(supportingDocuments)))

          service.getDocuments().futureValue mustEqual
            Seq(supportingDocument1, transportDocument1, transportDocument2, supportingDocument2)

          verify(mockConnector).getTransportDocuments()(any(), any())
          verify(mockConnector).getSupportingDocuments()(any(), any())
        }
      }

      "getTransportDocuments" - {
        "returns a list of transport documents" in {
          when(mockConnector.getTransportDocuments()(any(), any()))
            .thenReturn(Future.successful(Right(transportDocuments)))

          service.getTransportDocuments().futureValue mustEqual
            Seq(transportDocument1, transportDocument2)

          verify(mockConnector).getTransportDocuments()(any(), any())
        }
      }

      "getSupportingDocuments" - {
        "returns a list of supporting documents" in {
          when(mockConnector.getSupportingDocuments()(any(), any()))
            .thenReturn(Future.successful(Right(supportingDocuments)))

          service.getSupportingDocuments().futureValue mustEqual
            Seq(supportingDocument1, supportingDocument2)

          verify(mockConnector).getSupportingDocuments()(any(), any())
        }
      }
    }

    "Means Of Transport Identification Types" - {

      val tm1 = TransportMeansIdentification("t1", "d1")
      val tm2 = TransportMeansIdentification("t2", "d2")
      val tm3 = TransportMeansIdentification("t3", "d3")

      val tms = NonEmptySet.of(tm1, tm2, tm3)

      "getMeansOfTransportIdentificationTypes" - {
        "returns a list of means of transport" in {
          when(mockConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
            .thenReturn(Future.successful(Right(tms)))

          service.getMeansOfTransportIdentificationTypes().futureValue mustEqual
            Seq(tm1, tm2, tm3)

          verify(mockConnector).getMeansOfTransportIdentificationTypes()(any(), any())
        }
      }
    }
  }
}
