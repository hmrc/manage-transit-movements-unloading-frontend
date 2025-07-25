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

package generators

import models.{ArrivalId, ConsignmentLevelDocuments, HouseConsignmentLevelDocuments, Index, NormalMode, Procedure}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.FormError
import play.api.mvc.Call
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{Content, Hint, Label, RadioItem}
import uk.gov.hmrc.govukfrontend.views.html.components.implicits.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewModels.additionalReference.index.{AddAnotherAdditionalReferenceViewModel, AdditionalReferenceTypeViewModel}
import viewModels.documents.{AddAnotherDocumentViewModel, AdditionalInformationViewModel, DocumentReferenceNumberViewModel, TypeViewModel}
import viewModels.houseConsignment.AddAnotherHouseConsignmentViewModel
import viewModels.houseConsignment.index.additionalReference.AdditionalReferenceTypeViewModel as HCAdditionalReferenceTypeViewModel
import viewModels.houseConsignment.index.departureTransportMeans.{
  HouseConsignmentCountryViewModel,
  IdentificationNumberViewModel as HCIdentificationNumberViewModel
}
import viewModels.houseConsignment.index.documents as hcViewModel
import viewModels.houseConsignment.index.documents.{
  AddAnotherHouseConsignmentDocumentViewModel as DocumentsAddAnotherHouseConsignmentDocumentViewModel,
  ReferenceNumberViewModel
}
import viewModels.houseConsignment.index.UniqueConsignmentReferenceViewModel as HouseConsignmentUCRViewModel
import viewModels.houseConsignment.index.items.additionalReference.{
  AdditionalReferenceNumberViewModel,
  AdditionalReferenceTypeViewModel as AdditionalReferenceTypeItemViewModel
}
import viewModels.houseConsignment.index.items.document.{
  AddAnotherHouseConsignmentDocumentViewModel,
  ItemsAdditionalInformationViewModel,
  ItemsDocumentReferenceNumberViewModel
}
import viewModels.houseConsignment.index.items.packages.{
  AddAnotherPackageViewModel,
  NumberOfPackagesViewModel,
  PackageShippingMarksViewModel,
  PackageTypeViewModel
}
import viewModels.houseConsignment.index.items.{
  document as hcItemViewModel,
  AddAnotherItemViewModel,
  CombinedNomenclatureCodeViewModel,
  CommodityCodeViewModel,
  CustomsUnionAndStatisticsCodeViewModel,
  DescriptionViewModel,
  GrossWeightViewModel,
  NetWeightViewModel,
  UniqueConsignmentReferenceViewModel as ItemUniqueConsignmentReferenceViewModel
}
import viewModels.sections.Section.{AccordionSection, StaticSection}
import viewModels.transportEquipment.AddAnotherEquipmentViewModel
import viewModels.transportEquipment.index.seals.SealIdentificationNumberViewModel
import viewModels.transportEquipment.index.{AddAnotherSealViewModel, ApplyAnotherItemViewModel, ContainerIdentificationNumberViewModel}
import viewModels.*

trait ViewModelGenerators {
  self: Generators =>

  private val maxSeqLength = 10

  private val maxTransportDoc   = 99
  private val maxSupportDoc     = 99
  private val maxTransportDocHC = 99
  private val maxSupportDocHC   = 99

  implicit lazy val arbitraryText: Arbitrary[Text] = Arbitrary {
    for {
      content <- nonEmptyString
    } yield content.toText
  }

  implicit lazy val arbitraryContent: Arbitrary[Content] = Arbitrary {
    arbitrary[Text]
  }

  implicit lazy val arbitraryKey: Arbitrary[Key] = Arbitrary {
    for {
      content <- arbitrary[Content]
      classes <- Gen.alphaNumStr
    } yield Key(content, classes)
  }

  implicit lazy val arbitraryValue: Arbitrary[Value] = Arbitrary {
    for {
      content <- arbitrary[Content]
      classes <- Gen.alphaNumStr
    } yield Value(content, classes)
  }

  implicit lazy val arbitraryActionItem: Arbitrary[ActionItem] = Arbitrary {
    for {
      content            <- arbitrary[Content]
      href               <- Gen.alphaNumStr
      visuallyHiddenText <- Gen.option(Gen.alphaNumStr)
      classes            <- Gen.alphaNumStr
      attributes         <- Gen.const(Map.empty[String, String])
    } yield ActionItem(href, content, visuallyHiddenText, classes, attributes)
  }

