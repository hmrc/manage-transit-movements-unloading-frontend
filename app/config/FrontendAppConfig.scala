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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  lazy val appName: String = configuration.get[String]("appName")

  lazy val contactHost: String = configuration.get[String]("contact-frontend.host")

  val contactFormServiceIdentifier: String = "CTCTraders"

  val trackingConsentUrl: String = configuration.get[String]("microservice.services.tracking-consent-frontend.url")
  val gtmContainer: String       = configuration.get[String]("microservice.services.tracking-consent-frontend.gtm.container")

  val showPhaseBanner: Boolean        = configuration.get[Boolean]("banners.showPhase")
  val userResearchUrl: String         = configuration.get[String]("urls.userResearch")
  val showUserResearchBanner: Boolean = configuration.get[Boolean]("banners.showUserResearch")
  lazy val nctsHelpdeskUrl: String    = configuration.get[String]("urls.nctsHelpdesk")

  private val host: String = configuration.get[String]("host")

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"

  val signOutUrl: String = configuration.get[String]("urls.logoutContinue") + configuration.get[String]("urls.feedback")

  lazy val loginUrl: String         = configuration.get[String]("microservice.services.auth.login")
  lazy val loginContinueUrl: String = configuration.get[String]("microservice.services.auth.loginContinue")

  lazy val legacyEnrolmentKey: String           = configuration.get[String]("microservice.services.auth.legacy.enrolmentKey")
  lazy val legacyEnrolmentIdentifierKey: String = configuration.get[String]("microservice.services.auth.legacy.enrolmentIdentifierKey")

  lazy val newEnrolmentKey: String           = configuration.get[String]("microservice.services.auth.enrolmentKey")
  lazy val newEnrolmentIdentifierKey: String = configuration.get[String]("microservice.services.auth.enrolmentIdentifierKey")

  lazy val eccEnrolmentSplashPage: String = configuration.get[String]("urls.eccEnrolmentSplashPage")

  lazy val enrolmentProxyUrl: String = configuration.get[Service]("microservice.services.enrolment-store-proxy").fullServiceUrl

  lazy val loginHmrcServiceUrl: String = configuration.get[String]("urls.loginHmrcService")

  lazy val nctsEnquiriesUrl: String = configuration.get[String]("urls.nctsEnquiries")
  lazy val timeoutSeconds: Int      = configuration.get[Int]("session.timeoutSeconds")
  lazy val countdownSeconds: Int    = configuration.get[Int]("session.countdownSeconds")

  private val manageTransitMovementsHost     = configuration.get[String]("manage-transit-movements-frontend.host")
  lazy val manageTransitMovementsUrl: String = s"$manageTransitMovementsHost/manage-transit-movements"
  lazy val serviceUrl: String                = s"$manageTransitMovementsUrl/what-do-you-want-to-do"
  lazy val viewArrivals: String              = s"$manageTransitMovementsUrl/view-arrivals"

  lazy val referenceDataUrl: String = configuration.get[Service]("microservice.services.reference-data").fullServiceUrl

  lazy val arrivalsBackend: String         = configuration.get[Service]("microservice.services.arrivals-backend").fullServiceUrl
  lazy val arrivalsBackendBaseUrl: String  = configuration.get[Service]("microservice.services.arrivals-backend").baseUrl
  lazy val arrivalNotificationsUrl: String = configuration.get[String]("urls.arrivalNotifications")

  lazy val languageTranslationEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.welsh-translation")

  lazy val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLiveInSeconds")
}
