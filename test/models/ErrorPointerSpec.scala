/*
 * Copyright 2022 HM Revenue & Customs
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

package models

import com.lucidchart.open.xtract.XmlReader
import generators.Generators
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ErrorPointerSpec extends AnyFreeSpec with Generators with ScalaCheckPropertyChecks with Matchers with OptionValues {

  "ErrorPointer" - {
    "must read xml for single items" in {
      forAll(Gen.oneOf(ErrorPointer.values)) {
        pointer =>
          val xml = <test>{pointer.value}</test>
          XmlReader.of[ErrorPointer].read(xml).toOption.value mustBe pointer
      }
    }

    "must return DefaultPointer" in {

      forAll(
        nonEmptyString suchThat (
          x => !ErrorPointer.values.contains(x)
        )
      ) {
        string =>
          val xml = <test>{string}</test>
          XmlReader.of[ErrorPointer].read(xml).toOption.value mustBe DefaultPointer(string)
      }

    }

  }

}