  implicit lazy val arbitraryActions: Arbitrary[Actions] = Arbitrary {
    for {
      length <- Gen.choose(1, maxSeqLength)
      items  <- Gen.containerOfN[Seq, ActionItem](length, arbitrary[ActionItem])
    } yield Actions(items = items)
  }

  implicit lazy val arbitrarySummaryListRow: Arbitrary[SummaryListRow] = Arbitrary {
    for {
      key     <- arbitrary[Key]
      value   <- arbitrary[Value]
      classes <- Gen.alphaNumStr
      actions <- arbitrary[Option[Actions]]
    } yield SummaryListRow(key, value, classes, actions)
  }

  implicit lazy val arbitraryStaticSection: Arbitrary[StaticSection] = Arbitrary {
    for {
      sectionTitle <- nonEmptyString
      length       <- Gen.choose(0, maxSeqLength)
      rows         <- listWithMaxLength[SummaryListRow]()
      children     <- listWithMaxLength[AccordionSection]()
    } yield StaticSection(sectionTitle, rows, children)
  }

  lazy val arbitraryStaticSectionNoChildren: Arbitrary[StaticSection] = Arbitrary {
    for {
      sectionTitle <- nonEmptyString
      length       <- Gen.choose(1, maxSeqLength)
      rows         <- listWithMaxLength[SummaryListRow]()
      rows         <- listWithMaxLength[SummaryListRow]()
    } yield StaticSection(sectionTitle, rows, Nil)
  }

  implicit lazy val arbitraryAccordionSection: Arbitrary[AccordionSection] = Arbitrary {
    for {
      sectionTitle <- nonEmptyString
      length       <- Gen.choose(1, maxSeqLength)
      rows         <- listWithMaxLength[SummaryListRow]()
    } yield AccordionSection(sectionTitle, rows)
  }

  implicit lazy val arbitraryStaticSections: Arbitrary[List[StaticSection]] = Arbitrary {
    distinctListWithMaxLength[StaticSection, Option[String]]()(_.sectionTitle)
  }

  implicit lazy val arbitraryAccordionSections: Arbitrary[List[AccordionSection]] = Arbitrary {
    distinctListWithMaxLength[AccordionSection, Option[String]]()(_.sectionTitle)
  }

  implicit lazy val arbitraryHtml: Arbitrary[Html] = Arbitrary {
    for {
      text <- nonEmptyString
    } yield Html(text)
  }

  implicit lazy val arbitraryFormError: Arbitrary[FormError] = Arbitrary {
    for {
      key     <- nonEmptyString
      message <- nonEmptyString
    } yield FormError(key, message)
  }

  implicit lazy val arbitraryRadioItem: Arbitrary[RadioItem] = Arbitrary {
    for {
      content         <- arbitrary[Content]
      id              <- Gen.option(nonEmptyString)
      value           <- Gen.option(nonEmptyString)
      label           <- Gen.option(arbitrary[Label])
      hint            <- Gen.option(arbitrary[Hint])
      divider         <- Gen.option(nonEmptyString)
      checked         <- arbitrary[Boolean]
      conditionalHtml <- Gen.option(arbitrary[Html])
      disabled        <- arbitrary[Boolean]
      attributes      <- Gen.const(Map.empty[String, String])
    } yield RadioItem(content, id, value, label, hint, divider, checked, conditionalHtml, disabled, attributes)
  }

  implicit lazy val arbitraryRadioItems: Arbitrary[List[RadioItem]] = Arbitrary {
    for {
      radioItems   <- listWithMaxLength[RadioItem]()
      checkedIndex <- Gen.choose(0, radioItems.length - 1)
    } yield radioItems.zipWithIndex.map {
      case (radioItem, index) => radioItem.copy(checked = index == checkedIndex)
    }
  }

  implicit lazy val arbitraryLabel: Arbitrary[Label] = Arbitrary {
    for {
      forAttr       <- Gen.option(nonEmptyString)
      isPageHeading <- arbitrary[Boolean]
      classes       <- Gen.alphaNumStr
      attributes    <- Gen.const(Map.empty[String, String])
      content       <- arbitrary[Content]
    } yield Label(forAttr, isPageHeading, classes, attributes, content)
  }

