package controllers

import com.google.inject.{Inject, Singleton}
import daos.MessageDAO
import models.Message
import play.api.Logger
import play.api.Play.current
import play.api.db.DB
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{Action, Controller}

@Singleton
class Messages @Inject() (messageDAO : MessageDAO) extends Controller{

  /*
  Example of expected json :
  {
    "message" : {
      "id" : 1634
      "name" : "a name"
      "message" : "a message"
    }
  }
   */

  case class MessageWrapper(message : Message)

  case class MessagesWrapper(messages : List[Message])

  implicit val message : Reads[Message] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "message").read[String]
    )((name, message) => Message(0, name, message))

  implicit val messageWrapperRead : Reads[MessageWrapper] =
    (JsPath \ "message").read[Message].map(MessageWrapper)

  implicit val messageWrite : Writes[Message] = (
    (JsPath \ "id").write[Long] and
      (JsPath \ "name").write[String] and
      (JsPath \ "message").write[String]
    )((message : Message) => (message.id, message.name, message.message))

  implicit val messageWrapperWrite : Writes[MessageWrapper] =
    (JsPath \ "message").write[Message].contramap(unlift(MessageWrapper.unapply))

  implicit val messagesWrapperWrite : Writes[MessagesWrapper] =
    (JsPath \ "messages").write[List[Message]].contramap(unlift(MessagesWrapper.unapply))

  def create = Action(parse.json) { request =>
    val messageResult = request.body.validate[MessageWrapper]
    messageResult.fold(
      errors => Ok(Json.parse( """{"errors" : ["Une erreur est survenue"]}""")),
      messageWrapper => DB.withTransaction { implicit connection =>
        val createdMessage = messageDAO.insert(messageWrapper.message)
        Logger.info(s"message registered $createdMessage")
        Ok(Json.toJson(MessageWrapper(createdMessage)))
      }
    )
  }

  def getAll = Action { _ =>
    DB.withTransaction { implicit connection =>
      Ok(Json.toJson(MessagesWrapper(messageDAO.getAll())))
    }
  }

}
