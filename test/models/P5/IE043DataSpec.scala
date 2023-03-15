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

package models.P5

import base.SpecBase
import models.MovementReferenceNumber
import play.api.libs.json.Json

class IE043DataSpec extends SpecBase {

  "IE043Data" - {

    "must deserialize" in {

      val json = Json.parse(
        """
          |{
          |    "_links": {
          |        "self": {
          |            "href": "/customs/transits/movements/arrivals/6409d19c5d06a08a/messages/6409d1d572bc5aba"
          |        },
          |        "arrival": {
          |            "href": "/customs/transits/movements/arrivals/6409d19c5d06a08a"
          |        }
          |    },
          |    "id": "6409d1d572bc5aba",
          |    "arrivalId": "6409d19c5d06a08a",
          |    "received": "2023-03-09T12:32:21.38Z",
          |    "type": "IE043",
          |    "status": "Received",
          |    "body": {
          |        "n1:CC043C": {
          |            "TransitOperation": {
          |                "MRN": "38VYQTYFU3T0KUTUM3",
          |                "declarationType": "3",
          |                "declarationAcceptanceDate": "2014-06-09+01:00",
          |                "security": "4",
          |                "reducedDatasetIndicator": "1"
          |            },
          |            "preparationDateAndTime": "2007-10-26T07:36:28",
          |            "messageSender": "token",
          |            "Consignment": {
          |                "countryOfDestination": "FR",
          |                "containerIndicator": "1",
          |                "inlandModeOfTransport": "2",
          |                "grossMass": 1000.99,
          |                "Consignor": {
          |                    "identificationNumber": "id2",
          |                    "name": "john doe",
          |                    "Address": {
          |                        "streetAndNumber": "1 high street",
          |                        "postcode": "N1 99Z",
          |                        "city": "Newcastle",
          |                        "country": "GB"
          |                    }
          |                },
          |                "Consignee": {
          |                    "identificationNumber": "csgnee-1",
          |                    "name": "Jane Doe",
          |                    "Address": {
          |                        "streetAndNumber": "1 Champs Elysees",
          |                        "postcode": "75008",
          |                        "city": "Paris",
          |                        "country": "FR"
          |                    }
          |                },
          |                "TransportEquipment": [
          |                    {
          |                        "sequenceNumber": "te1",
          |                        "containerIdentificationNumber": "cin-1",
          |                        "numberOfSeals": 103,
          |                        "Seal": [
          |                            {
          |                                "sequenceNumber": "1001",
          |                                "identifier": "1002"
          |                            }
          |                        ],
          |                        "GoodsReference": [
          |                            {
          |                                "sequenceNumber": "gref-1",
          |                                "declarationGoodsItemNumber": 108
          |                            }
          |                        ]
          |                    }
          |                ],
          |                "DepartureTransportMeans": [
          |                    {
          |                        "sequenceNumber": "dtm-1",
          |                        "typeOfIdentification": "4",
          |                        "identificationNumber": "28",
          |                        "nationality": "DE"
          |                    }
          |                ],
          |                "PreviousDocument": [
          |                    {
          |                        "sequenceNumber": "pr-1",
          |                        "referenceNumber": "info1",
          |                        "complementOfInformation": "8",
          |                        "type": "9811"
          |                    }
          |                ],
          |                "SupportingDocument": [
          |                    {
          |                        "sequenceNumber": "sd-1",
          |                        "referenceNumber": "1234",
          |                        "complementOfInformation": "2",
          |                        "type": "4567"
          |                    }
          |                ],
          |                "TransportDocument": [
          |                    {
          |                        "sequenceNumber": "td-1",
          |                        "referenceNumber": "refn-1",
          |                        "type": "4"
          |                    }
          |                ],
          |                "AdditionalReference": [
          |                    {
          |                        "sequenceNumber": "12",
          |                        "referenceNumber": "addref-1",
          |                        "type": "4"
          |                    }
          |                ],
          |                "AdditionalInformation": [
          |                    {
          |                        "sequenceNumber": "adref-2",
          |                        "code": "6",
          |                        "text": "additional ref text"
          |                    }
          |                ],
          |                "Incident": [
          |                    {
          |                        "sequenceNumber": "23",
          |                        "code": "5",
          |                        "text": "some text 1",
          |                        "Endorsement": {
          |                            "date": "2013-05-22+01:00",
          |                            "authority": "de",
          |                            "place": "Cologne",
          |                            "country": "DE"
          |                        },
          |                        "Location": {
          |                            "qualifierOfIdentification": "loc1",
          |                            "UNLocode": "34",
          |                            "country": "DE",
          |                            "GNSS": {
          |                                "latitude": "91.0",
          |                                "longitude": "92.0"
          |                            },
          |                            "Address": {
          |                                "streetAndNumber": "2 high street",
          |                                "postcode": "ab12 34c",
          |                                "city": "city2"
          |                            }
          |                        },
          |                        "TransportEquipment": [
          |                            {
          |                                "sequenceNumber": "te1",
          |                                "containerIdentificationNumber": "tn1",
          |                                "numberOfSeals": 34,
          |                                "Seal": [
          |                                    {
          |                                        "sequenceNumber": "7",
          |                                        "identifier": "sl7"
          |                                    }
          |                                ],
          |                                "GoodsReference": [
          |                                    {
          |                                        "sequenceNumber": "gref-5",
          |                                        "declarationGoodsItemNumber": 78
          |                                    }
          |                                ]
          |                            }
          |                        ],
          |                        "Transhipment": {
          |                            "containerIndicator": "1",
          |                            "TransportMeans": {
          |                                "typeOfIdentification": "5",
          |                                "identificationNumber": "44",
          |                                "nationality": "FR"
          |                            }
          |                        }
          |                    }
          |                ],
          |                "HouseConsignment": [
          |                    {
          |                        "sequenceNumber": "hc1",
          |                        "grossMass": 1234.567,
          |                        "securityIndicatorFromExportDeclaration": "si1",
          |                        "Consignor": {
          |                            "identificationNumber": "csgr1",
          |                            "name": "michael doe",
          |                            "Address": {
          |                                "streetAndNumber": "3 main street",
          |                                "postcode": "bc2 45d",
          |                                "city": "city4",
          |                                "country": "FR"
          |                            }
          |                        },
          |                        "Consignee": {
          |                            "identificationNumber": "csgee1",
          |                            "name": "John Smith",
          |                            "Address": {
          |                                "streetAndNumber": "5 main street",
          |                                "postcode": "cd4 56e",
          |                                "city": "city5",
          |                                "country": "DE"
          |                            }
          |                        },
          |                        "DepartureTransportMeans": [
          |                            {
          |                                "sequenceNumber": "56",
          |                                "typeOfIdentification": "2",
          |                                "identificationNumber": "23",
          |                                "nationality": "IT"
          |                            }
          |                        ],
          |                        "PreviousDocument": [
          |                            {
          |                                "sequenceNumber": "67",
          |                                "referenceNumber": "4",
          |                                "complementOfInformation": "1",
          |                                "type": "3"
          |                            }
          |                        ],
          |                        "SupportingDocument": [
          |                            {
          |                                "sequenceNumber": "3",
          |                                "referenceNumber": "ref4",
          |                                "complementOfInformation": "6",
          |                                "type": "7"
          |                            }
          |                        ],
          |                        "TransportDocument": [
          |                            {
          |                                "sequenceNumber": "7",
          |                                "referenceNumber": "9",
          |                                "type": "8"
          |                            }
          |                        ],
          |                        "AdditionalReference": [
          |                            {
          |                                "sequenceNumber": "3",
          |                                "referenceNumber": "4",
          |                                "type": "5"
          |                            }
          |                        ],
          |                        "AdditionalInformation": [
          |                            {
          |                                "sequenceNumber": "6",
          |                                "code": "7",
          |                                "text": "8"
          |                            }
          |                        ],
          |                        "ConsignmentItem": [
          |                            {
          |                                "goodsItemNumber": "6",
          |                                "declarationGoodsItemNumber": 100,
          |                                "declarationType": "2",
          |                                "countryOfDestination": "DE",
          |                                "Consignee": {
          |                                    "identificationNumber": "5",
          |                                    "name": "Smith",
          |                                    "Address": {
          |                                        "streetAndNumber": "5 main street",
          |                                        "postcode": "ab12 3cd",
          |                                        "city": "Newcastle",
          |                                        "country": "GB"
          |                                    }
          |                                },
          |                                "Commodity": {
          |                                    "descriptionOfGoods": "shirts",
          |                                    "cusCode": "1",
          |                                    "CommodityCode": {
          |                                        "harmonizedSystemSubHeadingCode": "3",
          |                                        "combinedNomenclatureCode": "45"
          |                                    },
          |                                    "DangerousGoods": [
          |                                        {
          |                                            "sequenceNumber": "5",
          |                                            "UNNumber": "1"
          |                                        }
          |                                    ],
          |                                    "GoodsMeasure": {
          |                                        "grossMass": 123.45,
          |                                        "netMass": 123.45
          |                                    }
          |                                },
          |                                "Packaging": [
          |                                    {
          |                                        "sequenceNumber": "5",
          |                                        "typeOfPackages": "3",
          |                                        "numberOfPackages": 99,
          |                                        "shippingMarks": "xyz"
          |                                    }
          |                                ],
          |                                "PreviousDocument": [
          |                                    {
          |                                        "sequenceNumber": "2",
          |                                        "referenceNumber": "4",
          |                                        "goodsItemNumber": "5",
          |                                        "complementOfInformation": "1",
          |                                        "type": "3"
          |                                    }
          |                                ],
          |                                "SupportingDocument": [
          |                                    {
          |                                        "sequenceNumber": "6",
          |                                        "referenceNumber": "8",
          |                                        "complementOfInformation": "0",
          |                                        "type": "7"
          |                                    }
          |                                ],
          |                                "TransportDocument": [
          |                                    {
          |                                        "sequenceNumber": "3",
          |                                        "referenceNumber": "4",
          |                                        "type": "2"
          |                                    }
          |                                ],
          |                                "AdditionalReference": [
          |                                    {
          |                                        "sequenceNumber": "4",
          |                                        "referenceNumber": "1",
          |                                        "type": "3"
          |                                    }
          |                                ],
          |                                "AdditionalInformation": [
          |                                    {
          |                                        "sequenceNumber": "7",
          |                                        "code": "6",
          |                                        "text": "additional info text"
          |                                    }
          |                                ]
          |                            }
          |                        ]
          |                    }
          |                ]
          |            },
          |            "TraderAtDestination": {
          |                "identificationNumber": "tad-1"
          |            },
          |            "messageType": "CC043C",
          |            "CTLControl": {
          |                "continueUnloading": 9
          |            },
          |            "@PhaseID": "NCTS5.0",
          |            "correlationIdentifier": "token",
          |            "messageRecipient": "token",
          |            "HolderOfTheTransitProcedure": {
          |                "identificationNumber": "Fzsisks",
          |                "TIRHolderIdentificationNumber": "trp-id-1",
          |                "name": "Jean Doe",
          |                "Address": {
          |                    "streetAndNumber": "1 avenue marceau",
          |                    "postcode": "10006",
          |                    "city": "Paris",
          |                    "country": "FR"
          |                }
          |            },
          |            "messageIdentification": "token",
          |            "CustomsOfficeOfDestinationActual": {
          |                "referenceNumber": "GB000008"
          |            }
          |        }
          |    }
          |}
          |""".stripMargin
      )

      val expectedResult: IE043Data = IE043Data(
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

      json.validate[IE043Data].asOpt.value mustBe expectedResult
    }
  }

}
