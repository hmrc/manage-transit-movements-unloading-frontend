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
import models.reference.Country
import models.{Index, Link, UserAnswers}
import pages._
import pages.departureMeansOfTransport.{CountryPage, TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
import pages.sections._
import pages.sections.additionalReference.AdditionalReferenceSection._
import pages.sections.additionalReference.{AdditionalReferenceSection, AdditionalReferencesSection}
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import viewModels.sections.Section

import scala.concurrent.{ExecutionContext, Future}

class UnloadingFindingsAnswersHelper(userAnswers: UserAnswers)(implicit
  messages: Messages,
  ec: ExecutionContext
) extends UnloadingAnswersHelper(userAnswers) {

  def buildVehicleNationalityRow(index: Index): Future[Option[SummaryListRow]] =
    (for {
      x <- OptionT.fromOption[Future](userAnswers.get(CountryPage(index)))
    } yield transportRegisteredCountry(x)).value

  def buildMeansOfTransportRows(idRow: Option[SummaryListRow], nationalityRow: Option[SummaryListRow], numberRow: Option[SummaryListRow]): Seq[SummaryListRow] =
    idRow.map(Seq(_)).getOrElse(Seq.empty) ++
      numberRow.map(Seq(_)).getOrElse(Seq.empty) ++
      nationalityRow.map(Seq(_)).getOrElse(Seq.empty)

  def buildTransportSections: Future[Seq[Section]] =
    userAnswers
      .get(TransportMeansListSection)
      .traverse {
        _.zipWithIndex.traverse {
          y =>
            for {
              nationalityRow <- buildVehicleNationalityRow(y._2)
            } yield Section(
              messages("unloadingFindings.subsections.transportMeans", y._2.display),
              buildMeansOfTransportRows(transportMeansID(y._2), nationalityRow, transportMeansNumber(y._2))
            )
        }
      }
      .map(_.getOrElse(Seq.empty))

  def transportMeansID(transportMeansIndex: Index): Option[SummaryListRow] =
    userAnswers
      .get(TransportMeansIdentificationPage(transportMeansIndex))
      .flatMap(
        identificationAnswer =>
          buildRowWithAnswer[TransportMeansIdentification](
            optionalAnswer = Some(identificationAnswer),
            formatAnswer = formatAsText,
            prefix = "checkYourAnswers.departureMeansOfTransport.identification",
            id = Some(s"change-transport-means-identification-${transportMeansIndex.display}"),
            call = Some(Call(GET, "#"))
          )
      )

  private def transportMeansNumber(transportMeansIndex: Index): Option[SummaryListRow] =
    userAnswers
      .get(VehicleIdentificationNumberPage(transportMeansIndex))
      .flatMap(
        identificationNumber =>
          buildRowWithAnswer[String](
            optionalAnswer = Some(identificationNumber),
            formatAnswer = formatAsText,
            prefix = "checkYourAnswers.departureMeansOfTransport.identificationNumber",
            id = Some(s"change-transport-means-identification-number-${transportMeansIndex.display}"),
            call = Some(Call(GET, "#"))
          )
      )

  def transportRegisteredCountry(answer: Country): SummaryListRow = buildSimpleRow(
    answer = Text(answer.description),
    label = messages("unloadingFindings.rowHeadings.vehicleNationality"),
    prefix = "checkYourAnswers.departureMeansOfTransport.country",
    id = Some("change-registered-country"),
    call = Some(Call(GET, "#")),
    args = Seq.empty
  )

  def transportEquipmentSections: Seq[Section] =
    userAnswers
      .get(TransportEquipmentListSection)
      .mapWithIndex {
        (_, equipmentIndex) =>
          val containerRow: Seq[Option[SummaryListRow]] = Seq(containerIdentificationNumber(equipmentIndex))
          val sealRows: Seq[SummaryListRow]             = transportEquipmentSeals(equipmentIndex)

          val rows = containerRow.head match {
            case Some(containerRow) => Seq(containerRow) ++ sealRows
            case None               => sealRows
          }

          Some(
            Section(
              messages("unloadingFindings.subsections.transportEquipment", equipmentIndex.display),
              rows
            )
          )
      }

  private def transportEquipmentSeals(equipmentIndex: Index): Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(SealsSection(equipmentIndex))(
      sealIndex => transportEquipmentSeal(equipmentIndex, sealIndex)
    )

  def containerIdentificationNumber(index: Index): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ContainerIdentificationNumberPage(index),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.containerIdentificationNumber",
    id = Some(s"change-container-identification-number-${index.display}"),
    args = None,
    call = Some(Call(GET, "#"))
  )

  def transportEquipmentSeal(equipmentIndex: Index, sealIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = SealIdentificationNumberPage(equipmentIndex, sealIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.sealIdentifier",
    args = sealIndex.display,
    id = Some(s"change-seal-details-${sealIndex.display}"),
    call = Some(Call(GET, "#"))
  )

  def additionalReference(index: Index): Option[SummaryListRow] = getAnswerAndBuildRow[AdditionalReference](
    page = AdditionalReferenceSection(index),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.additional.reference",
    args = index.display,
    id = Some(s"change-additional-reference-${index.display}"),
    call = Some(Call(GET, "#")) //TODO change me please
  )(AdditionalReference.reads(index))

  def additionalReferences: Seq[SummaryListRow] = getAnswersAndBuildSectionRows(AdditionalReferencesSection)(additionalReference)

  def houseConsignmentSections: Seq[Section] =
    userAnswers.get(HouseConsignmentsSection).mapWithIndex {
      (_, houseConsignmentIndex) =>
        val rows = buildHouseConsignmentRows(
          houseConsignmentTotalWeightRows(houseConsignmentIndex),
          consignorName(houseConsignmentIndex),
          consignorIdentification(houseConsignmentIndex),
          consigneeName(houseConsignmentIndex),
          consigneeIdentification(houseConsignmentIndex)
        )

        Some(
          Section(
            sectionTitle = messages("unloadingFindings.subsections.houseConsignment", houseConsignmentIndex.display),
            rows,
            viewLink = Some(
              Link(
                id = s"view-house-consignment-${houseConsignmentIndex.display}",
                href = controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex).url,
                visuallyHidden = messages("summaryDetails.visuallyHidden", houseConsignmentIndex.display)
              )
            ),
            id = Some(s"houseConsignment${houseConsignmentIndex.display}")
          )
        )
    }
}
