package utils.transformers

import generated.AdditionalReferenceType03
import models.additionalReference.AdditionalReference
import models.{Index, UserAnswers}
import pages.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import pages.sections.additionalReference.AdditionalReferenceSection
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalReferenceTransformer @Inject() ()(implicit ec: ExecutionContext) extends PageTransformer {

  def transform(additionalReferences: Seq[AdditionalReferenceType03]): UserAnswers => Future[UserAnswers] = userAnswers => {
    additionalReferences.zipWithIndex.foldLeft(Future.successful(userAnswers))(
      {
        case (accumulator, (reference,i)) =>
          accumulator.flatMap { userAnswers =>
            val pipeline: UserAnswers => Future[UserAnswers] =
              set(AdditionalReferenceSection, Json.obj()) andThen
                set(AdditionalReferenceTypePage(Index(i)), reference.typeValue) andThen
                set(AdditionalReferenceNumberPage(Index(i)), reference.referenceNumber)

            pipeline(userAnswers)
          }

      }
    )

  }
}
