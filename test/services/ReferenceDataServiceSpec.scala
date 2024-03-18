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

import cats.data.NonEmptySet
import connectors.ReferenceDataConnector
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import models.reference.{CUSCode, Country, CustomsOffice}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataServiceSpec extends AnyFreeSpec with ScalaFutures with Matchers with MockitoSugar with BeforeAndAfterEach {

  private val mockConnector: ReferenceDataConnector = mock[ReferenceDataConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val uk      = Country("GB", "United Kingdom")
  private val andorra = Country("AD", "Andorra")
  private val france  = Country("FR", "France")

  private val countries = NonEmptySet.of(uk, andorra, france)

  private val customsOffice = CustomsOffice("ID1", "NAME001", "GB", None)

  private val cusCode = CUSCode("0010001-6")

  override def beforeEach(): Unit = {
    reset(mockConnector)
    super.beforeEach()
  }

  "ReferenceDataService" - {

    "getCountries should" - {
      "return a list of sorted countries" in {

        when(mockConnector.getCountries()(any(), any())).thenReturn(Future.successful(countries))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getCountries().futureValue mustBe
          Seq(andorra, france, uk)

        verify(mockConnector).getCountries()(any(), any())
      }
    }

    "getCountryByCode should" - {

      "return Country if country code exists" in {

        when(mockConnector.getCountry("GB")).thenReturn(Future.successful(uk))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getCountryByCode("GB").futureValue mustBe Country("GB", "United Kingdom")
      }

    }

    "getCustomsOfficeByCode should" - {
      "return a customsOffice" in {

        when(mockConnector.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getCustomsOfficeByCode("GB00001").futureValue mustBe customsOffice

        verify(mockConnector).getCustomsOffice(any())(any(), any())
      }

    }

    "doesCUSCodeExist should" - {
      "return true when cusCode exists" in {

        when(mockConnector.getCUSCode(any())(any(), any())).thenReturn(Future.successful(NonEmptySet.of(cusCode)))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.doesCUSCodeExist("0010001-6").futureValue mustBe
          true

        verify(mockConnector).getCUSCode(any())(any(), any())
      }

      "return false when cusCode does not exist" in {
        val cusCode = "0010001-6"

        when(mockConnector.getCUSCode(any())(any(), any())).thenReturn(Future.failed(new NoReferenceDataFoundException("")))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.doesCUSCodeExist(cusCode).futureValue mustBe
          false

        verify(mockConnector).getCUSCode(ArgumentMatchers.eq(cusCode))(any(), any())
      }

    }
  }
}
