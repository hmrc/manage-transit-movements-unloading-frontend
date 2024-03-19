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

package utils.answersHelpers

import models.DocType.Previous
import models.reference.CustomsOffice
import models.{CheckMode, Index, Link, NormalMode, RichOptionalJsArray, SecurityType, UserAnswers}
import pages.documents.TypePage
import pages.grossMass.GrossMassPage
import pages.sections._
import pages.sections.additionalInformation.AdditionalInformationListSection
import pages.sections.additionalReference.AdditionalReferencesSection
import pages.sections.documents.DocumentsSection
import pages.sections.incidents.IncidentsSection
import pages.{CustomsOfficeOfDestinationActualPage, SecurityTypePage}
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.answersHelpers.consignment._
import utils.answersHelpers.consignment.incident.IncidentAnswersHelper
import viewModels.sections.Section
import viewModels.sections.Section.{AccordionSection, StaticSection}

class ConsignmentAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) extends AnswersHelper(userAnswers) {

  def headerSection: Section = StaticSection(
    rows = Seq(
      declarationAcceptanceDateRow,
      declarationTypeRow,
      securityTypeRow,
      Some(reducedDatasetIndicatorRow),
      customsOfficeOfDestinationActual,
      grossMassRow,
      Some(traderAtDestinationRow)
    ).flatten
  )

  private def declarationTypeRow: Option[SummaryListRow] = userAnswers.ie043Data.TransitOperation.declarationType.map(
    dec =>
      buildRowWithNoChangeLink(
        prefix = "declarationType",
        answer = dec.toText
      )
  )

  private def securityTypeRow: Option[SummaryListRow] = getAnswerAndBuildRow[SecurityType](
    page = SecurityTypePage,
    formatAnswer = formatAsText,
    prefix = "securityType",
    id = None,
    call = None
  )

  private def reducedDatasetIndicatorRow: SummaryListRow = buildRowWithNoChangeLink(
    prefix = "reducedDatasetIndicator",
    answer = formatAsBoolean(userAnswers.ie043Data.TransitOperation.reducedDatasetIndicator.toString)
  )

  private def declarationAcceptanceDateRow: Option[SummaryListRow] = userAnswers.ie043Data.TransitOperation.declarationAcceptanceDate.map(
    dec =>
      buildRowWithNoChangeLink(
        prefix = "declarationAcceptanceDate",
        answer = formatAsDate(dec)
      )
  )

  def traderAtDestinationRow: SummaryListRow = buildRow(
    prefix = "traderAtDestination",
    answer = userAnswers.ie043Data.TraderAtDestination.identificationNumber.toText,
    id = None,
    call = None
  )

  def consignorSection: Option[Section] =
    userAnswers.ie043Data.Consignment.flatMap(_.Consignor).map {
      consignor =>
        val helper = new ConsignorAnswersHelper(userAnswers)
        StaticSection(
          sectionTitle = messages("unloadingFindings.consignor.heading"),
          rows = Seq(
            helper.identificationNumber(consignor.identificationNumber),
            helper.name(consignor.name),
            helper.country,
            helper.address(consignor.Address)
          ).flatten
        )
    }

  def consigneeSection: Option[Section] =
    userAnswers.ie043Data.Consignment.flatMap(_.Consignee).map {
      consignee =>
        val helper = new ConsigneeAnswersHelper(userAnswers)
        StaticSection(
          sectionTitle = messages("unloadingFindings.consignee.heading"),
          rows = Seq(
            helper.identificationNumber(consignee.identificationNumber),
            helper.name(consignee.name),
            helper.country,
            helper.address(consignee.Address)
          ).flatten
        )
    }

  def customsOfficeOfDestinationActual: Option[SummaryListRow] =
    getAnswerAndBuildRow[CustomsOffice](
      page = CustomsOfficeOfDestinationActualPage,
      formatAnswer = x => formatAsText(x.name),
      prefix = "customsOfficeOfDestinationActual",
      id = None,
      call = None
    )

  def holderOfTheTransitProcedureSection: Seq[Section] =
    userAnswers.ie043Data.HolderOfTheTransitProcedure.map {
      hotP =>
        val helper = new HolderOfTheTransitProcedureHelper(userAnswers)
        StaticSection(
          sectionTitle = Some(messages("unloadingFindings.rowHeadings.holderOfTheTransitProcedure.heading")),
          rows = Seq(
            helper.identificationNumber(hotP.identificationNumber),
            helper.name(hotP.name),
            helper.country,
            helper.address(hotP.Address),
            helper.tirHolderIdentificationNumber(hotP.TIRHolderIdentificationNumber)
          ).flatten
        )
    }.toList

  def grossMassRow: Option[SummaryListRow] = getAnswerAndBuildRow[BigDecimal](
    page = GrossMassPage,
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.grossMass",
    id = Some(s"change-gross-mass"),
    call = Some(Call(GET, "#"))
  )

