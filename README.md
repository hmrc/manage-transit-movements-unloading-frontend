
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

To toggle between the Phase 5 transition and post-transition modes we have defined two separate modules, each with their own set of class bindings to handle the rules associated with these two periods.

#### Transition
<pre>
sm2 --start CTC_TRADERS_P5_ACCEPTANCE_TRANSITION
sm2 --stop MANAGE_TRANSIT_MOVEMENTS_UNLOADING_FRONTEND_TRANSITION
sbt -Dplay.additional.module=config.TransitionModule run
</pre>

#### Final
Phase 5 final introduces multiple house consignments. This provides users with functionalities that are not available during the transition period, like the ability to add and remove entire house consignments.
<pre>
sm2 --start CTC_TRADERS_P5_ACCEPTANCE
sm2 --stop MANAGE_TRANSIT_MOVEMENTS_UNLOADING_FRONTEND
sbt -Dplay.additional.module=config.PostTransitionModule run
</pre>

We then need to post an IE007 (Arrival Notification) followed by a IE043 (Unloading Permission). From the `/view-arrival-notifications` page click the `Make unloading remarks` link for the relevant movement.
### Feature toggles

The following features can be toggled in [application.conf](conf/application.conf):

| Key                        | Argument type | sbt                                                            | Description                                                                                                                                                                                    |
|----------------------------|---------------|----------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `trader-test.enabled`      | `Boolean`     | `sbt -Dtrader-test.enabled=true run`                           | If enabled, this will override the behaviour of the "Is this page not working properly?" and "feedback" links. This is so we can receive feedback in the absence of Deskpro in `externaltest`. |
| `banners.showUserResearch` | `Boolean`     | `sbt -Dbanners.showUserResearch=true run`                      | Controls whether or not we show the user research banner.                                                                                                                                      |
| `play.additional.module`   | `String`      | `sbt -Dplay.additional.module=config.PostTransitionModule run` | Controls which module (TransitionModule or PostTransitionModule) we bind to the application at start-up.                                                                                       |
| `play.http.router`         | `String`      | `sbt -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes run`  | Controls which router is used for the application, either `prod.Routes` or `testOnlyDoNotUseInAppConf.Routes`                                                                                  |

### Navigation

Navigation is handled using the default HMRC style with a Navigator class defining normal and check routes

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
