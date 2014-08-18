package controllers

import com.google.inject.{Inject, Singleton}
import daos.LotteryParticipationDAO
import models.{ContributorInfo, LotteryParticipation}
import play.api.Logger
import play.api.db.DB
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.mvc.{Action, Controller}
import play.api.Play.current

@Singleton
class LotteryParticipations  @Inject() (lotteryParticipationDAO : LotteryParticipationDAO) extends Controller {
  /*
 Example of expected json :
 {
   "lotteryParticipation" : {
     "nbrTickets" : 4,
     "nbrPacks" : 1,
     "amount" : 20,
     "message" : "a message" (or null),
     "lastName" : "Doe" (or null),
     "firstName" : "John" (or null),
     "emailAddress" : "john.doe@test.be" (or null)
   }
 }
  */

  case class LotteryParticipationWrapper(lotteryParticipation : LotteryParticipation)
  implicit val lotteryParticipationRead : Reads[LotteryParticipation] =(
    (JsPath \ "nbrTickets").read[Int](min(0)) and
      (JsPath \ "nbrPacks").read[Int](min(0)) and
      (JsPath \ "amount").read[Int](min(0)) and
      (JsPath \ "message").readNullable[String] and
      (JsPath \ "lastName").readNullable[String](maxLength(255)) and
      (JsPath \ "firstName").readNullable[String](maxLength(255)) and
      (JsPath \ "emailAddress").readNullable[String](email)
    )((nbrTickets, nbrPacks, amount, message, lastName, firstName, emailAddress) => new LotteryParticipation(amount,
    message, ContributorInfo(lastName, firstName, emailAddress),nbrTickets, nbrPacks))
    .filter(participation => participation.amount == participation.nbrTickets * 10 + participation.nbrPacks * 50)

  implicit val lotteryParticipationWrapperRead : Reads[LotteryParticipationWrapper] =
    (JsPath \ "lotteryParticipation").read[LotteryParticipation].map(LotteryParticipationWrapper)

  implicit val lotteryParticipationWrite : Writes[LotteryParticipation] =(
    (JsPath \ "id").write[String] and
      (JsPath \ "nbrTickets").write[Int] and
      (JsPath \ "nbrPacks").write[Int] and
      (JsPath \ "amount").write[Int] and
      (JsPath \ "message").writeNullable[String] and
      (JsPath \ "lastName").writeNullable[String] and
      (JsPath \ "firstName").writeNullable[String] and
      (JsPath \ "emailAddress").writeNullable[String]
    )((lotteryParticipation : LotteryParticipation) => (lotteryParticipation.code, lotteryParticipation.nbrTickets, lotteryParticipation.nbrPacks,
    lotteryParticipation.amount, lotteryParticipation.message, lotteryParticipation.contributorInfo.lastName,
    lotteryParticipation.contributorInfo.firstName, lotteryParticipation.contributorInfo.emailAddress))

  implicit val lotteryParticipationWrapperWrite : Writes[LotteryParticipationWrapper] =
    (JsPath \ "lotteryParticipation").write[LotteryParticipation].contramap(unlift(LotteryParticipationWrapper.unapply))



  def create = Action(parse.json){ request =>
    val lotteryParticipationResult = request.body.validate[LotteryParticipationWrapper]
    Logger.info(request.body.toString())
    lotteryParticipationResult.fold(
      errors =>
        Ok(Json.parse("""{"errors" : ["Une erreur est survenue"]}""")),
      lotteryParticipationWrapper => DB.withTransaction { implicit connection =>
        val createdParticipation = lotteryParticipationDAO.insert(lotteryParticipationWrapper.lotteryParticipation)
        Logger.info(s"Lottery participation registered $createdParticipation")
        Ok(Json.toJson(LotteryParticipationWrapper(createdParticipation)))
      }
    )
  }
}
