
# manage-transit-movements-unloading-frontend

This service allows a user to report that transit movement goods have been unloaded.

Service manager port: 10123

## How to run this service locally

### Start service manager with the following command
    sm --start CTC_TRADERS_PRELODGE --appendArgs '{"MANAGE_TRANSIT_MOVEMENTS_UNLOADING_FRONTEND":["-Dmicroservice.services.arrivals-backend.port=9481", "-Dmicroservice.services.arrivals-backend.uri=/common-transit-convention-trader-at-destination"], "MANAGE_TRANSIT_MOVEMENTS_FRONTEND":["-Dmicroservice.services.destination.port=9481", "-Dmicroservice.services.destination.startUrl=common-transit-convention-trader-at-destination"], "MANAGE_TRANSIT_MOVEMENTS_ARRIVAL_FRONTEND":["-Dmicroservice.services.destination.port=9481","-Dmicroservice.services.destination.startUrl=common-transit-convention-trader-at-destination"]}' -r

### Change the port and start URL inside application.conf to
     arrivals-backend {
      host = "localhost"
      port = 9481
      protocol = "http"
      startUrl = "common-transit-convention-trader-at-destination"
    }
    

### Stop Unloading frontend and start this with sbt instead
    sm --start MANAGE_TRANSIT_MOVEMENTS_UNLOADING_FRONTEND

You now should be able to access [View arrivals]("http://localhost:9485/manage-transit-movements/view-arrivals")  
and access */manage-transit-movements/unloading* locally
    
### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

