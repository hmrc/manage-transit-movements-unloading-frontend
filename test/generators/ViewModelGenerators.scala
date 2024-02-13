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

import models.Index
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.FormError
import play.api.mvc.Call
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{Content, Hint, Label, RadioItem}
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import viewModels.{ListItem, UnloadingFindingsViewModel}
import viewModels.departureTransportMeans._
import viewModels.documents.{AdditionalInformationViewModel, DocumentReferenceNumberViewModel}
import viewModels.houseConsignment.index.items._
import viewModels.sections.Section
import viewModels.transportEquipment.index.{ApplyAnotherItemViewModel, ContainerIdentificationNumberViewModel}
import viewModels.transportEquipment.index.seals.SealIdentificationNumberViewModel

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

  implicit lazy val arbitrarySection: Arbitrary[Section] = Arbitrary {
    for {
      sectionTitle <- nonEmptyString
      length       <- Gen.choose(1, maxSeqLength)
      rows         <- Gen.containerOfN[Seq, SummaryListRow](length, arbitrary[SummaryListRow])
    } yield Section(sectionTitle, rows)
  }

  implicit lazy val arbitrarySections: Arbitrary[List[Section]] = Arbitrary {
    listWithMaxLength[Section]().retryUntil {
      sections =>
        val sectionTitles = sections.map(_.sectionTitle)
        sectionTitles.distinct.size == sectionTitles.size
    }
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
      sections <- arbitrarySections.arbitrary
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
      changeUrl <- nonEmptyString
      prefix    <- nonEmptyString
    } yield ListItem(name, changeUrl, prefix)
  }

  implicit lazy val arbitraryApplyAnotherItemViewModel: Arbitrary[ApplyAnotherItemViewModel] = Arbitrary {
    for {
      listItems    <- arbitrary[Seq[ListItem]]
      onSubmitCall <- arbitrary[Call]
    } yield ApplyAnotherItemViewModel(listItems, onSubmitCall, Index(0), isNumberItemsZero = false)
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
}
