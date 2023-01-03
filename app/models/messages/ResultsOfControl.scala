/*
 * Copyright 2023 HM Revenue & Customs
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

package models.messages

import cats.syntax.all._
import com.lucidchart.open.xtract.{__, XmlReader}
import models.{LanguageCodeEnglish, XMLWrites}

sealed trait ResultsOfControl extends Product with Serializable {
  val controlIndicator: ControlIndicator
}

object ResultsOfControl {

  val descriptionLength    = 140
  val correctedValueLength = 27

  implicit val xmlReader: XmlReader[ResultsOfControl] = ResultsOfControlDifferentValues.xmlReader orElse ResultsOfControlOther.xmlReader
}

case class ResultsOfControlOther(description: String) extends ResultsOfControl {
  val controlIndicator: ControlIndicator = ControlIndicator(OtherThingsToReport)
}

object ResultsOfControlSealsBroken extends ResultsOfControlOther("Some seals are broken")
object ResultsOfControlSealsNotReadable extends ResultsOfControlOther("Some seals not readable")
object ResultsOfControlSealsUpdated extends ResultsOfControlOther("Seals have been updated")

object ResultsOfControlOther {

  implicit val writes: XMLWrites[ResultsOfControlOther] =
    XMLWrites(
      resultsOfControl => <RESOFCON534>
        <DesTOC2>{resultsOfControl.description}</DesTOC2>
        <DesTOC2LNG>{LanguageCodeEnglish.code}</DesTOC2LNG>
        <ConInd424>{resultsOfControl.controlIndicator.indicator.value}</ConInd424>
      </RESOFCON534>
    )

  implicit val xmlReader: XmlReader[ResultsOfControlOther] = (__ \ "DesTOC2").read[String] map apply

}

case class ResultsOfControlDifferentValues(pointerToAttribute: PointerToAttribute, correctedValue: String) extends ResultsOfControl {
  val controlIndicator: ControlIndicator = ControlIndicator(DifferentValuesFound)
}

object ResultsOfControlDifferentValues {

  implicit val writes: XMLWrites[ResultsOfControlDifferentValues] =
    XMLWrites(
      resultsOfControl => <RESOFCON534>
      <ConInd424>{resultsOfControl.controlIndicator.indicator.value}</ConInd424>
      <PoiToTheAttTOC5>{resultsOfControl.pointerToAttribute.pointer.value}</PoiToTheAttTOC5>
      <CorValTOC4>{resultsOfControl.correctedValue}</CorValTOC4>
    </RESOFCON534>
    )

  implicit val xmlReader: XmlReader[ResultsOfControlDifferentValues] =
    (__.read[PointerToAttribute], (__ \ "CorValTOC4").read[String]).mapN(apply)

}