  def departureTransportMeansSection: Section =
    userAnswers
      .get(TransportMeansListSection)
      .mapWithIndex {
        case (_, index) =>
          val helper = new DepartureTransportMeansAnswersHelper(userAnswers, index)
          val rows = Seq(
            helper.transportMeansID,
            helper.transportMeansNumber,
            helper.transportRegisteredCountry
          ).flatten
          (rows, index)
      } match {
      case Nil =>
        StaticSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.transportMeans.parent.header")),
          viewLinks = Seq(departureTransportMeansAddRemoveLink)
        )
      case sections =>
        val children = sections.map {
          case (rows, index) =>
            AccordionSection(
              sectionTitle = Some(messages("unloadingFindings.subsections.transportMeans", index.display)),
              rows
            )
        }
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.transportMeans.parent.header")),
          children = children,
          viewLinks = Seq(departureTransportMeansAddRemoveLink),
          id = Some("departureTransportMeans")
        )
    }

  def transportEquipmentSection: Section =
    userAnswers.get(TransportEquipmentListSection).mapWithIndex {
      case (_, index) =>
        val helper                = new TransportEquipmentAnswersHelper(userAnswers, index)
        val containerAndSealsRows = Seq(helper.containerIdentificationNumber, helper.transportEquipmentSeals).flatten
        val itemsRows             = Seq(helper.transportEquipmentItems).flatten
        (containerAndSealsRows, itemsRows, index)
    } match {
      case Nil =>
        StaticSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.transportEquipment.parent.heading")),
          viewLinks = Seq(transportEquipmentAddRemoveLink)
        )
      case sectionsRows =>
        val transportEquipments = sectionsRows.map {
          case (containerAndSeals, items, index) =>
            val containerAndSealsSection =
              StaticSection(
                rows = containerAndSeals,
                viewLinks = Seq(sealsAddRemoveLink(index))
              )

            val itemsSection =
              StaticSection(
                sectionTitle = None,
                rows = items,
                viewLinks = Seq(itemsAddRemoveLink(index)),
                optionalInformationHeading = if (items.isEmpty) None else Some(messages("unloadingFindings.informationHeading.consignment.item"))
              )

            AccordionSection(
              sectionTitle = Some(messages("unloadingFindings.subsections.transportEquipment", index.display)),
              viewLinks = Nil,
              children = Seq(containerAndSealsSection, itemsSection)
            )
        }

        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.transportEquipment.parent.heading")),
          viewLinks = Seq(transportEquipmentAddRemoveLink),
          children = transportEquipments,
          id = Some("transportEquipments")
        )
    }

  def additionalReferencesSection: Section =
    userAnswers.get(AdditionalReferencesSection).mapWithIndex {
      case (_, index) =>
        val helper = new AdditionalReferenceAnswersHelper(userAnswers, index)
        val rows   = Seq(helper.code, helper.referenceNumber).flatten
        (rows, index)
    } match {
      case Nil =>
        StaticSection(
          sectionTitle = Some(messages("unloadingFindings.additional.reference.heading")),
          viewLinks = Seq(additionalReferenceAddRemoveLink)
        )
      case sectionsRows =>
        val children = sectionsRows.map {
          case (rows, index) =>
            AccordionSection(
              sectionTitle = Some(messages("unloadingFindings.additional.reference", index.display)),
              rows = rows
            )
        }
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.additional.reference.heading")),
          viewLinks = Seq(additionalReferenceAddRemoveLink),
          children = children,
          id = Some("additionalReferences")
        )
    }

  def additionalInformationSection: Option[Section] =
    userAnswers.get(AdditionalInformationListSection).mapWithIndex {
      case (_, index) =>
        val helper = new AdditionalInformationAnswersHelper(userAnswers, index)
        val rows   = Seq(helper.code, helper.description).flatten
        (rows, index)
    } match {
      case Nil =>
        None
      case sectionsRows =>
        val children = sectionsRows.map {
          case (rows, index) =>
            AccordionSection(
              sectionTitle = Some(messages("unloadingFindings.additionalInformation.label", index.display)),
              rows = rows
            )
        }
        Some(
          AccordionSection(
            sectionTitle = Some(messages("unloadingFindings.additionalInformation.heading")),
            children = children,
            id = Some("additionalInformation")
          )
        )
    }

  def incidentSection: Option[Section] =
    userAnswers
      .get(IncidentsSection)
      .mapWithIndex {
        case (_, index) =>
          val helper = new IncidentAnswersHelper(userAnswers, index)

          val rows = Seq(
            helper.incidentCountryRow,
            helper.incidentCodeRow,
            helper.incidentDescriptionRow,
            helper.incidentQualifierRow,
            helper.incidentCoordinatesRow,
            helper.incidentUnLocodeRow,
            helper.incidentLocationAddressRow,
            helper.containerIndicator
          ).flatten

          val transhipment = StaticSection(
            sectionTitle = Some(messages("unloadingFindings.subsections.incidents.replacementMeansOfTransport")),
            rows = helper.incidentReplacementMeansOfTransport
          )
          val endorsementSection = StaticSection(
            sectionTitle = Some(messages("unloadingFindings.subsections.incidents.endorsements")),
            rows = Seq(
              helper.incidentEndorsementDateRow,
              helper.incidentEndorsementAuthorityRow,
              helper.incidentEndorsementCountryRow,
              helper.incidentEndorsementPlaceRow
            ).flatten
          )

          AccordionSection(
            sectionTitle = Some(messages("unloadingFindings.subsections.incidents", index.display)),
            rows = rows,
            children = Seq(endorsementSection) ++ helper.incidentTransportEquipments ++ Seq(transhipment)
          )
      }
      .toList match {
      case Nil => None
      case sections =>
        Some(
          AccordionSection(
            sectionTitle = Some(messages("unloadingFindings.subsections.incidents.parent.header")),
            children = sections,
            id = Some("incidents")
          )
        )
    }

  def documentSection: Section =
    userAnswers.get(DocumentsSection).mapWithIndex {
      case (_, index) =>
        val helper   = new DocumentAnswersHelper(userAnswers, index)
        val readOnly = userAnswers.get(TypePage(index)).map(_.`type`).contains(Previous)

        val rows = Seq(
          helper.documentType(readOnly),
          helper.referenceNumber(readOnly),
          helper.additionalInformation(readOnly)
        ).flatten
        (rows, index)
    } match {
      case Nil =>
        StaticSection(
          sectionTitle = Some(messages("unloadingFindings.document.heading.parent.heading")),
          viewLinks = Seq(documentAddRemoveLink)
        )
      case documentSectionRows =>
        val documents = documentSectionRows.map {
          case (rows, index) =>
            AccordionSection(
              sectionTitle = Some(messages("unloadingFindings.document.heading", index.display)),
              rows = rows
            )
        }
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.document.heading.parent.heading")),
          viewLinks = Seq(documentAddRemoveLink),
          children = documents,
          id = Some("documents")
        )
    }

  // Don't show children sections here. These are accessed from the 'More details' link
  def houseConsignmentSection: Option[Section] =
    userAnswers
      .get(HouseConsignmentsSection)
      .mapWithIndex {
        case (_, index) =>
          val helper = new HouseConsignmentAnswersHelper(userAnswers, index)
          val rows = Seq(
            helper.consignorName,
            helper.consignorIdentification
          ).flatten

          AccordionSection(
            sectionTitle = messages("unloadingFindings.subsections.houseConsignment", index.display),
            rows = rows,
            viewLinks = Seq(
              Link(
                id = s"view-house-consignment-${index.display}",
                href = controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, index).url,
                visuallyHidden = messages("summaryDetails.visuallyHidden", index.display)
              )
            ),
            id = s"houseConsignment${index.display}",
            children = Seq(helper.houseConsignmentConsigneeSection)
          )
      }
      .toList match {
      case Nil => None
      case sections =>
        Some(
          AccordionSection(
            sectionTitle = Some(messages("unloadingFindings.subsections.houseConsignment.parent.heading")),
            children = sections,
            id = Some("houseConsignments")
          )
        )
    }

  private val documentAddRemoveLink: Link = Link(
    id = s"add-remove-documents",
    href = "#",
    text = messages("documentsLink.addRemove"),
    visuallyHidden = messages("documentsLink.visuallyHidden")
  )

  private val additionalReferenceAddRemoveLink: Link = Link(
    id = "add-remove-additional-reference",
    href = "#",
    text = messages("additionalReferenceLink.addRemove"),
    visuallyHidden = messages("additionalReferenceLink.visuallyHidden")
  )

  private val departureTransportMeansAddRemoveLink: Link =
    Link(
      id = s"add-remove-departure-transport-means",
      href = controllers.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController.onPageLoad(arrivalId, NormalMode).url,
      text = messages("departureTransportMeans.addRemove"),
      visuallyHidden = messages("departureTransportMeans.visuallyHidden")
    )

  private def sealsAddRemoveLink(index: Index): Link =
    Link(
      id = s"add-remove-seals-${index.display}",
      href = controllers.transportEquipment.index.routes.AddAnotherSealController.onPageLoad(arrivalId, NormalMode, index).url,
      text = messages("sealsLink.addRemove"),
      visuallyHidden = messages("sealsLink.visuallyHidden")
    )

  private def itemsAddRemoveLink(index: Index): Link =
    Link(
      id = s"add-remove-consignment-items-${index.display}",
      href = controllers.transportEquipment.index.routes.ApplyAnotherItemController.onPageLoad(arrivalId, NormalMode, index).url,
      text = messages("consignmentItemLink.addRemove", index.display),
      visuallyHidden = messages("consignmentItemLink.visuallyHidden", index.display)
    )

  private val transportEquipmentAddRemoveLink: Link = Link(
    id = s"add-remove-transport-equipment",
    href = "#",
    text = messages("transportEquipmentLink.addRemove"),
    visuallyHidden = messages("transportEquipmentLink.visuallyHidden")
  )
}
