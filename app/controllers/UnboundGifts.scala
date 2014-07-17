package controllers

import com.google.inject.{Inject, Singleton}
import daos.UnboundGiftDAO
import models.{ContributorInfo, UnboundGift}
import play.api.Logger
import play.api.db.DB
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.mvc.{Action, Controller}
import play.api.Play.current

@Singleton
class UnboundGifts @Inject() (unboundGiftDAO : UnboundGiftDAO) extends Controller{

  val logger = Logger(this.getClass)

  /*
  Example of expected json :
  {
    "unboundGift" : {
      "amount" : 20,
      "message" : "a message" (or null),
      "lastName" : "Doe" (or null),
      "firstName" : "John" (or null),
      "emailAddress" : "john.doe@test.be" (or null)
    }
  }
   */

  case class UnboundGiftWrapper(unboundGift : UnboundGift)

  implicit val unboundGiftRead : Reads[UnboundGift] =(
    (JsPath \ "amount").read[Int](min(0)) and
      (JsPath \ "message").readNullable[String] and
      (JsPath \ "lastName").readNullable[String](maxLength(255)) and
      (JsPath \ "firstName").readNullable[String](maxLength(255)) and
      (JsPath \ "emailAddress").readNullable[String](email)
    )((amount, message, lastName, firstName, emailAddress) => new UnboundGift(amount, message, ContributorInfo(lastName, firstName, emailAddress)))

  implicit val unboundGiftWrapperRead : Reads[UnboundGiftWrapper] =
    (JsPath \ "unboundGift").read[UnboundGift].map(UnboundGiftWrapper)



  def create = Action(parse.json){ request =>
    val unboundGiftResult = request.body.validate[UnboundGiftWrapper]
    unboundGiftResult.fold(
      errors => Ok(Json.parse("""{"errors" : ["Une erreur est survenue"]}""")),
      unboundGiftWrapper => DB.withTransaction { implicit connection =>
        unboundGiftDAO.insert(unboundGiftWrapper.unboundGift)
        Ok
      }
    )
  }
}
