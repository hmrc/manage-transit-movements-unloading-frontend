package controllers.$package$

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.$formProvider$
import generators.Generators
import models.reference.Country
import models.{DynamicAddress, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.$package$._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.CountriesService
import views.html.$package$.$className$View

import scala.concurrent.Future

class $className$ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val addressHolderName = Gen.alphaNumStr.sample.value

  private val testAddress = arbitrary[Address].sample.value
  private val country     = arbitrary[Country].sample.value

  private val formProvider                        = new $formProvider$()
  private def form(isPostalCodeRequired: Boolean) = formProvider("$package$.$className;format="decap"$", isPostalCodeRequired, addressHolderName)

  private val mode                                 = NormalMode
  private lazy val $className;format="decap"$Route = routes.$className$Controller.onPageLoad(lrn, mode).url

  private lazy val mockCountriesService: CountriesService = mock[CountriesService]

  override def beforeEach(): Unit = {
    reset(mockCountriesService)
    super.beforeEach()
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[$navRoute$NavigatorProvider]).toInstance(fake$navRoute$NavigatorProvider))
      .overrides(bind(classOf[CountriesService]).toInstance(mockCountriesService))

  "$className$ Controller" - {

    "must return OK and the correct view for a GET" - {

      "when postcode is required" in {

        val isPostalCodeRequired = true

        when(mockCountriesService.doesCountryRequireZip(any())(any())).thenReturn(Future.successful(isPostalCodeRequired))

        val userAnswers = emptyUserAnswers
          .setValue(NamePage, addressHolderName)
          .setValue(CountryPage, country)
        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, $className;format="decap"$Route)
        val result = route(app, request).value

        val view = injector.instanceOf[$className$View]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(isPostalCodeRequired), lrn, mode, addressHolderName, isPostalCodeRequired)(request, messages).toString
      }

      "when postcode is optional" in {

        val isPostalCodeRequired = false

        when(mockCountriesService.doesCountryRequireZip(any())(any())).thenReturn(Future.successful(isPostalCodeRequired))

        val userAnswers = emptyUserAnswers
          .setValue(NamePage, addressHolderName)
          .setValue(CountryPage, country)
        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, $className;format="decap"$Route)
        val result = route(app, request).value

        val view = injector.instanceOf[$className$View]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(isPostalCodeRequired), lrn, mode, addressHolderName, isPostalCodeRequired)(request, messages).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" - {

      "when postcode is required" in {

        val isPostalCodeRequired = true
        val testAddress          = arbitrary[DynamicAddress](arbitraryDynamicAddressWithRequiredPostalCode).sample.value

        when(mockCountriesService.doesCountryRequireZip(any())(any())).thenReturn(Future.successful(isPostalCodeRequired))

        val userAnswers = emptyUserAnswers
          .setValue(NamePage, addressHolderName)
          .setValue(CountryPage, country)
          .setValue($className$Page, testAddress)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, $className;format = "decap" $Route)

        val result = route(app, request).value

        val filledForm = form(isPostalCodeRequired).bind(
          Map(
            "numberAndStreet" -> testAddress.numberAndStreet,
            "city"            -> testAddress.city,
            "postalCode"      -> testAddress.postalCode.get
          )
        )

        val view = injector.instanceOf[$className$View]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, lrn, mode, addressHolderName, isPostalCodeRequired)(request, messages).toString
      }

      "when postcode is optional" in {

        val isPostalCodeRequired = false
        val testAddress          = arbitrary[DynamicAddress](arbitraryDynamicAddressWithRequiredPostalCode).sample.value

        when(mockCountriesService.doesCountryRequireZip(any())(any())).thenReturn(Future.successful(isPostalCodeRequired))

        val userAnswers = emptyUserAnswers
          .setValue(NamePage, addressHolderName)
          .setValue(CountryPage, country)
          .setValue($className$Page, testAddress)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, $className;format = "decap" $Route)

        val result = route(app, request).value

        val filledForm = form(isPostalCodeRequired).bind(
          Map(
            "numberAndStreet" -> testAddress.numberAndStreet,
            "city"            -> testAddress.city,
            "postalCode"      -> testAddress.postalCode.getOrElse("")
          )
        )

        val view = injector.instanceOf[$className$View]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, lrn, mode, addressHolderName, isPostalCodeRequired)(request, messages).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockCountriesService.doesCountryRequireZip(any())(any())).thenReturn(Future.successful(false))
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .setValue(NamePage, addressHolderName)
        .setValue(CountryPage, country)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, $className;format="decap"$Route)
        .withFormUrlEncodedBody(
          ("numberAndStreet", testAddress.numberAndStreet),
          ("city", testAddress.city),
          ("postalCode", testAddress.postalCode.getOrElse(""))
        )

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" - {

      "when postcode is required" in {

        val isPostalCodeRequired = true

        when(mockCountriesService.doesCountryRequireZip(any())(any())).thenReturn(Future.successful(isPostalCodeRequired))

        val userAnswers = emptyUserAnswers
          .setValue(NamePage, addressHolderName)
          .setValue(CountryPage, country)
        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, $className;format = "decap" $Route).withFormUrlEncodedBody(("value", ""))
        val boundForm = form(isPostalCodeRequired).bind(Map("value" -> ""))

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST

        val view = injector.instanceOf[$className$View]

        contentAsString(result) mustEqual
          view(boundForm, lrn, mode, addressHolderName, isPostalCodeRequired)(request, messages).toString
      }

      "when postcode is optional" in {

        val isPostalCodeRequired = false

        when(mockCountriesService.doesCountryRequireZip(any())(any())).thenReturn(Future.successful(isPostalCodeRequired))

        val userAnswers = emptyUserAnswers
          .setValue(NamePage, addressHolderName)
          .setValue(CountryPage, country)
        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, $className;format = "decap" $Route).withFormUrlEncodedBody(("value", ""))
        val boundForm = form(isPostalCodeRequired).bind(Map("value" -> ""))

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST

        val view = injector.instanceOf[$className$View]

        contentAsString(result) mustEqual
          view(boundForm, lrn, mode, addressHolderName, isPostalCodeRequired)(request, messages).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, $className;format="decap"$Route)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, $className;format="decap"$Route)
        .withFormUrlEncodedBody(
          ("numberAndStreet", testAddress.numberAndStreet),
          ("city", testAddress.city),
          ("postalCode", testAddress.postalCode.getOrElse(""))
        )

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
