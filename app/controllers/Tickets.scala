package controllers

import com.google.inject.{Inject, Singleton}
import daos.{LotteryParticipationDAO, TicketDAO}
import models.Ticket
import play.api.db.DB
import play.api.libs.json.{JsValue, JsPath, Json, Writes}
import play.api.Play.current
import play.api.mvc.{Action, Controller}
import services.FileUtil

@Singleton
class Tickets @Inject() (ticketDAO : TicketDAO, participationDAO : LotteryParticipationDAO) extends Controller {


  implicit val ticketWrite : Writes[Ticket] = new Writes[Ticket] {
    def writes(ticket: Ticket) = Json.obj(
      "code" -> ticket.code
    )
  }

  case class TicketsWrapper(tickets : List[Ticket])

  implicit val ticketWrapperWrite : Writes[TicketsWrapper] = new Writes[TicketsWrapper] {
    override def writes(wrapper: TicketsWrapper): JsValue = Json.obj(
      "tickets" -> wrapper.tickets
    )
  }

  def getTicketsForParticipation(participationCode : String) = Action { DB.withConnection { implicit connection =>
      try {
        ticketDAO.revealTickets(participationCode)
        Ok(Json.toJson(TicketsWrapper(ticketDAO.tickets(participationDAO.getParticipation(participationCode)))))
      }
      catch{
        case e : Exception => NotFound
      }
    }
  }

  def sendImage(participationCode : String, ticketCode : String) = Action { DB.withConnection { implicit connection =>
      try {
        val prizeId: Long = ticketDAO.getPrizeId(participationCode, ticketCode)
        Ok.sendFile(FileUtil.imagePath(prizeId), inline = true)
      }
      catch {
        case e: Exception =>
          NotFound
      }
    }
  }

  def sendPdf(participationCode : String, ticketCode : String) = Action { DB.withConnection { implicit connection =>
      try {
        val prizeId: Long = ticketDAO.getPrizeId(participationCode, ticketCode)
        Ok.sendFile(FileUtil.pdfPath(prizeId), inline = false)
      }
      catch {
        case e: Exception =>
          NotFound
      }
    }
  }
}
