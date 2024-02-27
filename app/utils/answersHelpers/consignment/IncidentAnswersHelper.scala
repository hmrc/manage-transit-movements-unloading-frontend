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

package utils.answersHelpers.consignment

import models.reference.{Country, Incident}
import models.{DynamicAddress, Index, UserAnswers}
import pages.incident.endorsement.EndorsementCountryPage
import pages.incident.location.address.{AddressCityPage, AddressPostcodePage, AddressStreetAndNumberPage}
import pages.incident.{IncidentCodePage, IncidentTextPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper

class IncidentAnswersHelper(userAnswers: UserAnswers, incidentIndex: Index)(implicit messages: Messages) extends AnswersHelper(userAnswers) {

  def incidentCodeRow: Option[SummaryListRow] = getAnswerAndBuildRow[Incident](
    page = IncidentCodePage(incidentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.incident.code",
    args = incidentIndex.display,
    id = None,
    call = None
  )

  def incidentDescriptionRow: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = IncidentTextPage(incidentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.incident.description",
    args = incidentIndex.display,
    id = None,
    call = None
  )

  def incidentEndorsementDateRow: Option[SummaryListRow] = userAnswers.ie043Data.Consignment
    .flatMap(
      _.Incident
        .lift(incidentIndex.position)
        .flatMap(_.Endorsement.map(_.date))
    )
    .map {
      date =>
        buildRowWithNoChangeLink(
          prefix = "unloadingFindings.incident.endorsement.date",
          answer = formatAsDate(date)
        )
    }

  def incidentEndorsementAuthorityRow: Option[SummaryListRow] = userAnswers.ie043Data.Consignment
    .flatMap(
      _.Incident
        .lift(incidentIndex.position)
        .flatMap(_.Endorsement.map(_.authority))
    )
    .map {
      auth =>
        buildRowWithNoChangeLink(
          prefix = "unloadingFindings.incident.endorsement.authority",
          answer = formatAsText(auth)
        )
    }

  def incidentEndorsementPlaceRow: Option[SummaryListRow] = userAnswers.ie043Data.Consignment
    .flatMap(
      _.Incident
        .lift(incidentIndex.position)
        .flatMap(_.Endorsement.map(_.place))
    )
    .map {
      place =>
        buildRowWithNoChangeLink(
          prefix = "unloadingFindings.incident.endorsement.place",
          answer = formatAsText(place)
        )
    }

  def incidentEndorsementCountryRow: Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = EndorsementCountryPage(incidentIndex),
    formatAnswer = formatAsCountry,
    prefix = "unloadingFindings.incident.endorsement.country",
    args = incidentIndex.display,
    id = None,
    call = None
  )

  def incidentLocationAddressRow: Option[SummaryListRow] = userAnswers.ie043Data.Consignment
    .flatMap(
      _.Incident
        .lift(incidentIndex.position)
        .flatMap(
          _.Location.Address.map(
            add => DynamicAddress(add.streetAndNumber, add.city, add.postcode)
          )
        )
    )
    .map {
      dynamicAddress =>
        buildRowWithNoChangeLink(
          prefix = "unloadingFindings.incident.location.address",
          answer = formatAsDynamicAddress(dynamicAddress)
        )
    }

}