  implicit lazy val arbitraryHint: Arbitrary[Hint] = Arbitrary {
    for {
      id         <- Gen.option(nonEmptyString)
      classes    <- Gen.alphaNumStr
      attributes <- Gen.const(Map.empty[String, String])
      content    <- arbitrary[Content]
    } yield Hint(id, classes, attributes, content)
  }

  implicit lazy val arbitraryUnloadingFindingsViewModel: Arbitrary[UnloadingFindingsViewModel] = Arbitrary {
    for {
      sections <- arbitraryStaticSections.arbitrary
    } yield UnloadingFindingsViewModel(sections)
  }

  implicit lazy val arbitraryNumberOfPackagesViewModel: Arbitrary[NumberOfPackagesViewModel] = Arbitrary {
    for {
      heading <- nonEmptyString
      title   <- nonEmptyString
      args    <- nonEmptyString
    } yield NumberOfPackagesViewModel(heading, title, Seq(args))
  }

  implicit lazy val arbitraryPackageTypeViewModel: Arbitrary[PackageTypeViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield PackageTypeViewModel(heading, title, requiredError)
  }

  implicit lazy val arbitraryPackageShippingMarksViewModel: Arbitrary[PackageShippingMarksViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield PackageShippingMarksViewModel(heading, title, requiredError)
  }

  implicit lazy val arbitrarySealIdentificationNumberViewModel: Arbitrary[SealIdentificationNumberViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield SealIdentificationNumberViewModel(heading, title, requiredError)
  }

  implicit lazy val addAnotherHouseConsignmentItemViewModelViewModel: Arbitrary[AddAnotherItemViewModel] = Arbitrary {
    for {
      listItems    <- arbitrary[Seq[ListItem]]
      onSubmitCall <- arbitrary[Call]
      nextIndex    <- arbitrary[Index]
    } yield AddAnotherItemViewModel(listItems, onSubmitCall, Index(0), nextIndex)
  }

  implicit lazy val arbitraryDepartureTransportMeansCountryViewModel: Arbitrary[departureTransportMeans.CountryViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield departureTransportMeans.CountryViewModel(heading, title, requiredError)
  }

  implicit lazy val arbitraryCountriesOfRoutingCountryViewModel: Arbitrary[countriesOfRouting.CountryViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield countriesOfRouting.CountryViewModel(heading, title, requiredError)
  }

  implicit lazy val arbitraryHouseConsignmentCountryViewModel: Arbitrary[HouseConsignmentCountryViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield HouseConsignmentCountryViewModel(heading, title, requiredError)
  }

  implicit lazy val arbitraryIdentificationViewModel: Arbitrary[departureTransportMeans.IdentificationViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield departureTransportMeans.IdentificationViewModel(heading, title, requiredError)
  }

  implicit lazy val arbitraryIdentificationNumberViewModel: Arbitrary[departureTransportMeans.IdentificationNumberViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield departureTransportMeans.IdentificationNumberViewModel(heading, title, requiredError)
  }

  implicit lazy val arbitraryHCIdentificationNumberViewModel: Arbitrary[HCIdentificationNumberViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield HCIdentificationNumberViewModel(heading, title, requiredError)
  }

  implicit lazy val arbitraryListItem: Arbitrary[ListItem] = Arbitrary {
    for {
      name      <- nonEmptyString
      changeUrl <- Gen.option(nonEmptyString)
      removeUrl <- Gen.option(nonEmptyString)
    } yield ListItem(name, changeUrl, removeUrl)
  }

  implicit lazy val arbitraryApplyAnotherItemViewModel: Arbitrary[ApplyAnotherItemViewModel] = Arbitrary {
    for {
      listItems    <- arbitrary[Seq[ListItem]]
      onSubmitCall <- arbitrary[Call]
      nextIndex    <- arbitrary[Index]
    } yield ApplyAnotherItemViewModel(listItems, onSubmitCall, Index(0), isNumberItemsZero = false, nextIndex)
  }

  implicit lazy val arbitraryAddAnotherDepartureMeansOfTransportViewModel: Arbitrary[departureTransportMeans.AddAnotherDepartureMeansOfTransportViewModel] =
    Arbitrary {
      for {
        listItems    <- arbitrary[Seq[ListItem]]
        onSubmitCall <- arbitrary[Call]
        nextIndex    <- arbitrary[Index]
      } yield departureTransportMeans.AddAnotherDepartureMeansOfTransportViewModel(listItems, onSubmitCall, nextIndex)
    }

