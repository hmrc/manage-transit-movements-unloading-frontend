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

package services.P5

import base.SpecBase
import connectors.ArrivalMovementConnector
import generated._
import generators.Generators
import models.P5.ArrivalMessageType.{ArrivalNotification, RejectionFromOfficeOfDestination, UnloadingPermission}
import models.P5._
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.Node

class UnloadingPermissionMessageServiceSpec extends SpecBase with BeforeAndAfterEach with Generators {

  private val mockConnector = mock[ArrivalMovementConnector]

  private val service = new UnloadingPermissionMessageService(mockConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  private val unloadingPermission1 = MessageMetaData(LocalDateTime.now(), UnloadingPermission, "path1/url")
  private val unloadingPermission2 = MessageMetaData(LocalDateTime.now().minusDays(1), UnloadingPermission, "path2/url")
  private val unloadingPermission3 = MessageMetaData(LocalDateTime.now().minusDays(2), UnloadingPermission, "path3/url")
  private val arrivalNotification  = MessageMetaData(LocalDateTime.now(), ArrivalNotification, "path4/url")

  "UnloadingPermissionMessageService" - {

    "getUnloadingPermissionMessage" - {

      "must return latest unloading permission" in {

        val messageMetaData = Messages(List(unloadingPermission1, arrivalNotification, unloadingPermission3, unloadingPermission2))

        when(mockConnector.getMessageMetaData(arrivalId)).thenReturn(Future.successful(messageMetaData))

        service.getUnloadingPermissionMessage(arrivalId).futureValue mustBe Some(unloadingPermission1)
      }

      "must return none when there is no unloading permission" in {

        val messageMetaData = Messages(List(arrivalNotification))

        when(mockConnector.getMessageMetaData(arrivalId)).thenReturn(Future.successful(messageMetaData))

        service.getUnloadingPermissionMessage(arrivalId).futureValue mustBe None
      }
    }

    "getMessageHead" - {

      val rejectionFromOfficeOfDestination = MessageMetaData(LocalDateTime.now(), RejectionFromOfficeOfDestination, "path/url")
      val unloadingPermission2             = MessageMetaData(LocalDateTime.now().minusDays(1), UnloadingPermission, "path/url")
      val unloadingPermission1             = MessageMetaData(LocalDateTime.now().minusDays(3), UnloadingPermission, "path/url")
      val arrivalNotification              = MessageMetaData(LocalDateTime.now().minusDays(4), ArrivalNotification, "path/url")

      "must return latest message" in {

        val messageMetaData = Messages(List(rejectionFromOfficeOfDestination, arrivalNotification, unloadingPermission1, unloadingPermission2))

        when(mockConnector.getMessageMetaData(arrivalId)).thenReturn(Future.successful(messageMetaData))

        service.getMessageHead(arrivalId).futureValue mustBe Some(rejectionFromOfficeOfDestination)
      }
    }

    "getUnloadingPermissionXml" - {

      "must return latest unloading permission message" in {

        val messageMetaData = Messages(List(unloadingPermission1, arrivalNotification, unloadingPermission3, unloadingPermission2))

        val message: Node =
          <ncts:CC043C PhaseID="NCTS5.0" xmlns:ncts="http://ncts.dgtaxud.ec">
            <messageSender>token</messageSender>
            <messageRecipient>token</messageRecipient>
            <preparationDateAndTime>2007-10-26T07:36:28</preparationDateAndTime>
            <messageIdentification>token</messageIdentification>
            <messageType>CC043C</messageType>
            <correlationIdentifier>token</correlationIdentifier>
            <TransitOperation>
              <MRN>38VYQTYFU3T0KUTUM3</MRN>
              <declarationType>3</declarationType>
              <declarationAcceptanceDate>2014-06-09+01:00</declarationAcceptanceDate>
              <security>4</security>
              <reducedDatasetIndicator>1</reducedDatasetIndicator>
            </TransitOperation>
            <CustomsOfficeOfDestinationActual>
              <referenceNumber>GB000068</referenceNumber>
            </CustomsOfficeOfDestinationActual>
            <HolderOfTheTransitProcedure>
              <identificationNumber>Fzsisks</identificationNumber>
              <TIRHolderIdentificationNumber>trp-id-1</TIRHolderIdentificationNumber>
              <name>Jean Doe</name>
              <Address>
                <streetAndNumber>1 avenue marceau</streetAndNumber>
                <postcode>10006</postcode>
                <city>Paris</city>
                <country>FR</country>
              </Address>
            </HolderOfTheTransitProcedure>
            <TraderAtDestination>
              <identificationNumber>tad-1</identificationNumber>
            </TraderAtDestination>
            <CTLControl>
              <continueUnloading>9</continueUnloading>
            </CTLControl>
            <Consignment>
              <countryOfDestination>FR</countryOfDestination>
              <containerIndicator>1</containerIndicator>
              <inlandModeOfTransport>2</inlandModeOfTransport>
              <grossMass>1000.99</grossMass>
              <Consignor>
                <identificationNumber>id2</identificationNumber>
                <name>john doe</name>
                <Address>
                  <streetAndNumber>1 high street</streetAndNumber>
                  <postcode>N1 99Z</postcode>
                  <city>Newcastle</city>
                  <country>GB</country>
                </Address>
              </Consignor>
              <Consignee>
                <identificationNumber>csgnee-1</identificationNumber>
                <name>Jane Doe</name>
                <Address>
                  <streetAndNumber>1 Champs Elysees</streetAndNumber>
                  <postcode>75008</postcode>
                  <city>Paris</city>
                  <country>FR</country>
                </Address>
              </Consignee>
              <TransportEquipment>
                <sequenceNumber>1</sequenceNumber>
                <containerIdentificationNumber>cin-1</containerIdentificationNumber>
                <numberOfSeals>103</numberOfSeals>
                <Seal>
                  <sequenceNumber>1001</sequenceNumber>
                  <identifier>1002</identifier>
                </Seal>
                <GoodsReference>
                  <sequenceNumber>1</sequenceNumber>
                  <declarationGoodsItemNumber>108</declarationGoodsItemNumber>
                </GoodsReference>
              </TransportEquipment>
              <DepartureTransportMeans>
                <sequenceNumber>1</sequenceNumber>
                <typeOfIdentification>4</typeOfIdentification>
                <identificationNumber>28</identificationNumber>
                <nationality>DE</nationality>
              </DepartureTransportMeans>
              <PreviousDocument>
                <sequenceNumber>1</sequenceNumber>
                <type>9811</type>
                <referenceNumber>info1</referenceNumber>
                <complementOfInformation>8</complementOfInformation>
              </PreviousDocument>
              <SupportingDocument>
                <sequenceNumber>1</sequenceNumber>
                <type>4567</type>
                <referenceNumber>1234</referenceNumber>
                <complementOfInformation>2</complementOfInformation>
              </SupportingDocument>
              <TransportDocument>
                <sequenceNumber>1</sequenceNumber>
                <type>4</type>
                <referenceNumber>refn-1</referenceNumber>
              </TransportDocument>
              <AdditionalReference>
                <sequenceNumber>12</sequenceNumber>
                <type>4</type>
                <referenceNumber>addref-1</referenceNumber>
              </AdditionalReference>
              <AdditionalInformation>
                <sequenceNumber>2</sequenceNumber>
                <code>6</code>
                <text>additional ref text</text>
              </AdditionalInformation>
              <Incident>
                <sequenceNumber>23</sequenceNumber>
                <code>5</code>
                <text>some text 1</text>
                <Endorsement>
                  <date>2013-05-22+01:00</date>
                  <authority>de</authority>
                  <place>Cologne</place>
                  <country>DE</country>
                </Endorsement>
                <Location>
                  <qualifierOfIdentification>loc1</qualifierOfIdentification>
                  <UNLocode>34</UNLocode>
                  <country>DE</country>
                  <GNSS>
                    <latitude>91.0</latitude>
                    <longitude>92.0</longitude>
                  </GNSS>
                  <Address>
                    <streetAndNumber>2 high street</streetAndNumber>
                    <postcode>ab12 34c</postcode>
                    <city>city2</city>
                  </Address>
                </Location>
                <TransportEquipment>
                  <sequenceNumber>1</sequenceNumber>
                  <containerIdentificationNumber>tn1</containerIdentificationNumber>
                  <numberOfSeals>34</numberOfSeals>
                  <Seal>
                    <sequenceNumber>7</sequenceNumber>
                    <identifier>sl7</identifier>
                  </Seal>
                  <GoodsReference>
                    <sequenceNumber>5</sequenceNumber>
                    <declarationGoodsItemNumber>78</declarationGoodsItemNumber>
                  </GoodsReference>
                </TransportEquipment>
                <Transhipment>
                  <containerIndicator>1</containerIndicator>
                  <TransportMeans>
                    <typeOfIdentification>5</typeOfIdentification>
                    <identificationNumber>44</identificationNumber>
                    <nationality>FR</nationality>
                  </TransportMeans>
                </Transhipment>
              </Incident>
              <HouseConsignment>
                <sequenceNumber>1</sequenceNumber>
                <grossMass>1234.567</grossMass>
                <securityIndicatorFromExportDeclaration>si1</securityIndicatorFromExportDeclaration>
                <Consignor>
                  <identificationNumber>csgr1</identificationNumber>
                  <name>michael doe</name>
                  <Address>
                    <streetAndNumber>3 main street</streetAndNumber>
                    <postcode>bc2 45d</postcode>
                    <city>city4</city>
                    <country>FR</country>
                  </Address>
                </Consignor>
                <Consignee>
                  <identificationNumber>csgee1</identificationNumber>
                  <name>John Smith</name>
                  <Address>
                    <streetAndNumber>5 main street</streetAndNumber>
                    <postcode>cd4 56e</postcode>
                    <city>city5</city>
                    <country>DE</country>
                  </Address>
                </Consignee>
                <DepartureTransportMeans>
                  <sequenceNumber>56</sequenceNumber>
                  <typeOfIdentification>2</typeOfIdentification>
                  <identificationNumber>23</identificationNumber>
                  <nationality>IT</nationality>
                </DepartureTransportMeans>
                <PreviousDocument>
                  <sequenceNumber>67</sequenceNumber>
                  <type>3</type>
                  <referenceNumber>4</referenceNumber>
                  <complementOfInformation>1</complementOfInformation>
                </PreviousDocument>
                <SupportingDocument>
                  <sequenceNumber>3</sequenceNumber>
                  <type>7</type>
                  <referenceNumber>ref4</referenceNumber>
                  <complementOfInformation>6</complementOfInformation>
                </SupportingDocument>
                <TransportDocument>
                  <sequenceNumber>7</sequenceNumber>
                  <type>8</type>
                  <referenceNumber>9</referenceNumber>
                </TransportDocument>
                <AdditionalReference>
                  <sequenceNumber>3</sequenceNumber>
                  <type>5</type>
                  <referenceNumber>4</referenceNumber>
                </AdditionalReference>
                <AdditionalInformation>
                  <sequenceNumber>6</sequenceNumber>
                  <code>7</code>
                  <text>8</text>
                </AdditionalInformation>
                <ConsignmentItem>
                  <goodsItemNumber>6</goodsItemNumber>
                  <declarationGoodsItemNumber>100</declarationGoodsItemNumber>
                  <declarationType>2</declarationType>
                  <countryOfDestination>DE</countryOfDestination>
                  <Consignee>
                    <identificationNumber>5</identificationNumber>
                    <name>Smith</name>
                    <Address>
                      <streetAndNumber>5 main street</streetAndNumber>
                      <postcode>ab12 3cd</postcode>
                      <city>Newcastle</city>
                      <country>GB</country>
                    </Address>
                  </Consignee>
                  <Commodity>
                    <descriptionOfGoods>shirts</descriptionOfGoods>
                    <cusCode>1</cusCode>
                    <CommodityCode>
                      <harmonizedSystemSubHeadingCode>3</harmonizedSystemSubHeadingCode>
                      <combinedNomenclatureCode>45</combinedNomenclatureCode>
                    </CommodityCode>
                    <DangerousGoods>
                      <sequenceNumber>5</sequenceNumber>
                      <UNNumber>1</UNNumber>
                    </DangerousGoods>
                    <GoodsMeasure>
                      <grossMass>123.45</grossMass>
                      <netMass>123.45</netMass>
                    </GoodsMeasure>
                  </Commodity>
                  <Packaging>
                    <sequenceNumber>5</sequenceNumber>
                    <typeOfPackages>3</typeOfPackages>
                    <numberOfPackages>99</numberOfPackages>
                    <shippingMarks>xyz</shippingMarks>
                  </Packaging>
                  <PreviousDocument>
                    <sequenceNumber>2</sequenceNumber>
                    <type>3</type>
                    <referenceNumber>4</referenceNumber>
                    <goodsItemNumber>5</goodsItemNumber>
                    <complementOfInformation>1</complementOfInformation>
                  </PreviousDocument>
                  <SupportingDocument>
                    <sequenceNumber>6</sequenceNumber>
                    <type>7</type>
                    <referenceNumber>8</referenceNumber>
                    <complementOfInformation>0</complementOfInformation>
                  </SupportingDocument>
                  <TransportDocument>
                    <sequenceNumber>3</sequenceNumber>
                    <type>2</type>
                    <referenceNumber>4</referenceNumber>
                  </TransportDocument>
                  <AdditionalReference>
                    <sequenceNumber>4</sequenceNumber>
                    <type>3</type>
                    <referenceNumber>1</referenceNumber>
                  </AdditionalReference>
                  <AdditionalInformation>
                    <sequenceNumber>7</sequenceNumber>
                    <code>6</code>
                    <text>additional info text</text>
                  </AdditionalInformation>
                </ConsignmentItem>
              </HouseConsignment>
            </Consignment>
          </ncts:CC043C>

        when(mockConnector.getMessageMetaData(arrivalId)).thenReturn(Future.successful(messageMetaData))
        when(mockConnector.getUnloadingPermissionXml(arrivalId, unloadingPermission1.id)).thenReturn(Future.successful(message))

        service.getUnloadingPermissionXml(arrivalId).futureValue.value mustBe a[CC043CType]
      }

      "must return none when there is no unloading permission message" in {

        val messageMetaData = Messages(List(arrivalNotification))

        when(mockConnector.getMessageMetaData(arrivalId)).thenReturn(Future.successful(messageMetaData))

        service.getUnloadingPermissionXml(arrivalId).futureValue mustBe None
      }
    }
  }
}
