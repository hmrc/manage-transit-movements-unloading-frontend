import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-play-28"             % "0.64.0",
    "com.typesafe.play"    %% "play-iteratees"                 % "2.6.1",
    "uk.gov.hmrc"          %% "logback-json-logger"            % "5.2.0",
    "uk.gov.hmrc"          %% "play-conditional-form-mapping"  % "1.11.0-play-28",
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-28"     % "5.24.0",
    "uk.gov.hmrc"          %% "play-allowlist-filter"          % "1.1.0",
    "com.lucidchart"       %% "xtract"                         % "2.3.0-alpha3",
    "uk.gov.hmrc"          %% "play-frontend-hmrc"             % "3.21.0-play-28"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-test-play-28"  % "0.64.0",
    "org.scalatest"              %% "scalatest"                % "3.2.12",
    "org.scalatestplus"          %% "mockito-3-2"              % "3.1.2.0",
    "org.scalatestplus.play"     %% "scalatestplus-play"       % "5.1.0",
    "org.scalatestplus"          %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    "org.pegdown"                 % "pegdown"                  % "1.6.0",
    "org.jsoup"                   % "jsoup"                    % "1.15.1",
    "com.typesafe.play"          %% "play-test"                % PlayVersion.current,
    "org.mockito"                 % "mockito-core"             % "4.5.1",
    "org.scalacheck"             %% "scalacheck"               % "1.16.0",
    "wolfendale"                 %% "scalacheck-gen-regexp"    % "0.1.1",
    "com.github.tomakehurst"      % "wiremock-standalone"      % "2.27.2",
    "com.vladsch.flexmark"        % "flexmark-all"             % "0.62.2"
    ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