  implicit lazy val arbitraryAddAnotherCountryOfRoutingViewModel: Arbitrary[countriesOfRouting.AddAnotherCountryViewModel] =
    Arbitrary {
      for {
        listItems    <- arbitrary[Seq[ListItem]]
        onSubmitCall <- arbitrary[Call]
        nextIndex    <- arbitrary[Index]
      } yield countriesOfRouting.AddAnotherCountryViewModel(listItems, onSubmitCall, nextIndex)
    }

  implicit lazy val arbitraryAddAnotherDepartureMeansOfTransportHCViewModel
    : Arbitrary[viewModels.houseConsignment.index.departureTransportMeans.AddAnotherDepartureMeansOfTransportViewModel] = Arbitrary {
    for {
      listItems    <- arbitrary[Seq[ListItem]]
      onSubmitCall <- arbitrary[Call]
      nextIndex    <- arbitrary[Index]
    } yield viewModels.houseConsignment.index.departureTransportMeans.AddAnotherDepartureMeansOfTransportViewModel(listItems, onSubmitCall, nextIndex, Index(0))
  }

  implicit lazy val addAnotherEquipmentViewModel: Arbitrary[AddAnotherEquipmentViewModel] = Arbitrary {
    for {
      listItems    <- arbitrary[Seq[ListItem]]
      onSubmitCall <- arbitrary[Call]
      nextIndex    <- arbitrary[Index]
    } yield AddAnotherEquipmentViewModel(listItems, onSubmitCall, nextIndex)
  }

  implicit lazy val addAnotherDocumentViewModel: Arbitrary[AddAnotherDocumentViewModel] = Arbitrary {
    for {
      listItems    <- arbitrary[Seq[ListItem]]
      onSubmitCall <- arbitrary[Call]
      nextIndex    <- arbitrary[Index]
      documents    <- arbitrary[ConsignmentLevelDocuments](arbitraryConsignmentLevelDocuments)
      allowMore    <- arbitrary[Boolean]
    } yield AddAnotherDocumentViewModel(listItems, onSubmitCall, nextIndex, documents, allowMore)
  }

  implicit lazy val houseConsignmentAddAnotherDocumentViewModel: Arbitrary[AddAnotherHouseConsignmentDocumentViewModel] = Arbitrary {
    for {
      listItems    <- arbitrary[Seq[ListItem]]
      onSubmitCall <- arbitrary[Call]
      nextIndex    <- arbitrary[Index]
      documents    <- arbitrary[HouseConsignmentLevelDocuments](arbitraryHouseConsignmentLevelDocuments)
      allowMore    <- arbitrary[Boolean]
    } yield AddAnotherHouseConsignmentDocumentViewModel(listItems, onSubmitCall, nextIndex, documents, allowMore)
  }

  implicit lazy val houseConsignmentDocumentAddAnotherDocumentViewModel: Arbitrary[DocumentsAddAnotherHouseConsignmentDocumentViewModel] = Arbitrary {
    for {
      listItems    <- arbitrary[Seq[ListItem]]
      onSubmitCall <- arbitrary[Call]
      nextIndex    <- arbitrary[Index]
      documents    <- arbitrary[HouseConsignmentLevelDocuments](arbitraryHouseConsignmentLevelDocuments)
      allowMore    <- arbitrary[Boolean]
    } yield DocumentsAddAnotherHouseConsignmentDocumentViewModel(listItems, onSubmitCall, nextIndex, documents, allowMore)
  }

