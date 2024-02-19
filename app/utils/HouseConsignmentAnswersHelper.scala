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

package utils

import cats.data.OptionT
import cats.implicits._
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.{AdditionalReferenceType, Country}
import models.{Index, UserAnswers}
import pages._
import pages.houseConsignment.index.items.ItemDescriptionPage
import pages.houseConsignment.index.items.additionalReference.AdditionalReferencePage
import pages.sections._
import pages.sections.departureTransportMeans.DepartureTransportMeansListSection
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import viewModels.sections.Section

import scala.concurrent.{ExecutionContext, Future}

class HouseConsignmentAnswersHelper(userAnswers: UserAnswers, houseConsignmentIndex: Index)(implicit
  messages: Messages,
  ec: ExecutionContext
) extends UnloadingAnswersHelper(userAnswers) {

  def buildVehicleNationalityRow(transportMeansIndex: Index): Future[Option[SummaryListRow]] =
    (for {
      x <- OptionT.fromOption[Future](userAnswers.get(DepartureTransportMeansCountryPage(houseConsignmentIndex, transportMeansIndex)))
    } yield transportRegisteredCountry(x)).value

  def buildMeansOfTransportRows(idRow: Option[SummaryListRow], nationalityRow: Option[SummaryListRow]): Seq[SummaryListRow] =
    idRow.map(Seq(_)).getOrElse(Seq.empty) ++
      nationalityRow.map(Seq(_)).getOrElse(Seq.empty)

  def buildTransportSections: Future[Seq[Section]] =
    userAnswers
      .get(DepartureTransportMeansListSection(houseConsignmentIndex))
      .traverse {
        _.zipWithIndex.traverse {
          case (_, index) =>
            val nationalityRow: Future[Option[SummaryListRow]] = buildVehicleNationalityRow(index)
            val meansIdRow: Option[SummaryListRow]             = transportMeansID(index)

            nationalityRow.map {
              nationalityRow =>
                Section(
                  messages("unloadingFindings.subsections.transportMeans", index.display),
                  buildMeansOfTransportRows(meansIdRow, nationalityRow)
                )
            }
        }
      }
      .map(_.getOrElse(Seq.empty))

  def transportMeansID(transportMeansIndex: Index): Option[SummaryListRow] =
    userAnswers
      .get(DepartureTransportMeansIdentificationTypePage(houseConsignmentIndex, transportMeansIndex))
      .flatMap(
        identificationAnswer =>
          buildRowWithAnswer[TransportMeansIdentification](
            optionalAnswer = Some(identificationAnswer),
            formatAnswer = formatAsText,
            prefix = "checkYourAnswers.departureMeansOfTransport.identification",
            id = None,
            call = None
          )
      )

  def transportRegisteredCountry(answer: Country): SummaryListRow = buildSimpleRow(
    answer = Text(answer.description),
    label = messages("unloadingFindings.rowHeadings.vehicleNationality"),
    prefix = "unloadingFindings.rowHeadings.vehicleNationality",
    id = None,
    call = None,
    args = Seq.empty
  )

  def houseConsignmentSection: Seq[Section] = {

    val rows = buildHouseConsignmentRows(
      houseConsignmentTotalWeightRows(houseConsignmentIndex),
      consignorName(houseConsignmentIndex),
      consignorIdentification(houseConsignmentIndex),
      consigneeName(houseConsignmentIndex),
      consigneeIdentification(houseConsignmentIndex)
    )

    Seq(Section(rows))
  }

  def itemSections: Seq[Section] =
    userAnswers
      .get(ItemsSection(houseConsignmentIndex))
      .mapWithIndexSeq {
        (_, itemIndex) =>
          val itemDescription: Option[SummaryListRow] = itemDescriptionRow(houseConsignmentIndex, itemIndex)
          val grossWeight: Option[SummaryListRow]     = grossWeightRow(houseConsignmentIndex, itemIndex)
          val netWeight: Option[SummaryListRow]       = netWeightRow(houseConsignmentIndex, itemIndex)
          val additionalReferencesSection: Section    = additionalReferenceSection(houseConsignmentIndex, itemIndex)

          val itemSection = Section(
            messages("unloadingFindings.subsections.item", itemIndex.display),
            Seq(itemDescription, grossWeight, netWeight).flatten
          )
          Seq(itemSection, additionalReferencesSection)
      }

  def additionalReferenceSection(houseConsignmentIndex: Index, itemIndex: Index): Section =
    Section(
      messages("unloadingFindings.additional.reference.heading"),
      additionalReferences(houseConsignmentIndex, itemIndex)
    )

  def itemDescriptionRow(houseConsignmentIndex: Index, itemIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ItemDescriptionPage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.description",
    id = None,
    call = None
  )

  def additionalReferences(hcIndex: Index, itemIndex: Index): Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(HouseConsignmentAdditionalReferencesSection(hcIndex, itemIndex)) {
      additionalReferenceIndex =>
        additionalReference(hcIndex, itemIndex, additionalReferenceIndex)
    }

  def additionalReference(hcIndex: Index, itemIndex: Index, additionalReferenceIndex: Index): Option[SummaryListRow] =
    getAnswerAndBuildRow[AdditionalReferenceType](
      page = AdditionalReferencePage(hcIndex, itemIndex, additionalReferenceIndex),
      formatAnswer = formatAsText,
      prefix = "unloadingFindings.additional.reference",
      args = additionalReferenceIndex.display,
      id = Some(s"change-additional-reference-${additionalReferenceIndex.display}"),
      call = Some(Call(GET, "#")) //TODO change me please
    )

}
