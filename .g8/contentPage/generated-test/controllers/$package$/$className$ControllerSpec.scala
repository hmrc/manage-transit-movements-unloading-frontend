package controllers.$package$

import base.{AppWithDefaultMockFixtures, SpecBase}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.$package$.$className$View

class $className$ControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private lazy val $className;format="decap"$Route = routes.$className$Controller.onPageLoad(lrn).url

  "$className$ Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, $className;format="decap"$Route)
      val result = route(app, request).value

      val view = injector.instanceOf[$className$View]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(lrn)(request, messages).toString
    }
  }
}
