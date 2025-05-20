import play.sbt.PlayImport.caffeine
import sbt.*

object AppDependencies {

  private val mongoVersion = "2.6.0"
  private val bootstrapVersion = "9.12.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-play-30"                      % mongoVersion,
    "uk.gov.hmrc"          %% "play-conditional-form-mapping-play-30"   % "3.3.0",
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-30"              % bootstrapVersion,
    "org.apache.commons"    % "commons-text"                            % "1.13.1",
    "org.typelevel"        %% "cats-core"                               % "2.13.0",
    "uk.gov.hmrc"          %% "play-frontend-hmrc-play-30"              % "12.1.0",
    "uk.gov.hmrc"          %% "crypto-json-play-30"                     % "8.2.0",
    "javax.xml.bind"        % "jaxb-api"                                % "2.3.1",
    caffeine
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-test-play-30"  % mongoVersion,
    "org.scalatest"              %% "scalatest"                % "3.2.19",
    "uk.gov.hmrc"                %% "bootstrap-test-play-30"   % bootstrapVersion,
    "org.mockito"                 % "mockito-core"             % "5.17.0",
    "org.scalatestplus"          %% "mockito-5-12"             % "3.2.19.0",
    "org.scalacheck"             %% "scalacheck"               % "1.18.1",
    "org.scalatestplus"          %% "scalacheck-1-18"          % "3.2.19.0",
    "io.github.wolfendale"       %% "scalacheck-gen-regexp"    % "1.1.0",
    "org.jsoup"                   % "jsoup"                    % "1.20.1",
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
