#!/bin/bash
sbt run -Dconfig.resource=application.conf -Dmicroservice.services.arrivals-backend.port=9481 -Dmicroservice.services.arrivals-backend.startUrl=common-transit-convention-trader-at-destination
