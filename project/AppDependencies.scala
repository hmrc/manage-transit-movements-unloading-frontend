import sbt._

object AppDependencies {

  private val mongoVersion = "2.1.0"
  private val bootstrapVersion = "9.0.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-play-30"                      % mongoVersion,
    "uk.gov.hmrc"          %% "play-conditional-form-mapping-play-30"   % "2.0.0",
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-30"              % bootstrapVersion,
    "org.apache.commons"    % "commons-text"                            % "1.11.0",
    "org.typelevel"        %% "cats-core"                               % "2.10.0",
    "uk.gov.hmrc"          %% "play-frontend-hmrc-play-30"              % "10.3.0",
    "uk.gov.hmrc"          %% "crypto-json-play-30"                     % "8.0.0",
    "javax.xml.bind"        % "jaxb-api"                                % "2.3.1"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-test-play-30"  % mongoVersion,
    "org.scalatest"              %% "scalatest"                % "3.2.18",
    "uk.gov.hmrc"                %% "bootstrap-test-play-30"   % bootstrapVersion,
    "org.mockito"                 % "mockito-core"             % "5.11.0",
    "org.scalatestplus"          %% "mockito-4-11"             % "3.2.18.0",
    "org.scalacheck"             %% "scalacheck"               % "1.18.0",
    "org.scalatestplus"          %% "scalacheck-1-17"          % "3.2.18.0",
    "io.github.wolfendale"       %% "scalacheck-gen-regexp"    % "1.1.0",
    "org.jsoup"                   % "jsoup"                    % "1.17.2",
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
