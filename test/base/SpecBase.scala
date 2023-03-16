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

package base

import cats.data.NonEmptyList
import models.P5.IE043Data
import models.{ArrivalId, EoriNumber, GoodsItem, Index, MovementReferenceNumber, Packages, ProducedDocument, UserAnswers}
import org.scalatest._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.QuestionPage
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.libs.json.{JsObject, JsValue, Json, Reads, Writes}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import models.P5._

import java.time.Instant

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with OptionValues
    with TryValues
    with EitherValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with GuiceOneAppPerSuite {

  val arrivalId: ArrivalId = ArrivalId("AB123")

  val configKey = "config"

  def injector: Injector = app.injector

  val mrn: MovementReferenceNumber = MovementReferenceNumber("19", "GB", "1234567890123")
  val eoriNumber: EoriNumber       = EoriNumber("id")

  val index: Index                  = Index(0)
  val equipmentIndex: Index         = Index(0)
  val sealIndex: Index              = Index(0)
  val itemIndex: Index              = Index(0)
  def emptyUserAnswers: UserAnswers = UserAnswers(arrivalId, mrn, eoriNumber, Json.obj(), Json.obj(), Instant.now())
  def ie043UserAnswers: UserAnswers = UserAnswers(arrivalId, mrn, eoriNumber, ieo43Data, ieo43Data, Instant.now())

  protected lazy val packages: Packages = Packages(Some("Ref."), "BX", Some(1), None)

  protected lazy val producedDocuments: ProducedDocument = ProducedDocument("235", Some("Ref."), None)

  protected lazy val goodsItemMandatory: GoodsItem = GoodsItem(
    itemNumber = 1,
    commodityCode = None,
    description = "Flowers",
    GrossWeight = Some("1000"),
    netMass = Some("999"),
    producedDocuments = Seq.empty,
    containers = Seq.empty,
    packages = NonEmptyList(packages, Nil),
    sensitiveGoodsInformation = Seq.empty
  )

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  def messagesApi: MessagesApi    = app.injector.instanceOf[MessagesApi]
  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  implicit class RichUserAnswers(userAnswers: UserAnswers) {

    def getValue[T](page: QuestionPage[T])(implicit rds: Reads[T]): T =
      userAnswers.get(page).value

    def setValue[T](page: QuestionPage[T], value: T)(implicit wts: Writes[T]): UserAnswers =
      userAnswers.set(page, value).success.value

    def removeValue(page: QuestionPage[_]): UserAnswers =
      userAnswers.remove(page).success.value
  }

  val ieo43Data: JsObject = Json.toJsObject(
    MessageData(
      TransitOperation(
        MRN = MovementReferenceNumber("38VYQTYFU3T0KUTUM3").get
      ),
      Consignment = Consignment(
        Option(
          List(
            TransportEquipment(
              sequenceNumber = Option("te1"),
              containerIdentificationNumber = Option("cin-1"),
              numberOfSeals = Option(103),
              Seal = Option(
                List(
                  Seal(
                    sequenceNumber = Option("1001"),
                    identifier = Option("1002")
                  )
                )
              )
            )
          )
        ),
        DepartureTransportMeans = Option(
          List(
            DepartureTransportMeans(
              sequenceNumber = Option("dtm-1"),
              typeOfIdentification = Option("4"),
              identificationNumber = Option("28"),
              nationality = Option("DE")
            )
          )
        ),
        HouseConsignment = List(
          HouseConsignment(
            Option("hc1"),
            DepartureTransportMeans = Option(
              List(
                DepartureTransportMeans(
                  sequenceNumber = Option("56"),
                  typeOfIdentification = Option("2"),
                  identificationNumber = Option("23"),
                  nationality = Option("IT")
                )
              )
            ),
            ConsignmentItem = Option(
              List(
                ConsignmentItem(
                  Option("6"),
                  Option(100),
                  Commodity = Option(
                    Commodity(
                      Option("shirts"),
                      GoodsMeasure = Option(
                        GoodsMeasure(
                          grossMass = Option(123.45),
                          netMass = Option(123.45)
                        )
                      )
                    )
                  ),
                  Packaging = Option(
                    List(
                      Packaging(
                        Some("5"),
                        Some(99)
                      )
                    )
                  )
                )
              )
            )
          )
        )
      ),
      CustomsOfficeOfDestinationActual = CustomsOfficeOfDestinationActual(referenceNumber = "GB000008")
    )
  )

}
