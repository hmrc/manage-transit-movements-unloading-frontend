import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "org.reactivemongo"    %% "play2-reactivemongo"            % "0.20.13-play28",
    "org.reactivemongo"    %% "reactivemongo-play-json-compat" % "0.20.13-play28",
    "com.typesafe.play"    %% "play-iteratees"                 % "2.6.1",
    "uk.gov.hmrc"          %% "logback-json-logger"            % "5.1.0",
    "uk.gov.hmrc"          %% "play-conditional-form-mapping"  % "1.10.0-play-28",
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-28"     % "5.19.0",
    "uk.gov.hmrc"          %% "play-allowlist-filter"          % "1.0.0-play-28",
    "uk.gov.hmrc"          %% "play-nunjucks"                  % "0.35.0-play-28",
    "uk.gov.hmrc"          %% "play-nunjucks-viewmodel"        % "0.15.0-play-28",
    "org.webjars.npm"      % "govuk-frontend"                  % "3.14.0",
    "uk.gov.hmrc.webjars"  % "hmrc-frontend"                   % "3.1.1",
    "com.lucidchart"       %% "xtract"                         % "2.2.1"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"             %% "scalatest"              % "3.2.10",
    "org.scalatestplus.play"    %% "scalatestplus-play"     % "5.1.0",
    "org.scalatestplus"         %% "scalatestplus-mockito"  % "1.0.0-M2",
    "org.scalatestplus"         %% "scalacheck-1-15"        % "3.2.9.0",
    "org.pegdown"               % "pegdown"                 % "1.6.0",
    "org.jsoup"                 % "jsoup"                   % "1.14.2",
    "com.typesafe.play"         %% "play-test"              % PlayVersion.current,
    "org.mockito"               % "mockito-core"            % "4.1.0",
    "org.scalacheck"            %% "scalacheck"             % "1.15.0",
    "com.github.tomakehurst"    %  "wiremock-standalone"    % "2.27.2",
    "wolfendale"                %% "scalacheck-gen-regexp"  % "0.1.2",
    "com.vladsch.flexmark"      % "flexmark-all"            % "0.62.2"
    ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
