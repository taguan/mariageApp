package controllers

import com.google.inject.{Inject, Singleton}
import daos.ConfirmationDAO
import models.Confirmation
import play.api.Logger
import play.api.Play.current
import play.api.db.DB
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{Action, Controller}

@Singleton
class Confirmations @Inject() (confirmationDAO : ConfirmationDAO) extends Controller{

  /*
  Example of expected json :
  {
    "confirmation" : {
      "lastName" : "Doe",
      "firstName" : "John" (or null),
      "isComing" : boolean,
      "nbrComing" : int,
      "comment" : String (or null)
    }
  }
   */

  case class ConfirmationWrapper(confirmation : Confirmation)

  implicit val confirmation : Reads[Confirmation] = (
    (JsPath \ "lastName").read[String] and
      (JsPath \ "firstName").readNullable[String](maxLength(255)) and
      (JsPath \ "isComing").read[Boolean] and
      (JsPath \ "nbrComing").read[Int](min(0)) and
      (JsPath \ "comment").readNullable[String]
    )((lastName, firstName, isComing, nbrComing, comment) => Confirmation(0, lastName, firstName, isComing, comment, nbrComing))

  implicit val confirmationWrapperRead : Reads[ConfirmationWrapper] =
    (JsPath \ "confirmation").read[Confirmation].map(ConfirmationWrapper)

  implicit val confirmationWrite : Writes[Confirmation] =(
    (JsPath \ "id").write[Long] and
      (JsPath \ "lastName").write[String] and
      (JsPath \ "firstName").writeNullable[String] and
      (JsPath \ "isComing").write[Boolean] and
      (JsPath \ "nbrComing").write[Int] and
      (JsPath \ "comment").writeNullable[String]
    )((confirmation : Confirmation) => (confirmation.id, confirmation.lastName, confirmation.firstName, confirmation.isComing,
    confirmation.nbrComing, confirmation.comment))

  implicit val confirmationWrapperWrite : Writes[ConfirmationWrapper] =
    (JsPath \ "confirmation").write[Confirmation].contramap(unlift(ConfirmationWrapper.unapply))

  def create = Action(parse.json){ request =>
    val confirmationResult = request.body.validate[ConfirmationWrapper]
    confirmationResult.fold(
      errors => Ok(Json.parse("""{"errors" : ["Une erreur est survenue"]}""")),
      confirmationWrapper => DB.withTransaction { implicit connection =>
        val createdConfirmation = confirmationDAO.insert(confirmationWrapper.confirmation)
        Logger.info(s"confirmation registered $confirmation")
        Ok(Json.toJson(ConfirmationWrapper(createdConfirmation)))
      }
    )
  }
}


