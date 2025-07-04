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

package utils.answersHelpers.consignment.incident

import generated.{Flag, TranshipmentType}
import models.reference.{Country, Incident, QualifierOfIdentification}
import models.{Coordinates, DynamicAddress, Index, UserAnswers}
import pages.incident.endorsement.EndorsementCountryPage
import pages.incident.location.*
import pages.incident.{IncidentCodePage, IncidentTextPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper
import utils.answersHelpers.consignment.replacementMeansOfTransport.ReplacementMeansOfTransportAnswersHelper
import viewModels.sections.Section
import viewModels.sections.Section.AccordionSection

class IncidentAnswersHelper(userAnswers: UserAnswers, incidentIndex: Index)(implicit messages: Messages) extends AnswersHelper(userAnswers) {

  def incidentCountryRow: Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = CountryPage(incidentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.incident.country",
    args = incidentIndex.display,
    id = None,
    call = None
  )

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

  def incidentQualifierRow: Option[SummaryListRow] = getAnswerAndBuildRow[QualifierOfIdentification](
    page = QualifierOfIdentificationPage(incidentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.incident.qualifier",
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

  def incidentCoordinatesRow: Option[SummaryListRow] =
    userAnswers.ie043Data.Consignment.flatMap(_.Incident.lift(incidentIndex.position).flatMap(_.Location.GNSS)).map {
      gnss =>
        buildRowWithNoChangeLink(
          prefix = "unloadingFindings.incident.coordinates",
          answer = formatAsText(Coordinates(gnss.latitude, gnss.longitude))
        )
    }

  def incidentUnLocodeRow: Option[SummaryListRow] =
    userAnswers.ie043Data.Consignment.flatMap(_.Incident.lift(incidentIndex.position).flatMap(_.Location.UNLocode)).map {
      unlocode =>
        buildRowWithNoChangeLink(
          prefix = "unloadingFindings.incident.unLocode",
          answer = formatAsText(unlocode)
        )
    }

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
          answer = formatAsHtmlContent(dynamicAddress)
        )
    }

  def containerIndicator: Option[SummaryListRow] = buildRowWithNoChangeLink[Flag](
    data = userAnswers.ie043Data.Consignment.map(_.containerIndicator),
    formatAnswer = formatAsYesOrNo,
    prefix = "unloadingFindings.rowHeadings.containerIndicator"
  )

  def incidentTransportEquipments: Section =
    userAnswers.ie043Data.Consignment
      .flatMap(_.Incident.lift(incidentIndex.position))
      .map(_.TransportEquipment)
      .toSeq
      .flatten
      .zipWithIndex
      .map {
        case (transportEquipment, index) =>
          val equipmentIndex = Index(index)
          val helper         = new IncidentTransportEquipmentAnswersHelper(userAnswers, transportEquipment)

          val rows = Seq(helper.containerIdentificationNumber).flatten

          val children = Seq(
            helper.transportEquipmentSeals,
            helper.itemNumbers
          )

          AccordionSection(
            sectionTitle = Some(messages("unloadingFindings.incident.transportEquipment.heading", equipmentIndex.display)),
            rows = rows,
            children = children,
            id = Some(s"incident-$incidentIndex-transport-equipment-$index")
          )
      } match {
      case children =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.incident.transportEquipment.parent.heading")),
          children = children
        )
    }

  def incidentReplacementMeansOfTransport: Seq[SummaryListRow] = {
    val transhipment: Option[TranshipmentType] = userAnswers.ie043Data.Consignment
      .flatMap(_.Incident.lift(incidentIndex.position))
      .flatMap(_.Transhipment)

    val helper = new ReplacementMeansOfTransportAnswersHelper(userAnswers, transhipment, incidentIndex)

    Seq(
      helper.typeOfIdentification,
      helper.identificationNumber,
      helper.nationality
    ).flatten

  }

}
