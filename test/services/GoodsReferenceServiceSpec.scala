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

package services

import base.{AppWithDefaultMockFixtures, SpecBase}
import models.Index
import models.reference.GoodsReference
import pages.houseConsignment.index.items.{DeclarationGoodsItemNumberPage, ItemDescriptionPage}
import pages.transportEquipment.index.ItemPage
import play.api.libs.json.Json

class GoodsReferenceServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val service = app.injector.instanceOf[GoodsReferenceService]

  "getGoodsReference" - {
    "must get goods reference" in {
      val userAnswers = emptyUserAnswers
        .setValue(ItemPage(Index(0), Index(0)), BigInt(1))
        .setValue(ItemPage(Index(0), Index(1)), BigInt(2))
        .setValue(ItemPage(Index(1), Index(0)), BigInt(3))
        .setValue(ItemPage(Index(1), Index(1)), BigInt(4))
        .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
        .setValue(ItemDescriptionPage(Index(0), Index(0)), "description 1")
        .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(1)), BigInt(2))
        .setValue(ItemDescriptionPage(Index(0), Index(1)), "description 2")
        .setValue(DeclarationGoodsItemNumberPage(Index(1), Index(0)), BigInt(3))
        .setValue(ItemDescriptionPage(Index(1), Index(0)), "description 3")
        .setValue(DeclarationGoodsItemNumberPage(Index(1), Index(1)), BigInt(4))
        .setValue(ItemDescriptionPage(Index(1), Index(1)), "description 4")

      val result1 = service.getGoodsReference(userAnswers, Index(0), Index(0))
      result1.value mustBe GoodsReference(BigInt(1), "description 1")

      val result2 = service.getGoodsReference(userAnswers, Index(0), Index(1))
      result2.value mustBe GoodsReference(BigInt(2), "description 2")

      val result3 = service.getGoodsReference(userAnswers, Index(1), Index(0))
      result3.value mustBe GoodsReference(BigInt(3), "description 3")

      val result4 = service.getGoodsReference(userAnswers, Index(1), Index(1))
      result4.value mustBe GoodsReference(BigInt(4), "description 4")
    }
  }

  "getGoodsReferences" - {
    import pages.sections.transport.equipment.ItemSection

    "must get goods references that have not already been applied to the transport equipment" - {
      "when there is no current index" - {
        "and none have been applied to the transport equipment" in {
          val userAnswers = emptyUserAnswers
            .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
            .setValue(ItemDescriptionPage(Index(0), Index(0)), "description 1")
            .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(1)), BigInt(2))
            .setValue(ItemDescriptionPage(Index(0), Index(1)), "description 2")
            .setValue(DeclarationGoodsItemNumberPage(Index(1), Index(0)), BigInt(3))
            .setValue(ItemDescriptionPage(Index(1), Index(0)), "description 3")
            .setValue(DeclarationGoodsItemNumberPage(Index(1), Index(1)), BigInt(4))
            .setValue(ItemDescriptionPage(Index(1), Index(1)), "description 4")

          val result = service.getGoodsReferences(userAnswers, Index(0), None)

          result mustBe Seq(
            GoodsReference(BigInt(1), "description 1"),
            GoodsReference(BigInt(2), "description 2"),
            GoodsReference(BigInt(3), "description 3"),
            GoodsReference(BigInt(4), "description 4")
          )
        }

        "and one has been applied to the transport equipment" in {
          val userAnswers = emptyUserAnswers
            .setValue(ItemPage(Index(0), Index(0)), BigInt(1))
            .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
            .setValue(ItemDescriptionPage(Index(0), Index(0)), "description 1")
            .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(1)), BigInt(2))
            .setValue(ItemDescriptionPage(Index(0), Index(1)), "description 2")
            .setValue(DeclarationGoodsItemNumberPage(Index(1), Index(0)), BigInt(3))
            .setValue(ItemDescriptionPage(Index(1), Index(0)), "description 3")
            .setValue(DeclarationGoodsItemNumberPage(Index(1), Index(1)), BigInt(4))
            .setValue(ItemDescriptionPage(Index(1), Index(1)), "description 4")

          val result = service.getGoodsReferences(userAnswers, Index(0), None)

          result mustBe Seq(
            GoodsReference(BigInt(2), "description 2"),
            GoodsReference(BigInt(3), "description 3"),
            GoodsReference(BigInt(4), "description 4")
          )
        }

        "and multiple have been applied to the transport equipment with a removal" in {
          val userAnswers = emptyUserAnswers
            .setValue(ItemPage(Index(0), Index(0)), BigInt(1))
            .setValue(ItemPage(Index(0), Index(1)), BigInt(2))
            .setRemoved(ItemSection(Index(0), Index(1)))
            .setValue(ItemPage(Index(0), Index(2)), BigInt(3))
            .setValue(ItemPage(Index(0), Index(3)), BigInt(4))
            .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
            .setValue(ItemDescriptionPage(Index(0), Index(0)), "description 1")
            .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(1)), BigInt(2))
            .setValue(ItemDescriptionPage(Index(0), Index(1)), "description 2")
            .setValue(DeclarationGoodsItemNumberPage(Index(1), Index(0)), BigInt(3))
            .setValue(ItemDescriptionPage(Index(1), Index(0)), "description 3")
            .setValue(DeclarationGoodsItemNumberPage(Index(1), Index(1)), BigInt(4))
            .setValue(ItemDescriptionPage(Index(1), Index(1)), "description 4")

          val result = service.getGoodsReferences(userAnswers, Index(0), None)

          result mustBe Seq(
            GoodsReference(BigInt(2), "description 2")
          )
        }
      }

      "when there is a current index" - {
        "and none have been applied to the transport equipment" in {
          val userAnswers = emptyUserAnswers
            .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
            .setValue(ItemDescriptionPage(Index(0), Index(0)), "description 1")
            .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(1)), BigInt(2))
            .setValue(ItemDescriptionPage(Index(0), Index(1)), "description 2")
            .setValue(DeclarationGoodsItemNumberPage(Index(1), Index(0)), BigInt(3))
            .setValue(ItemDescriptionPage(Index(1), Index(0)), "description 3")
            .setValue(DeclarationGoodsItemNumberPage(Index(1), Index(1)), BigInt(4))
            .setValue(ItemDescriptionPage(Index(1), Index(1)), "description 4")

          val result = service.getGoodsReferences(userAnswers, Index(0), Some(Index(0)))

          result mustBe Seq(
            GoodsReference(BigInt(1), "description 1"),
            GoodsReference(BigInt(2), "description 2"),
            GoodsReference(BigInt(3), "description 3"),
            GoodsReference(BigInt(4), "description 4")
          )
        }

        "and one has been applied to the transport equipment" in {
          val userAnswers = emptyUserAnswers
            .setValue(ItemPage(Index(0), Index(0)), BigInt(1))
            .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
            .setValue(ItemDescriptionPage(Index(0), Index(0)), "description 1")
            .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(1)), BigInt(2))
            .setValue(ItemDescriptionPage(Index(0), Index(1)), "description 2")
            .setValue(DeclarationGoodsItemNumberPage(Index(1), Index(0)), BigInt(3))
            .setValue(ItemDescriptionPage(Index(1), Index(0)), "description 3")
            .setValue(DeclarationGoodsItemNumberPage(Index(1), Index(1)), BigInt(4))
            .setValue(ItemDescriptionPage(Index(1), Index(1)), "description 4")

          val result = service.getGoodsReferences(userAnswers, Index(0), Some(Index(0)))

          result mustBe Seq(
            GoodsReference(BigInt(1), "description 1"),
            GoodsReference(BigInt(2), "description 2"),
            GoodsReference(BigInt(3), "description 3"),
            GoodsReference(BigInt(4), "description 4")
          )
        }

        "and multiple have been applied to the transport equipment with a removal" in {
          val userAnswers = emptyUserAnswers
            .setValue(ItemPage(Index(0), Index(0)), BigInt(1))
            .setValue(ItemPage(Index(0), Index(1)), BigInt(2))
            .setRemoved(ItemSection(Index(0), Index(1)))
            .setValue(ItemPage(Index(0), Index(2)), BigInt(3))
            .setValue(ItemPage(Index(0), Index(3)), BigInt(4))
            .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
            .setValue(ItemDescriptionPage(Index(0), Index(0)), "description 1")
            .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(1)), BigInt(2))
            .setValue(ItemDescriptionPage(Index(0), Index(1)), "description 2")
            .setValue(DeclarationGoodsItemNumberPage(Index(1), Index(0)), BigInt(3))
            .setValue(ItemDescriptionPage(Index(1), Index(0)), "description 3")
            .setValue(DeclarationGoodsItemNumberPage(Index(1), Index(1)), BigInt(4))
            .setValue(ItemDescriptionPage(Index(1), Index(1)), "description 4")

          val result = service.getGoodsReferences(userAnswers, Index(0), Some(Index(0)))

          result mustBe Seq(
            GoodsReference(BigInt(1), "description 1"),
            GoodsReference(BigInt(2), "description 2")
          )
        }
      }
    }
  }

  "getNextDeclarationGoodsItemNumber" - {
    "must return 1" - {
      "when no house consignments and no consignment items" in {
        val userAnswers = emptyUserAnswers

        val result = service.getNextDeclarationGoodsItemNumber(userAnswers)

        result mustBe BigInt(1)
      }
    }

    "must return next declaration goods item number" - {
      "when one house consignment with consignment items" in {
        val userAnswers = emptyUserAnswers
          .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
          .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(1)), BigInt(2))

        val result = service.getNextDeclarationGoodsItemNumber(userAnswers)

        result mustBe BigInt(3)
      }

      "when multiple house consignments with consignment items" in {
        val userAnswers = emptyUserAnswers
          .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
          .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(1)), BigInt(2))
          .setValue(DeclarationGoodsItemNumberPage(Index(1), Index(0)), BigInt(3))
          .setValue(DeclarationGoodsItemNumberPage(Index(1), Index(1)), BigInt(4))
          .setValue(DeclarationGoodsItemNumberPage(Index(1), Index(2)), BigInt(5))

        val result = service.getNextDeclarationGoodsItemNumber(userAnswers)

        result mustBe BigInt(6)
      }
    }
  }

  "removeEmptyItems" - {
    import pages.sections.ItemSection

    "when there is an item with nothing in it" - {
      "must remove it" in {
        val userAnswers = emptyUserAnswers
          .setValue(ItemSection(Index(0), Index(0)), Json.obj())

        val result = service.removeEmptyItems(userAnswers, Index(0))

        result.get(ItemSection(Index(0), Index(0))) must not be defined
        result.get(ItemSection(Index(1), Index(0))) must not be defined
      }
    }

    "when there is an item with just a declaration goods item number" - {
      "must remove it" in {
        val userAnswers = emptyUserAnswers
          .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))

        val result = service.removeEmptyItems(userAnswers, Index(0))

        result.get(ItemSection(Index(0), Index(0))) must not be defined
      }
    }

    "when there is an item with a goods item number and a declaration goods item number" - {
      "must not remove it" in {
        val userAnswers = emptyUserAnswers
          .setSequenceNumber(ItemSection(Index(0), Index(0)), 1)
          .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))

        val result = service.removeEmptyItems(userAnswers, Index(0))

        result.get(ItemSection(Index(0), Index(0))) mustBe defined
        result.get(DeclarationGoodsItemNumberPage(Index(0), Index(0))) mustBe defined
      }
    }

    "when there are empty and non-empty items" - {
      "must remove the empty items" in {
        val userAnswers = emptyUserAnswers
          // Item 1 - Imported from IE043
          .setSequenceNumber(ItemSection(Index(0), Index(0)), 1)
          .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
          .setNotRemoved(ItemSection(Index(0), Index(0)))
          // Item 2 - Semi-added in IE044 (needs removing)
          .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(1)), BigInt(2))
          // Item 3 - Semi-added in IE044 (needs removing)
          .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(2)), BigInt(3))
          // Item 4 - Added in IE044
          .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(3)), BigInt(4))
          .setValue(ItemDescriptionPage(Index(0), Index(3)), "item4Description")
          // Item 5 - Added in IE044
          .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(4)), BigInt(5))
          .setValue(ItemDescriptionPage(Index(0), Index(4)), "item5Description")
          // Item 6 - Semi-added in IE044 (needs removing)
          .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(5)), BigInt(6))

        val result = service.removeEmptyItems(userAnswers, Index(0))

        result.get(ItemSection(Index(0), Index(0))) mustBe defined
        result.get(DeclarationGoodsItemNumberPage(Index(0), Index(0))).value mustBe BigInt(1)

        result.get(ItemSection(Index(0), Index(1))) mustBe defined
        result.get(DeclarationGoodsItemNumberPage(Index(0), Index(1))).value mustBe BigInt(4)

        result.get(ItemSection(Index(0), Index(2))) mustBe defined
        result.get(DeclarationGoodsItemNumberPage(Index(0), Index(2))).value mustBe BigInt(5)
      }
    }
  }
}
