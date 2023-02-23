package forms

import forms.mappings.Mappings
import models.AddressLine._
import models.DynamicAddress
import play.api.data.Form
import play.api.data.Forms.{mapping, optional}
import play.api.i18n.Messages

import javax.inject.Inject

class $formProvider$ @Inject() extends Mappings {

  def apply(prefix: String, isPostalCodeRequired: Boolean, args: Any*)(implicit messages: Messages): Form[DynamicAddress] =
    Form(
      mapping(
        NumberAndStreet.field -> {
          trimmedText(s"\$prefix.error.required", NumberAndStreet.arg +: args)
            .verifying(
              StopOnFirstFail[String](
                maxLength(NumberAndStreet.length, s"\$prefix.error.length", Seq(NumberAndStreet.arg) ++ args ++ Seq(NumberAndStreet.length)),
                regexp(NumberAndStreet.regex, s"\$prefix.error.invalid", NumberAndStreet.arg +: args)
              )
            )
        },
        City.field -> {
          trimmedText(s"\$prefix.error.required", City.arg +: args)
            .verifying(
              StopOnFirstFail[String](
                maxLength(City.length, s"\$prefix.error.length", Seq(City.arg) ++ args ++ Seq(City.length)),
                regexp(City.regex, s"\$prefix.error.invalid", City.arg +: args)
              )
            )
        },
        PostalCode.field -> {
          val constraint = StopOnFirstFail[String](
            maxLength(PostalCode.length, s"\$prefix.error.length", Seq(PostalCode.arg) ++ args ++ Seq(PostalCode.length)),
            regexp(PostalCode.regex, s"\$prefix.error.postalCode.invalid", args)
          )
          if (isPostalCodeRequired) {
            trimmedText(s"\$prefix.error.required", PostalCode.arg +: args)
              .verifying(constraint)
              .transform[Option[String]](Some(_), _.getOrElse(""))
          } else {
            optional(
              trimmedText()
                .verifying(constraint)
            )
          }
        }
      )(DynamicAddress.apply)(DynamicAddress.unapply)
    )
}

object $formProvider$ {

  def apply(prefix: String, isPostalCodeRequired: Boolean, args: Any*)(implicit messages: Messages): Form[DynamicAddress] =
    new $formProvider$()(prefix, isPostalCodeRequired, args: _*)
}
