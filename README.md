
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
  
### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

