
# manage-transit-movements-unloading-frontend

This service allows a user to report that transit movement goods have been unloaded.

Service manager port: 10123

### Testing

Run unit tests:
<pre>sbt test</pre>
Run integration tests:
<pre>sbt it/test</pre>
Run accessibility linter tests:
<pre>sbt A11y/test</pre>

### Running manually or for journey tests

<pre>
sm2 --start CTC_TRADERS_P5_ACCEPTANCE
sm2 --stop MANAGE_TRANSIT_MOVEMENTS_UNLOADING_FRONTEND
sbt run
</pre>

We then need to post an IE007 (Arrival Notification) followed by a IE043 (Unloading Permission). From the `/view-arrival-notifications` page click the `Make unloading remarks` link for the relevant movement.
### Feature toggles

The following features can be toggled in [application.conf](conf/application.conf):

| Key                             | Argument type | sbt                                                           | Description                                                                                                                                                                                    |
|---------------------------------|---------------|---------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `feature-flags.phase-6-enabled` | `Boolean`     | `sbt -Dfeature-flags.phase-6-enabled=true run`                | If enabled, this will enable phase 6 functionality, including pages that aren't otherwise reachable. It will also trigger customs-reference-data to retrieve reference data from crdl-cache.   |
| `trader-test.enabled`           | `Boolean`     | `sbt -Dtrader-test.enabled=true run`                          | If enabled, this will override the behaviour of the "Is this page not working properly?" and "feedback" links. This is so we can receive feedback in the absence of Deskpro in `externaltest`. |
| `banners.showUserResearch`      | `Boolean`     | `sbt -Dbanners.showUserResearch=true run`                     | Controls whether or not we show the user research banner.                                                                                                                                      |
| `play.http.router`              | `String`      | `sbt -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes run` | Controls which router is used for the application, either `prod.Routes` or `testOnlyDoNotUseInAppConf.Routes`                                                                                  |

### Navigation

Navigation is handled using multiple modes to facilitate additions, amendments, and removals at the various data levels.

The two modes are `NormalMode` and `CheckMode`. `CheckMode` is indicative of something being *changed*, and can be seen by the presence of `change` in the URL.

Examples:
1. Changing an additional reference in a consignment item:
   
   I am *changing* a house consignment by *changing* a consignment item by *changing* an additional reference
   
   So my URL would follow the structure of: `.../change-house-consignment/:houseConsignmentIndex/change-item/:itemIndex/change-additional-reference/:additionalReferenceIndex/...`
2. Adding an additional reference to a consignment item:
   
   I am *changing* a house consignment by *changing* a consignment item by *adding* an additional reference
   
   So my URL would follow the structure of: `.../change-house-consignment/:houseConsignmentIndex/change-item/:itemIndex/additional-reference/:additionalReferenceIndex/...`
3. Adding an additional reference while adding a consignment item:
   
   I am *changing* a house consignment by *adding* a consignment item and *adding* an additional reference
   
   So my URL would follow the structure of: `.../change-house-consignment/:houseConsignmentIndex/item/:itemIndex/additional-reference/:additionalReferenceIndex/...`
4. Adding an additional reference while adding a consignment item while adding a house consignment:
   
   I am *adding* a house consignment and *adding* a consignment item and *adding* an additional reference
   
   So my URL would follow the structure of: `.../house-consignment/:houseConsignmentIndex/item/:itemIndex/additional-reference/:additionalReferenceIndex/...`
5. Removing an additional reference from a consignment item:
   
   I am *changing* a house consignment by *changing* a consignment item by *removing* an additional reference
   
   So my URL would follow the structure of: `.../change-house-consignment/:houseConsignmentIndex/change-item/:itemIndex/additional-reference/:additionalReferenceIndex/remove`

These modes help us navigate the user to the relevant 'check your answers' page after they have made their various additions, amendments, or removals.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
