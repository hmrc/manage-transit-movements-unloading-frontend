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
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import models.reference.{Country, CustomsOffice}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataServiceImpl @Inject() (connector: ReferenceDataConnector) extends ReferenceDataService {

  def getCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[Country]] =
    connector.getCountries().map(_.toSeq)

  def getCountryByCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Country] =
    connector.getCountry(code)

  def getCustomsOfficeByCode(customsOfficeCode: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[CustomsOffice] =
    connector.getCustomsOffice(customsOfficeCode)

  def getCountryNameByCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[String] =
    connector.getCountryNameByCode(code).map(_.code)

  def doesCUSCodeExist(cusCode: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Boolean] =
    connector
      .getCUSCode(cusCode)
      .map(
        _ => true
      )
      .recover {
        case _: NoReferenceDataFoundException => false
      }
}

trait ReferenceDataService {
  def getCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[Country]]
  def getCountryByCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Country]
  def getCustomsOfficeByCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[CustomsOffice]
  def getCountryNameByCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[String]
  def doesCUSCodeExist(cusCode: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Boolean]
}
