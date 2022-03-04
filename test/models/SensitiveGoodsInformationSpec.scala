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

import com.lucidchart.open.xtract.{ParseSuccess, XmlReader}
import generators.Generators
import models.XMLWrites._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.StreamlinedXmlEquality
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.xml.NodeSeq

class SensitiveGoodsInformationSpec extends AnyFreeSpec with Matchers with Generators with ScalaCheckPropertyChecks with StreamlinedXmlEquality {

  "SensitiveGoodsInformation" - {

    "must serialise SensitiveGoodsInformation from xml" in {

      forAll(arbitrary[SensitiveGoodsInformation]) {

        sensitiveGoodsInformation =>
          val goodsCode = sensitiveGoodsInformation.goodsCode
            .map {
              code =>
                <SenGooCodSD22>{code}</SenGooCodSD22>
            }

          val result =
            <SGICODSD2>
              {goodsCode.getOrElse(NodeSeq.Empty)}
              <SenQuaSD23>{sensitiveGoodsInformation.quantity}</SenQuaSD23>
            </SGICODSD2>

          XmlReader.of[SensitiveGoodsInformation].read(result) mustBe
            ParseSuccess(SensitiveGoodsInformation(sensitiveGoodsInformation.goodsCode, sensitiveGoodsInformation.quantity))
      }

    }

    "must serialize SensitiveGoodsInformation to xml" in {
      forAll(arbitrary[SensitiveGoodsInformation]) {
        sensitiveGoodsInformation =>
          val goodsCode = sensitiveGoodsInformation.goodsCode.fold(NodeSeq.Empty) {
            code =>
              <SenGooCodSD22>{code}</SenGooCodSD22>
          }

          val result =
            <SGICODSD2>
              {goodsCode}
              <SenQuaSD23>{sensitiveGoodsInformation.quantity}</SenQuaSD23>
            </SGICODSD2>

          sensitiveGoodsInformation.toXml mustEqual result
      }

    }

  }

}
