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

package connectors

import java.time.LocalDate

import com.github.tomakehurst.wiremock.client.WireMock._
import generators.MessagesModelGenerators
import models.XMLWrites._
import models._
import models.messages.UnloadingRemarksRequest
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, StreamlinedXmlEquality}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.xml.NodeSeq

class UnloadingConnectorSpec
    extends AnyFreeSpec
    with ScalaFutures
    with IntegrationPatience
    with WireMockSuite
    with Matchers
    with OptionValues
    with MessagesModelGenerators
    with StreamlinedXmlEquality
    with ScalaCheckPropertyChecks {

  import UnloadingConnectorSpec._

  override protected def portConfigKey: String = "microservice.services.arrivals-backend.port"

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "UnloadingConnectorSpec" - {

    "post" - {

      "should handle an ACCEPTED response" in {
        server.stubFor(
          post(postUri)
            .withHeader("Channel", containing("web"))
            .withHeader("Content-Type", containing("application/xml"))
            .willReturn(status(ACCEPTED)))

        val app = appBuilder.build()

        running(app) {
          val connector: UnloadingConnector = app.injector.instanceOf[UnloadingConnector]
          val unloadingRemarksRequest       = arbitrary[UnloadingRemarksRequest].sample.value

          val result = connector.post(arrivalId, unloadingRemarksRequest).futureValue

          result.status mustBe ACCEPTED
        }
      }

      "should handle client and server errors" in {

        val app = appBuilder.build()

        running(app) {
          val errorResponsesCodes: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)
          val connector: UnloadingConnector = app.injector.instanceOf[UnloadingConnector]

          forAll(arbitrary[UnloadingRemarksRequest], errorResponsesCodes) {
            (unloadingRemarksRequest, errorResponseCode) =>
              server.stubFor(
                post(postUri)
                  .withHeader("Channel", containing("web"))
                  .withHeader("Content-Type", containing("application/xml"))
                  .willReturn(aResponse().withStatus(errorResponseCode)))

              connector.post(arrivalId, unloadingRemarksRequest).futureValue.status mustBe errorResponseCode
          }
        }
      }
    }

    "getUnloadingPermission" - {

      "must return valid UnloadingPermission" in {

        val json = Json.obj("message" -> unloadingPermission.toString())

        server.stubFor(
          get(urlEqualTo(unloadingPermissionUrl))
            .withHeader("Channel", containing("web"))
            .willReturn(
              okJson(json.toString)
            )
        )

        val app = appBuilder.build()
        running(app) {
          val connector: UnloadingConnector = app.injector.instanceOf[UnloadingConnector]
          val result                        = connector.getUnloadingPermission(unloadingPermissionUrl).futureValue
          result.value mustBe an[UnloadingPermission]
        }
      }

      "must return None when xml fails conversion" in {

        val expectedResult: NodeSeq = {
          <CC043A>
          </CC043A>
        }

        val json = Json.obj("message" -> expectedResult.toString())

        server.stubFor(
          get(urlEqualTo(unloadingPermissionUrl))
            .withHeader("Channel", containing("web"))
            .willReturn(
              okJson(json.toString)
            )
        )

        val app = appBuilder.build()
        running(app) {
          val connector: UnloadingConnector = app.injector.instanceOf[UnloadingConnector]
          connector.getUnloadingPermission(unloadingPermissionUrl).futureValue mustBe None
        }
      }

      "must return 'None' when an error response is returned" in {
        forAll(responseCodes) {
          code: Int =>
            server.stubFor(
              get(unloadingPermissionUrl)
                .withHeader("Channel", containing("web"))
                .willReturn(aResponse().withStatus(code))
            )

            val app = appBuilder.build()
            running(app) {
              val connector: UnloadingConnector = app.injector.instanceOf[UnloadingConnector]
              connector.getUnloadingPermission(unloadingPermissionUrl).futureValue mustBe None
            }
        }
      }
    }

    "getSummary" - {

      "must be return summary of messages" in {
        val json = Json.obj(
          "arrivalId" -> arrivalId.value,
          "messages" -> Json.obj(
            "IE043" -> s"/movements/arrivals/${arrivalId.value}/messages/3",
            "IE044" -> s"/movements/arrivals/${arrivalId.value}/messages/4",
            "IE058" -> s"/movements/arrivals/${arrivalId.value}/messages/5"
          )
        )

        val messageAction =
          MessagesSummary(
            arrivalId,
            MessagesLocation(
              s"/movements/arrivals/${arrivalId.value}/messages/3",
              Some(s"/movements/arrivals/${arrivalId.value}/messages/4"),
              Some(s"/movements/arrivals/${arrivalId.value}/messages/5")
            )
          )

        server.stubFor(
          get(urlEqualTo(summaryUri))
            .withHeader("Channel", containing("web"))
            .willReturn(
              okJson(json.toString)
            )
        )

        val app = appBuilder.build()
        running(app) {
          val connector: UnloadingConnector = app.injector.instanceOf[UnloadingConnector]
          connector.getSummary(arrivalId).futureValue mustBe Some(messageAction)
        }
      }

      "must return 'None' when an error response is returned from getSummary" in {
        forAll(responseCodes) {
          code: Int =>
            server.stubFor(
              get(summaryUri)
                .withHeader("Channel", containing("web"))
                .willReturn(aResponse().withStatus(code))
            )

            val app = appBuilder.build()
            running(app) {
              val connector: UnloadingConnector = app.injector.instanceOf[UnloadingConnector]
              connector.getSummary(ArrivalId(1)).futureValue mustBe None
            }
        }
      }
    }

    "getRejectionMessage" - {
      "must return valid 'rejection message'" in {
        val genRejectionError     = arbitrary[ErrorType].sample.value
        val rejectionXml: NodeSeq = <CC058A>
          <HEAHEA>
            <DocNumHEA5>19IT021300100075E9</DocNumHEA5>
            <UnlRemRejDatHEA218>20191018</UnlRemRejDatHEA218>
          </HEAHEA>
          <FUNERRER1>
            <ErrTypER11>{genRejectionError.code}</ErrTypER11>
            <ErrPoiER12>Message type</ErrPoiER12>
            <OriAttValER14>GB007A</OriAttValER14>
          </FUNERRER1>
        </CC058A>

        val json = Json.obj("message" -> rejectionXml.toString())

        server.stubFor(
          get(urlEqualTo(rejectionUri))
            .withHeader("Channel", containing("web"))
            .willReturn(
              okJson(json.toString)
            )
        )

        val expectedResult = Some(
          UnloadingRemarksRejectionMessage(
            MovementReferenceNumber("19IT021300100075E9").get,
            LocalDate.of(2019, 10, 18),
            None,
            List(FunctionalError(genRejectionError, DefaultPointer("Message type"), None, Some("GB007A")))
          ))

        val app = appBuilder.build()
        running(app) {
          val connector: UnloadingConnector = app.injector.instanceOf[UnloadingConnector]
          val result                        = connector.getRejectionMessage(rejectionUri).futureValue
          result mustBe expectedResult
        }
      }

      "must return None for malformed xml'" in {
        val rejectionXml: NodeSeq = <CC058A>
          <HEAHEA>
            <DocNumHEA5>19IT021300100075E9</DocNumHEA5>
          </HEAHEA>
        </CC058A>

        val json = Json.obj("message" -> rejectionXml.toString())

        server.stubFor(
          get(urlEqualTo(rejectionUri))
            .withHeader("Channel", containing("web"))
            .willReturn(
              okJson(json.toString)
            )
        )

        val app = appBuilder.build()
        running(app) {
          val connector: UnloadingConnector = app.injector.instanceOf[UnloadingConnector]
          connector.getRejectionMessage(rejectionUri).futureValue mustBe None
        }
      }

      "must return None when an error response is returned from getRejectionMessage" in {
        forAll(responseCodes) {
          code: Int =>
            server.stubFor(
              get(rejectionUri)
                .withHeader("Channel", containing("web"))
                .willReturn(aResponse().withStatus(code))
            )

            val app = appBuilder.build()
            running(app) {
              val connector: UnloadingConnector = app.injector.instanceOf[UnloadingConnector]
              connector.getRejectionMessage(rejectionUri).futureValue mustBe None
            }
        }
      }
    }

    "getUnloadingRemarksMessage" - {
      "must return valid 'unloading remarks message'" in {
        val unloadingRemarksRequest = arbitrary[UnloadingRemarksRequest].sample.value

        val json = Json.obj("message" -> unloadingRemarksRequest.toXml.toString())

        server.stubFor(
          get(urlEqualTo(unloadingRemarksUri))
            .withHeader("Channel", containing("web"))
            .willReturn(
              okJson(json.toString)
            )
        )

        val app = appBuilder.build()
        running(app) {
          val connector: UnloadingConnector = app.injector.instanceOf[UnloadingConnector]
          val result                        = connector.getUnloadingRemarksMessage(unloadingRemarksUri).futureValue.value
          result mustBe unloadingRemarksRequest
        }
      }

      "must return None when an error response is returned from getUnloadingRemarksMessage" in {
        forAll(responseCodes) {
          code: Int =>
            server.stubFor(
              get(unloadingRemarksUri)
                .withHeader("Channel", containing("web"))
                .willReturn(aResponse().withStatus(code))
            )

            val app = appBuilder.build()
            running(app) {
              val connector: UnloadingConnector = app.injector.instanceOf[UnloadingConnector]
              connector.getUnloadingRemarksMessage(rejectionUri).futureValue mustBe None
            }
        }
      }
    }

    "getPDF" - {
      "must return status Ok" in {

        server.stubFor(
          get(urlEqualTo(getPDFUrl))
            .withHeader("Channel", containing("web"))
            .withHeader("Authorization", containing("bearerToken"))
            .willReturn(
              aResponse()
                .withStatus(200)
            )
        )

        val app = appBuilder.build()
        running(app) {
          val connector: UnloadingConnector = app.injector.instanceOf[UnloadingConnector]
          val result: Future[WSResponse]    = connector.getPDF(arrivalId, "bearerToken")

          result.futureValue.status mustBe 200
        }
      }
      "must return other error status codes without exceptions" in {

        forAll(responseCodes) {
          responseCode =>
            server.stubFor(
              get(urlEqualTo(getPDFUrl))
                .withHeader("Channel", containing("web"))
                .willReturn(
                  aResponse()
                    .withStatus(responseCode)
                )
            )

            val app = appBuilder.build()
            running(app) {
              val connector: UnloadingConnector = app.injector.instanceOf[UnloadingConnector]
              val result: Future[WSResponse]    = connector.getPDF(arrivalId, "bearerToken")

              result.futureValue.status mustBe responseCode
            }
        }
      }
    }
  }

}

