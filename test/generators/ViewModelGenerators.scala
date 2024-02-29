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

import models.{ArrivalId, Index, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.FormError
import play.api.mvc.Call
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{Content, Hint, Label, RadioItem}
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import viewModels.additionalReference.index.AdditionalReferenceTypeViewModel
import viewModels.departureTransportMeans._
import viewModels.documents.{AdditionalInformationViewModel, DocumentReferenceNumberViewModel}
import viewModels.houseConsignment.index.items._
import viewModels.houseConsignment.index.items.additionalReference.{AdditionalReferenceNumberViewModel, AdditionalReferenceViewModel}
import viewModels.houseConsignment.index.items.document.{ItemsAdditionalInformationViewModel, ItemsDocumentReferenceNumberViewModel}
import viewModels.sections.Section.{AccordionSection, StaticSection}
import viewModels.transportEquipment.index.seals.SealIdentificationNumberViewModel
import viewModels.transportEquipment.index.{AddAnotherSealViewModel, ApplyAnotherItemViewModel, ContainerIdentificationNumberViewModel}
import viewModels.{ListItem, UnloadingFindingsViewModel}
import viewModels.transportEquipment.index.{ApplyAnotherItemViewModel, ContainerIdentificationNumberViewModel}
import viewModels.{ListItem, ListItemForApply, UnloadingFindingsViewModel}

trait ViewModelGenerators {
  self: Generators =>

  private val maxSeqLength = 10

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
      attributes         <- Gen.const(Map.empty[String, String]) // TODO: Do we need to have valid attributes generated here? Use case?
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
      length       <- Gen.choose(1, maxSeqLength)
      rows         <- Gen.containerOfN[Seq, SummaryListRow](length, arbitrary[SummaryListRow])
    } yield StaticSection(sectionTitle, rows)
  }

  implicit lazy val arbitraryAccordionSection: Arbitrary[AccordionSection] = Arbitrary {
    for {
      sectionTitle <- nonEmptyString
      length       <- Gen.choose(1, maxSeqLength)
      rows         <- Gen.containerOfN[Seq, SummaryListRow](length, arbitrary[SummaryListRow])
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
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield NumberOfPackagesViewModel(heading, title, requiredError)
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

  implicit lazy val arbitraryCountryViewModel: Arbitrary[CountryViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield CountryViewModel(heading, title, requiredError)
  }

  implicit lazy val arbitraryIdentificationViewModel: Arbitrary[IdentificationViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield IdentificationViewModel(heading, title, requiredError)
  }

  implicit lazy val arbitraryIdentificationNumberViewModel: Arbitrary[IdentificationNumberViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield IdentificationNumberViewModel(heading, title, requiredError)
  }

  implicit lazy val arbitraryListItem: Arbitrary[ListItem] = Arbitrary {
    for {
      name      <- nonEmptyString
      changeUrl <- Gen.option(nonEmptyString)
      removeUrl <- Gen.option(nonEmptyString)
    } yield ListItem(name, changeUrl, removeUrl)
  }

  implicit lazy val arbitraryListItemForApply: Arbitrary[ListItemForApply] = Arbitrary {
    for {
      name      <- nonEmptyString
      changeUrl <- nonEmptyString
      prefix    <- nonEmptyString
    } yield ListItemForApply(name, changeUrl, prefix)
  }

  implicit lazy val arbitraryApplyAnotherItemViewModel: Arbitrary[ApplyAnotherItemViewModel] = Arbitrary {
    for {
      listItems    <- arbitrary[Seq[ListItemForApply]]
      onSubmitCall <- arbitrary[Call]
    } yield ApplyAnotherItemViewModel(listItems, onSubmitCall, Index(0), isNumberItemsZero = false)
  }

  implicit lazy val arbitraryAddAnotherDepartureMeansOfTransportViewModel: Arbitrary[AddAnotherDepartureMeansOfTransportViewModel] = Arbitrary {
    for {
      listItems    <- arbitrary[Seq[ListItem]]
      onSubmitCall <- arbitrary[Call]
    } yield AddAnotherDepartureMeansOfTransportViewModel(listItems, onSubmitCall)
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

  implicit lazy val arbitraryAdditionalInformationViewModel: Arbitrary[AdditionalInformationViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
    } yield AdditionalInformationViewModel(heading, title, requiredError)
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

  implicit lazy val arbitraryItemsAdditionalReferenceViewModel: Arbitrary[AdditionalReferenceViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      arrivalId     <- nonEmptyString
    } yield AdditionalReferenceViewModel(heading, title, requiredError, ArrivalId(arrivalId), NormalMode, Index(0), Index(0), Index(0))
  }

  implicit lazy val arbitraryItemsAdditionalReferenceNumberViewModel: Arbitrary[AdditionalReferenceNumberViewModel] = Arbitrary {
    for {
      heading       <- nonEmptyString
      title         <- nonEmptyString
      requiredError <- nonEmptyString
      arrivalId     <- nonEmptyString
    } yield AdditionalReferenceNumberViewModel(heading, title, requiredError, ArrivalId(arrivalId), NormalMode, Index(0), Index(0), Index(0))
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
    } yield AddAnotherSealViewModel(listItems, onSubmitCall)
  }
}
