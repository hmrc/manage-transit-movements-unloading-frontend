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

package models

import base.{AppWithDefaultMockFixtures, SpecBase}
import pages.QuestionPage
import pages.sections.Section
import play.api.libs.json._
import play.api.test.Helpers.running

import java.time.Instant

class UserAnswersSpec extends SpecBase with AppWithDefaultMockFixtures {

  "UserAnswers" - {

    case object TestPage extends QuestionPage[String] {
      override def path: JsPath = JsPath \ "foo"
    }

    "getAndCopyTo" - {
      "must copy value from one user answers to another" in {
        val json         = JsObject(Map("foo" -> JsString("bar")))
        val userAnswers1 = emptyUserAnswers.copy(data = json)
        val userAnswers2 = emptyUserAnswers

        val result = userAnswers1.getAndCopyTo(JsPath \ "foo", userAnswers2)

        result.data mustEqual json
      }

      "must return unchanged answers when get retrieves nothing" in {
        val json         = JsObject(Map("foo" -> JsString("bar")))
        val userAnswers1 = emptyUserAnswers
        val userAnswers2 = emptyUserAnswers.copy(data = json)

        val result = userAnswers1.getAndCopyTo(JsPath \ "foo", userAnswers2)

        result.data mustEqual json
      }
    }

    "get" - {

      "must return data when defined" in {

        val userAnswers = UserAnswers(arrivalId, mrn, eoriNumber, basicIe043, JsObject(Map("foo" -> JsString("bar"))), Instant.now())

        userAnswers.get(TestPage).value mustEqual "bar"
      }

      "must return None when not defined" in {

        val userAnswers = UserAnswers(arrivalId, mrn, eoriNumber, basicIe043, Json.obj(), Instant.now())

        userAnswers.get(TestPage) must not be defined
      }
    }

    "set" - {

      "must set data" in {

        val userAnswers = UserAnswers(arrivalId, mrn, eoriNumber, basicIe043, Json.obj(), Instant.now())

        val expectedUserAnswers = userAnswers.copy(data = JsObject(Map("foo" -> JsString("bar"))))

        val result = userAnswers.set(TestPage, "bar").toOption.value.data

        result mustEqual expectedUserAnswers.data
      }
    }

    "remove" - {

      "must remove data" in {

        val userAnswers = UserAnswers(arrivalId, mrn, eoriNumber, basicIe043, JsObject(Map("foo" -> JsString("bar"))), Instant.now())

        val expectedUserAnswers = userAnswers.copy(data = Json.obj())

        val result = userAnswers.remove(TestPage).toOption.value.data

        result mustEqual expectedUserAnswers.data
      }
    }

    "formats" - {

      val userAnswers = UserAnswers(
        id = arrivalId,
        mrn = mrn,
        eoriNumber = eoriNumber,
        ie043Data = basicIe043,
        data = Json.obj(),
        lastUpdated = Instant.ofEpochMilli(1662546803472L)
      )

      "when encryption enabled" - {
        val app = guiceApplicationBuilder()
          .configure("encryption.enabled" -> true)
          .build()

        running(app) {
          val sensitiveFormats                     = app.injector.instanceOf[SensitiveFormats]
          implicit val format: Format[UserAnswers] = UserAnswers.format(sensitiveFormats)

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "${arrivalId.value}",
               |  "mrn" : "$mrn",
               |  "eoriNumber" : "${eoriNumber.value}",
               |  "ie043Data" : "$encryptedIe043",
               |  "data" : "T+FWrvLPJMKyRZ1aoW8rdZmETyL89CdpWxaog0joG6B/hxCF",
               |  "lastUpdated" : {
               |    "$$date" : {
               |      "$$numberLong" : "1662546803472"
               |    }
               |  }
               |}
               |""".stripMargin)

          "read correctly" in {
            val result = json.as[UserAnswers]
            result mustEqual userAnswers
          }

          "write and read correctly" in {
            val result = Json.toJson(userAnswers).as[UserAnswers]
            result mustEqual userAnswers
          }
        }
      }

      "when encryption disabled" - {
        val app = guiceApplicationBuilder()
          .configure("encryption.enabled" -> false)
          .build()

        running(app) {
          val sensitiveFormats                     = app.injector.instanceOf[SensitiveFormats]
          implicit val format: Format[UserAnswers] = UserAnswers.format(sensitiveFormats)

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "${arrivalId.value}",
               |  "mrn" : "$mrn",
               |  "eoriNumber" : "${eoriNumber.value}",
               |  "ie043Data" : "${basicIe043.toXML.toString()}",
               |  "data" : {},
               |  "lastUpdated" : {
               |    "$$date" : {
               |      "$$numberLong" : "1662546803472"
               |    }
               |  }
               |}
               |""".stripMargin)

          "must read correctly" in {
            val result = json.as[UserAnswers]
            result mustEqual userAnswers
          }

          "write correctly" in {
            val result = Json.toJson(userAnswers)
            result mustEqual json
          }
        }
      }
    }

    "auditWrites" - {
      "must write data as unencrypted" in {
        val userAnswers = UserAnswers(
          id = arrivalId,
          mrn = mrn,
          eoriNumber = eoriNumber,
          ie043Data = basicIe043,
          data = Json.obj("bar" -> "baz"),
          lastUpdated = Instant.ofEpochMilli(1662546803472L)
        )

        val result = Json.toJson(userAnswers)(UserAnswers.auditWrites)

        val json: JsValue = Json.parse(s"""
             |{
             |  "_id" : "${arrivalId.value}",
             |  "mrn" : "$mrn",
             |  "eoriNumber" : "${eoriNumber.value}",
             |  "ie043Data" : "${basicIe043.toXML.toString()}",
             |  "data" : {
             |    "bar" : "baz"
             |  },
             |  "lastUpdated" : {
             |    "$$date" : {
             |      "$$numberLong" : "1662546803472"
             |    }
             |  }
             |}
             |""".stripMargin)

        result mustEqual json
      }
    }

    "remove fields methods" - {

      case object FakeSection extends Section[JsObject] {
        override def path: JsPath = JsPath \ "some" \ "example" \ "path" \ 0
      }

      "removeDataGroup" - {
        "must remove everything except sequence number" - {
          "when only one element" in {
            val data = Json
              .parse("""
                  |{
                  |  "some" : {
                  |    "example" : {
                  |      "path" : [
                  |        {
                  |          "sequenceNumber" : "1",
                  |          "foo" : "foo",
                  |          "bar" : "bar",
                  |          "baz" : "baz"
                  |        }
                  |      ]
                  |    }
                  |  }
                  |}
                  |""".stripMargin)
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(data = data)
            val result      = userAnswers.removeDataGroup(FakeSection).get
            result.data mustEqual Json.parse("""
                |{
                |  "some" : {
                |    "example" : {
                |      "path" : [
                |        {
                |          "sequenceNumber" : "1",
                |          "removed" : true
                |        }
                |      ]
                |    }
                |  }
                |}
                |""".stripMargin)
          }

          "when one of several elements" in {
            case object FakeSection extends Section[JsObject] {
              override def path: JsPath = JsPath \ "some" \ "example" \ "path" \ 1
            }
            val data = Json
              .parse("""
                  |{
                  |  "some" : {
                  |    "example" : {
                  |      "path" : [
                  |        {
                  |          "sequenceNumber" : "1",
                  |          "foo" : "foo",
                  |          "bar" : "bar",
                  |          "baz" : "baz"
                  |        },
                  |        {
                  |          "sequenceNumber" : "2",
                  |          "foo" : "foo",
                  |          "bar" : "bar",
                  |          "baz" : "baz"
                  |        },
                  |        {
                  |          "sequenceNumber" : "3",
                  |          "foo" : "foo",
                  |          "bar" : "bar",
                  |          "baz" : "baz"
                  |        }
                  |      ]
                  |    }
                  |  }
                  |}
                  |""".stripMargin)
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(data = data)
            val result      = userAnswers.removeDataGroup(FakeSection).get
            result.data mustEqual Json.parse("""
                |{
                |  "some" : {
                |    "example" : {
                |      "path" : [
                |        {
                |          "sequenceNumber" : "1",
                |          "foo" : "foo",
                |          "bar" : "bar",
                |          "baz" : "baz"
                |        },
                |        {
                |          "sequenceNumber" : "2",
                |          "removed" : true
                |        },
                |        {
                |          "sequenceNumber" : "3",
                |          "foo" : "foo",
                |          "bar" : "bar",
                |          "baz" : "baz"
                |        }
                |      ]
                |    }
                |  }
                |}
                |""".stripMargin)
          }

          "when element was newly added so doesn't have a sequence number" in {
            case object FakeSection extends Section[JsObject] {
              override def path: JsPath = JsPath \ "some" \ "example" \ "path" \ 1
            }
            val data = Json
              .parse("""
                  |{
                  |  "some" : {
                  |    "example" : {
                  |      "path" : [
                  |        {
                  |          "sequenceNumber" : "1",
                  |          "foo" : "foo",
                  |          "bar" : "bar",
                  |          "baz" : "baz"
                  |        },
                  |        {
                  |          "foo" : "foo",
                  |          "bar" : "bar",
                  |          "baz" : "baz"
                  |        },
                  |        {
                  |          "sequenceNumber" : "3",
                  |          "foo" : "foo",
                  |          "bar" : "bar",
                  |          "baz" : "baz"
                  |        }
                  |      ]
                  |    }
                  |  }
                  |}
                  |""".stripMargin)
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(data = data)
            val result      = userAnswers.removeDataGroup(FakeSection).get
            result.data mustEqual Json.parse("""
                |{
                |  "some" : {
                |    "example" : {
                |      "path" : [
                |        {
                |          "sequenceNumber" : "1",
                |          "foo" : "foo",
                |          "bar" : "bar",
                |          "baz" : "baz"
                |        },
                |        {
                |          "sequenceNumber" : "3",
                |          "foo" : "foo",
                |          "bar" : "bar",
                |          "baz" : "baz"
                |        }
                |      ]
                |    }
                |  }
                |}
                |""".stripMargin)
          }
        }
      }

      "removeItem" - {
        "must remove everything except sequence number and declaration goods item number" - {
          "when sequence number defined" in {
            val data = Json
              .parse("""
                  |{
                  |  "some" : {
                  |    "example" : {
                  |      "path" : [
                  |        {
                  |          "sequenceNumber" : "1",
                  |          "declarationGoodsItemNumber" : 1,
                  |          "foo" : "foo",
                  |          "bar" : "bar",
                  |          "baz" : "baz"
                  |        }
                  |      ]
                  |    }
                  |  }
                  |}
                  |""".stripMargin)
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(data = data)
            val result      = userAnswers.removeItem(FakeSection).get
            result.data mustEqual Json.parse("""
                |{
                |  "some" : {
                |    "example" : {
                |      "path" : [
                |        {
                |          "sequenceNumber" : "1",
                |          "declarationGoodsItemNumber" : 1,
                |          "removed" : true
                |        }
                |      ]
                |    }
                |  }
                |}
                |""".stripMargin)
          }

          "when sequence number undefined" in {
            val data = Json
              .parse("""
                  |{
                  |  "some" : {
                  |    "example" : {
                  |      "path" : [
                  |        {
                  |          "declarationGoodsItemNumber" : 1,
                  |          "foo" : "foo",
                  |          "bar" : "bar",
                  |          "baz" : "baz"
                  |        }
                  |      ]
                  |    }
                  |  }
                  |}
                  |""".stripMargin)
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(data = data)
            val result      = userAnswers.removeItem(FakeSection).get
            result.data mustEqual Json.parse("""
                |{
                |  "some" : {
                |    "example" : {
                |      "path" : []
                |    }
                |  }
                |}
                |""".stripMargin)
          }
        }
      }

      "removeDocument" - {
        "must remove everything except sequence number and document type" - {
          "when sequence number defined" in {
            val data = Json
              .parse("""
                  |{
                  |  "some" : {
                  |    "example" : {
                  |      "path" : [
                  |        {
                  |          "sequenceNumber" : "1",
                  |          "type" : {
                  |            "type" : "Supporting",
                  |            "code" : "N002",
                  |            "description" : "Certificate of conformity with the European Union marketing standards for fresh fruit and vegetables"
                  |          },
                  |          "foo" : "foo",
                  |          "bar" : "bar",
                  |          "baz" : "baz"
                  |        }
                  |      ]
                  |    }
                  |  }
                  |}
                  |""".stripMargin)
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(data = data)
            val result      = userAnswers.removeDocument(FakeSection).get
            result.data mustEqual Json.parse("""
                |{
                |  "some" : {
                |    "example" : {
                |      "path" : [
                |        {
                |          "sequenceNumber" : "1",
                |          "type" : {
                |            "type" : "Supporting"
                |          },
                |          "removed" : true
                |        }
                |      ]
                |    }
                |  }
                |}
                |""".stripMargin)
          }

          "when sequence number undefined" in {
            val data = Json
              .parse("""
                  |{
                  |  "some" : {
                  |    "example" : {
                  |      "path" : [
                  |        {
                  |          "type" : {
                  |            "type" : "Supporting",
                  |            "code" : "N002",
                  |            "description" : "Certificate of conformity with the European Union marketing standards for fresh fruit and vegetables"
                  |          },
                  |          "foo" : "foo",
                  |          "bar" : "bar",
                  |          "baz" : "baz"
                  |        }
                  |      ]
                  |    }
                  |  }
                  |}
                  |""".stripMargin)
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(data = data)
            val result      = userAnswers.removeDocument(FakeSection).get
            result.data mustEqual Json.parse("""
                |{
                |  "some" : {
                |    "example" : {
                |      "path" : []
                |    }
                |  }
                |}
                |""".stripMargin)
          }
        }
      }

      "removeSeal" - {
        "must remove everything except sequence number and seal identifier" - {
          "when sequence number defined" in {
            val data = Json
              .parse("""
                  |{
                  |  "some" : {
                  |    "example" : {
                  |      "path" : [
                  |        {
                  |          "sequenceNumber" : "1",
                  |          "identifier" : "1"
                  |        }
                  |      ]
                  |    }
                  |  }
                  |}
                  |""".stripMargin)
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(data = data)
            val result      = userAnswers.removeSeal(FakeSection).get
            result.data mustEqual Json.parse("""
                |{
                |  "some" : {
                |    "example" : {
                |      "path" : [
                |        {
                |          "sequenceNumber" : "1",
                |          "identifier" : "1",
                |          "removed" : true
                |        }
                |      ]
                |    }
                |  }
                |}
                |""".stripMargin)
          }

          "when sequence number undefined" in {
            val data = Json
              .parse("""
                  |{
                  |  "some" : {
                  |    "example" : {
                  |      "path" : [
                  |        {
                  |          "identifier" : "1"
                  |        }
                  |      ]
                  |    }
                  |  }
                  |}
                  |""".stripMargin)
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(data = data)
            val result      = userAnswers.removeSeal(FakeSection).get
            result.data mustEqual Json.parse("""
                |{
                |  "some" : {
                |    "example" : {
                |      "path" : []
                |    }
                |  }
                |}
                |""".stripMargin)
          }
        }
      }

      "removeGoodsReference" - {
        "must remove everything except sequence number and declaration goods item number" - {
          "when sequence number defined" in {
            val data = Json
              .parse("""
                  |{
                  |  "some" : {
                  |    "example" : {
                  |      "path" : [
                  |        {
                  |          "sequenceNumber" : "1",
                  |          "declarationGoodsItemNumber" : 1
                  |        }
                  |      ]
                  |    }
                  |  }
                  |}
                  |""".stripMargin)
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(data = data)
            val result      = userAnswers.removeGoodsReference(FakeSection).get
            result.data mustEqual Json.parse("""
                |{
                |  "some" : {
                |    "example" : {
                |      "path" : [
                |        {
                |          "sequenceNumber" : "1",
                |          "declarationGoodsItemNumber" : 1,
                |          "removed" : true
                |        }
                |      ]
                |    }
                |  }
                |}
                |""".stripMargin)
          }

          "when sequence number undefined" in {
            val data = Json
              .parse("""
                  |{
                  |  "some" : {
                  |    "example" : {
                  |      "path" : [
                  |        {
                  |          "declarationGoodsItemNumber" : 1
                  |        }
                  |      ]
                  |    }
                  |  }
                  |}
                  |""".stripMargin)
              .as[JsObject]

            val userAnswers = emptyUserAnswers.copy(data = data)
            val result      = userAnswers.removeGoodsReference(FakeSection).get
            result.data mustEqual Json.parse("""
                |{
                |  "some" : {
                |    "example" : {
                |      "path" : []
                |    }
                |  }
                |}
                |""".stripMargin)
          }
        }
      }
    }
  }
}