  implicit lazy val arbitraryContainerIdentificationNumberViewModel: Arbitrary[ContainerIdentificationNumberViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      paragraph     <- nonEmptyString
    } yield ContainerIdentificationNumberViewModel(heading, title, requiredError, Some(paragraph))
  }

  implicit lazy val arbitraryDocumentReferenceNumberViewModel: Arbitrary[DocumentReferenceNumberViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield DocumentReferenceNumberViewModel(heading, title, requiredError)
  }

  implicit lazy val arbitraryReferenceNumberViewModel: Arbitrary[ReferenceNumberViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield ReferenceNumberViewModel(heading, title, requiredError)
  }

  implicit lazy val arbitraryAdditionalInformationViewModel: Arbitrary[AdditionalInformationViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield AdditionalInformationViewModel(heading, title, requiredError)
  }

  implicit lazy val arbitraryConsignmentLevelDocuments: Arbitrary[ConsignmentLevelDocuments] = Arbitrary {
    for {
      support   <- positiveInts
      transport <- positiveInts
    } yield ConsignmentLevelDocuments(support, transport)
  }

  implicit lazy val arbitraryConsignmentLevelDocumentsMaxedOutTransport: Arbitrary[ConsignmentLevelDocuments] = Arbitrary {
    ConsignmentLevelDocuments(0, maxTransportDoc)
  }

  implicit lazy val arbitraryConsignmentLevelDocumentsMaxedOutSupport: Arbitrary[ConsignmentLevelDocuments] = Arbitrary {
    ConsignmentLevelDocuments(maxSupportDoc, 0)
  }

  implicit lazy val arbitraryTypeViewModel: Arbitrary[TypeViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      documents     <- arbitrary[ConsignmentLevelDocuments](arbitraryConsignmentLevelDocuments)
    } yield TypeViewModel(heading, title, requiredError, documents)
  }

  implicit lazy val arbitraryHouseConsignmentLevelDocuments: Arbitrary[HouseConsignmentLevelDocuments] = Arbitrary {
    for {
      support   <- positiveInts
      transport <- positiveInts
    } yield HouseConsignmentLevelDocuments(support, transport)
  }

  implicit lazy val arbitraryHouseConsignmentLevelDocumentsMaxedOutTransport: Arbitrary[HouseConsignmentLevelDocuments] = Arbitrary {
    HouseConsignmentLevelDocuments(0, maxTransportDocHC)
  }

  implicit lazy val arbitraryHouseConsignmentLevelDocumentsMaxedOutSupport: Arbitrary[HouseConsignmentLevelDocuments] = Arbitrary {
    HouseConsignmentLevelDocuments(maxSupportDocHC, 0)
  }

  implicit lazy val arbitraryHouseConsignmentItemTypeViewModel: Arbitrary[hcItemViewModel.TypeViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      documents     <- arbitrary[HouseConsignmentLevelDocuments](arbitraryHouseConsignmentLevelDocuments)
    } yield hcItemViewModel.TypeViewModel(heading, title, requiredError, documents)
  }

  implicit lazy val arbitraryHouseConsignmentTypeViewModel: Arbitrary[hcViewModel.TypeViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      documents     <- arbitrary[HouseConsignmentLevelDocuments](arbitraryHouseConsignmentLevelDocuments)
    } yield hcViewModel.TypeViewModel(heading, title, requiredError, documents)
  }

  implicit lazy val arbitraryItemsDocumentReferenceNumberViewModel: Arbitrary[ItemsDocumentReferenceNumberViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield ItemsDocumentReferenceNumberViewModel(heading, title, requiredError)
  }

  implicit lazy val arbitraryItemsAdditionalInformationViewModel: Arbitrary[ItemsAdditionalInformationViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield ItemsAdditionalInformationViewModel(heading, title, requiredError)
  }

  implicit lazy val arbitraryItemsAdditionalReferenceViewModel: Arbitrary[AdditionalReferenceTypeItemViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      arrivalId     <- nonEmptyString
    } yield AdditionalReferenceTypeItemViewModel(
      heading,
      title,
      requiredError,
      ArrivalId(arrivalId),
      NormalMode,
      NormalMode,
      NormalMode,
      Index(0),
      Index(0),
      Index(0)
    )
  }

  implicit lazy val arbitraryItemsAdditionalReferenceNumberViewModel: Arbitrary[AdditionalReferenceNumberViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      arrivalId     <- nonEmptyString
    } yield AdditionalReferenceNumberViewModel(
      heading,
      title,
      requiredError,
      ArrivalId(arrivalId),
      NormalMode,
      NormalMode,
      NormalMode,
      Index(0),
      Index(0),
      Index(0)
    )
  }

  implicit lazy val arbitraryAdditionalReferenceViewModel: Arbitrary[AdditionalReferenceTypeViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      arrivalId     <- nonEmptyString
    } yield AdditionalReferenceTypeViewModel(heading, title, requiredError, ArrivalId(arrivalId), NormalMode, Index(0))
  }

  implicit lazy val arbitraryAdditionalReferenceNumberViewModel: Arbitrary[viewModels.additionalReference.index.AdditionalReferenceNumberViewModel] =
    Arbitrary {
      for {
        heading       <- nonEmptyString
        title         <- nonEmptyString
        requiredError <- nonEmptyString
      } yield viewModels.additionalReference.index.AdditionalReferenceNumberViewModel(heading, title, requiredError)
    }

  implicit lazy val arbitraryAddAnotherSealViewModel: Arbitrary[AddAnotherSealViewModel] = Arbitrary {
    for {
      listItems    <- arbitrary[Seq[ListItem]]
      onSubmitCall <- arbitrary[Call]
    } yield AddAnotherSealViewModel(listItems, onSubmitCall, Index(0), Index(0))
  }

  implicit lazy val addAnotherAdditionalReferenceViewModelViewModel: Arbitrary[AddAnotherAdditionalReferenceViewModel] = Arbitrary {
    for {
      listItems    <- arbitrary[Seq[ListItem]]
      onSubmitCall <- arbitrary[Call]
      nextIndex    <- arbitrary[Index]
    } yield AddAnotherAdditionalReferenceViewModel(listItems, onSubmitCall, nextIndex)
  }

  implicit lazy val addAnotherAdditionalReferenceViewModelViewModelHouseConsignmentItemLevel
    : Arbitrary[viewModels.houseConsignment.index.items.additionalReference.AddAnotherAdditionalReferenceViewModel] = Arbitrary {
    for {
      listItems    <- arbitrary[Seq[ListItem]]
      onSubmitCall <- arbitrary[Call]
      nextIndex    <- arbitrary[Index]
    } yield viewModels.houseConsignment.index.items.additionalReference.AddAnotherAdditionalReferenceViewModel(listItems,
                                                                                                               onSubmitCall,
                                                                                                               nextIndex,
                                                                                                               Index(0),
                                                                                                               Index(0)
    )
  }

  implicit lazy val addAnotherAdditionalReferenceViewModelViewModelHouseConsignmentLevel
    : Arbitrary[viewModels.houseConsignment.index.additionalReference.AddAnotherAdditionalReferenceViewModel] = Arbitrary {
    for {
      listItems    <- arbitrary[Seq[ListItem]]
      onSubmitCall <- arbitrary[Call]
      nextIndex    <- arbitrary[Index]
    } yield viewModels.houseConsignment.index.additionalReference.AddAnotherAdditionalReferenceViewModel(listItems, onSubmitCall, nextIndex, Index(0))
  }

  implicit lazy val arbitraryAddAnotherPackageViewModel: Arbitrary[AddAnotherPackageViewModel] = Arbitrary {
    for {
      listItems    <- arbitrary[Seq[ListItem]]
      onSubmitCall <- arbitrary[Call]
      nextIndex    <- arbitrary[Index]
    } yield AddAnotherPackageViewModel(listItems, onSubmitCall, nextIndex)
  }

  implicit lazy val arbitraryHCAdditionalReferenceViewModel: Arbitrary[HCAdditionalReferenceTypeViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      arrivalId     <- nonEmptyString
    } yield HCAdditionalReferenceTypeViewModel(heading, title, requiredError, ArrivalId(arrivalId), NormalMode, NormalMode, Index(0), Index(0))
  }

  implicit lazy val arbitraryHouseConsignmentIdentificationViewModel
    : Arbitrary[viewModels.houseConsignment.index.departureTransportMeans.IdentificationViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      paragraph     <- nonEmptyString
    } yield viewModels.houseConsignment.index.departureTransportMeans.IdentificationViewModel(heading, title, requiredError, Some(paragraph))
  }

  implicit lazy val arbitraryHCDocumentsAdditionalInformationViewModel: Arbitrary[viewModels.houseConsignment.index.documents.AdditionalInformationViewModel] =
    Arbitrary {
      for {
        heading       <- nonEmptyString
        title         <- nonEmptyString
        requiredError <- nonEmptyString
      } yield viewModels.houseConsignment.index.documents.AdditionalInformationViewModel(heading, title, requiredError)
    }

  implicit lazy val arbitraryAddAnotherHouseConsignmentViewModel: Arbitrary[AddAnotherHouseConsignmentViewModel] = Arbitrary {
    for {
      listItems    <- arbitrary[Seq[ListItem]]
      onSubmitCall <- arbitrary[Call]
      nextIndex    <- arbitrary[Index]
    } yield AddAnotherHouseConsignmentViewModel(listItems, onSubmitCall, nextIndex)
  }

  implicit lazy val arbitraryItemDescriptionViewModel: Arbitrary[DescriptionViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      arrivalId     <- arbitrary[ArrivalId]
    } yield DescriptionViewModel(heading, title, requiredError, arrivalId, NormalMode, NormalMode, Index(0), Index(0))
  }

  implicit lazy val arbitraryItemGrossWeightViewModel: Arbitrary[GrossWeightViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      arrivalId     <- arbitrary[ArrivalId]
    } yield GrossWeightViewModel(heading, title, requiredError, arrivalId, NormalMode, NormalMode, Index(0), Index(0))
  }

  implicit lazy val arbitraryItemNetWeightViewModel: Arbitrary[NetWeightViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      arrivalId     <- arbitrary[ArrivalId]
    } yield NetWeightViewModel(heading, title, requiredError, arrivalId, NormalMode, NormalMode, Index(0), Index(0))
  }

  implicit lazy val arbitraryItemCustomsUnionAndStatisticsCodeViewModel: Arbitrary[CustomsUnionAndStatisticsCodeViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      arrivalId     <- arbitrary[ArrivalId]
    } yield CustomsUnionAndStatisticsCodeViewModel(heading, title, requiredError, arrivalId, NormalMode, NormalMode, Index(0), Index(0))
  }

  implicit lazy val arbitraryItemUniqueConsignmentReferenceViewModel: Arbitrary[ItemUniqueConsignmentReferenceViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      arrivalId     <- arbitrary[ArrivalId]
    } yield ItemUniqueConsignmentReferenceViewModel(heading, title, requiredError, arrivalId, NormalMode, NormalMode, Index(0), Index(0))
  }

  implicit lazy val arbitraryItemCommodityCodeViewModel: Arbitrary[CommodityCodeViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      arrivalId     <- arbitrary[ArrivalId]
      invalidError  <- nonEmptyString
    } yield CommodityCodeViewModel(heading, title, requiredError, arrivalId, NormalMode, NormalMode, Index(0), Index(0), invalidError)
  }

  implicit lazy val arbitraryConsignmentUniqueReferenceNumberViewModel: Arbitrary[UniqueConsignmentReferenceViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield UniqueConsignmentReferenceViewModel(heading, title, requiredError)
  }

  implicit lazy val arbitraryHouseConsignmentUniqueReferenceNumberViewModel: Arbitrary[HouseConsignmentUCRViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield HouseConsignmentUCRViewModel(heading, title, requiredError, Index(0))
  }

  implicit lazy val arbitraryItemCombinedNomenclatureCodeViewModel: Arbitrary[CombinedNomenclatureCodeViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      arrivalId     <- arbitrary[ArrivalId]
    } yield CombinedNomenclatureCodeViewModel(heading, title, requiredError, arrivalId, NormalMode, NormalMode, Index(0), Index(0))
  }

  implicit lazy val arbitraryOtherThingsToReportViewModel: Arbitrary[OtherThingsToReportViewModel] = Arbitrary {
    for {
      title          <- nonEmptyString
      heading        <- nonEmptyString
      additionalHtml <- Gen.option(arbitrary[OtherThingsToReportViewModel.AdditionalHtml])
      requiredError  <- nonEmptyString
      maxLengthError <- nonEmptyString
      invalidError   <- nonEmptyString
      onSubmitCall   <- arbitrary[Call]
    } yield OtherThingsToReportViewModel(title, heading, additionalHtml, requiredError, maxLengthError, invalidError, onSubmitCall)
  }

  implicit lazy val arbitraryOtherThingsToReportAdditionalHtml: Arbitrary[OtherThingsToReportViewModel.AdditionalHtml] = Arbitrary {
    for {
      paragraph1 <- nonEmptyString
      paragraph2 <- nonEmptyString
      linkText   <- nonEmptyString
      linkHref   <- arbitrary[Call]
      paragraph3 <- nonEmptyString
    } yield OtherThingsToReportViewModel.AdditionalHtml(paragraph1, paragraph2, linkText, linkHref, paragraph3)
  }

  implicit lazy val arbitraryCheckYourAnswersViewModel: Arbitrary[CheckYourAnswersViewModel] = Arbitrary {
    for {
      procedureSection      <- arbitrary[StaticSection]
      sections              <- listWithMaxLength[StaticSection]()
      showDiscrepanciesLink <- arbitrary[Boolean]
      procedure             <- arbitrary[Procedure]
    } yield CheckYourAnswersViewModel(procedureSection, sections, showDiscrepanciesLink, procedure)
  }
}
