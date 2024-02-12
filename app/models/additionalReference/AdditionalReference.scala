package models.additionalReference

import play.api.libs.json.{Format, Json}

case class AdditionalReference(typeValue: String, referenceNumber: Option[String])

object AdditionalReference {
  implicit val format: Format[AdditionalReference] = Json.format[AdditionalReference]
}
