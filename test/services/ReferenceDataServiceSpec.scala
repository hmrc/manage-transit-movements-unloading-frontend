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

import connectors.ReferenceDataConnector
import models.reference.Country
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataServiceSpec extends AnyFreeSpec with ScalaFutures with Matchers with MockitoSugar {

  private val mockConnector: ReferenceDataConnector = mock[ReferenceDataConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val uk      = Country("GB", "United Kingdom")
  private val andorra = Country("AD", "Andorra")
  private val france  = Country("FR", "France")

  private val countries = Seq(uk, andorra, france)

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

      "return None if country can't be found" in {

        when(mockConnector.getCountries()).thenReturn(Future.successful(Nil))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getCountryByCode(Some("GB")).futureValue mustBe None
      }

      "return None if country code is not passed in" in {

        when(mockConnector.getCountries()).thenReturn(Future.successful(Nil))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getCountryByCode(None).futureValue mustBe None
      }

      "return Country if country code exists" in {

        when(mockConnector.getCountries()).thenReturn(Future.successful(countries))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getCountryByCode(Some("GB")).futureValue mustBe Some(
          Country("GB", "United Kingdom")
        )
      }

    }

  }

}
