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
import models.reference.{Country, CustomsOffice}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataServiceImpl @Inject() (connector: ReferenceDataConnector) extends ReferenceDataService {

  def getCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[Country]] =
    connector.getCountries().map(sort)

  def getCountryByCode(code: Option[String])(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[Country]] =
    code match {
      case Some(countryCode) => getCountries().map(_.find(_.code.equals(countryCode)))
      case None              => Future.successful(None)
    }

  def getCustomsOfficeByCode(customsOfficeCode: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[CustomsOffice]] = {
    val customsOfficesResult: Future[NonEmptySet[CustomsOffice]] = connector.getCustomsOffice(customsOfficeCode)

    customsOfficesResult.flatMap(
      offices => Future.successful(offices.toSeq.headOption)
    )
  }

  def getCountryNameByCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[String] = {
    val countriesResult: Future[NonEmptySet[Country]] = connector.getCountryNameByCode(code)

    countriesResult.flatMap(
      countries => Future.successful(countries.head.code)
    )
  }

  private def sort(countries: NonEmptySet[Country]): Seq[Country] =
    countries.toSeq.sortBy(
      country =>
        country.description match {
          case Some(desc) => desc.toLowerCase()
          case None       => country.code
        }
    )
}

trait ReferenceDataService {
  def getCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[Country]]
  def getCountryByCode(code: Option[String])(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[Country]]

  def getCustomsOfficeByCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[CustomsOffice]]

  def getCountryNameByCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[String]
}