object UnloadingConnectorSpec {

  val unloadingPermission: NodeSeq = {
    <CC043A>
      <SynIdeMES1>UNOC</SynIdeMES1>
      <SynVerNumMES2>3</SynVerNumMES2>
      <MesSenMES3>NTA.GB</MesSenMES3>
      <MesRecMES6>MDTP-000000000000000000000000091-01</MesRecMES6>
      <DatOfPreMES9>20200914</DatOfPreMES9>
      <TimOfPreMES10>1432</TimOfPreMES10>
      <IntConRefMES11>66390912144854</IntConRefMES11>
      <AppRefMES14>NCTS</AppRefMES14>
      <TesIndMES18>0</TesIndMES18>
      <MesIdeMES19>66390912144854</MesIdeMES19>
      <MesTypMES20>GB043A</MesTypMES20>
      <HEAHEA>
        <DocNumHEA5>99IT9876AB88901209</DocNumHEA5>
        <TypOfDecHEA24>T1</TypOfDecHEA24>
        <CouOfDesCodHEA30>GB</CouOfDesCodHEA30>
        <CouOfDisCodHEA55>IT</CouOfDisCodHEA55>
        <IdeOfMeaOfTraAtDHEA78>abcd</IdeOfMeaOfTraAtDHEA78>
        <NatOfMeaOfTraAtDHEA80>IT</NatOfMeaOfTraAtDHEA80>
        <ConIndHEA96>0</ConIndHEA96>
        <AccDatHEA158>20190912</AccDatHEA158>
        <TotNumOfIteHEA305>1</TotNumOfIteHEA305>
        <TotNumOfPacHEA306>1</TotNumOfPacHEA306>
        <TotGroMasHEA307>1000</TotGroMasHEA307>
      </HEAHEA>
      <TRAPRIPC1>
        <NamPC17>Mancini Carriers</NamPC17>
        <StrAndNumPC122>90 Desio Way</StrAndNumPC122>
        <PosCodPC123>MOD 5JJ</PosCodPC123>
        <CitPC124>Modena</CitPC124>
        <CouPC125>IT</CouPC125>
        <TINPC159>IT444100201000</TINPC159>
      </TRAPRIPC1>
      <TRACONCO1>
        <NamCO17>Mancini Carriers</NamCO17>
        <StrAndNumCO122>90 Desio Way</StrAndNumCO122>
        <PosCodCO123>MOD 5JJ</PosCodCO123>
        <CitCO124>Modena</CitCO124>
        <CouCO125>IT</CouCO125>
        <TINCO159>IT444100201000</TINCO159>
      </TRACONCO1>
      <TRACONCE1>
        <NamCE17>Mancini Carriers</NamCE17>
        <StrAndNumCE122>90 Desio Way</StrAndNumCE122>
        <PosCodCE123>MOD 5JJ</PosCodCE123>
        <CitCE124>Modena</CitCE124>
        <CouCE125>IT</CouCE125>
        <TINCE159>IT444100201000</TINCE159>
      </TRACONCE1>
      <TRADESTRD>
        <NamTRD7>The Luggage Carriers</NamTRD7>
        <StrAndNumTRD22>225 Suedopolish Yard,</StrAndNumTRD22>
        <PosCodTRD23>SS8 2BB</PosCodTRD23>
        <CitTRD24>,</CitTRD24>
        <CouTRD25>GB</CouTRD25>
        <TINTRD59>GB163910077000</TINTRD59>
      </TRADESTRD>
      <CUSOFFDEPEPT>
        <RefNumEPT1>IT021100</RefNumEPT1>
      </CUSOFFDEPEPT>
      <CUSOFFPREOFFRES>
        <RefNumRES1>GB000060</RefNumRES1>
      </CUSOFFPREOFFRES>
      <GOOITEGDS>
        <IteNumGDS7>1</IteNumGDS7>
        <GooDesGDS23>Flowers</GooDesGDS23>
        <GroMasGDS46>1000</GroMasGDS46>
        <NetMasGDS48>999</NetMasGDS48>
        <CouOfDisGDS58>GB</CouOfDisGDS58>
        <CouOfDesGDS59>GB</CouOfDesGDS59>
        <PRODOCDC2>
          <DocTypDC21>18</DocTypDC21>
          <DocRefDC23>Ref.</DocRefDC23>
        </PRODOCDC2>
        <PACGS2>
          <MarNumOfPacGS21>Ref.</MarNumOfPacGS21>
          <KinOfPacGS23>BX</KinOfPacGS23>
          <NumOfPacGS24>1</NumOfPacGS24>
        </PACGS2>
        <SGICODSD2>
          <SenGooCodSD22>1</SenGooCodSD22>
          <SenQuaSD23>1</SenQuaSD23>
        </SGICODSD2>
      </GOOITEGDS>
    </CC043A>
  }

  private val arrivalId           = ArrivalId(1)
  private val postUri             = s"/transit-movements-trader-at-destination/movements/arrivals/${arrivalId.value}/messages/"
  private val summaryUri          = s"/transit-movements-trader-at-destination/movements/arrivals/${arrivalId.value}/messages/summary"
  private val rejectionUri        = s"/transit-movements-trader-at-destination/movements/arrivals/${arrivalId.value}/messages/1"
  private val unloadingRemarksUri = s"/transit-movements-trader-at-destination/movements/arrivals/${arrivalId.value}/messages/1"
  private val getPDFUrl           = s"/transit-movements-trader-at-destination/movements/arrivals/${arrivalId.value}/unloading-permission"

  private val unloadingPermissionUrl = s"/transit-movements-trader-at-destination/movements/arrivals/${arrivalId.value}/unloading-permission"

  val responseCodes: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)
}
