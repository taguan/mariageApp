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
      "id" -> ticket.code
    )
  }

  case class TicketsWrapper(tickets : List[Ticket])

  implicit val ticketsWrapperWrite : Writes[TicketsWrapper] = new Writes[TicketsWrapper] {
    override def writes(wrapper: TicketsWrapper): JsValue = Json.obj(
      "tickets" -> wrapper.tickets
    )
  }

  case class TicketWrapper(ticket : Ticket)

  implicit val ticketWrapperWrite : Writes[TicketWrapper] = new Writes[TicketWrapper] {
    override def writes(wrapper: TicketWrapper): JsValue = Json.obj(
      "ticket" -> wrapper.ticket
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

  def getTicket(ticketCode : String) = Action { DB.withConnection { implicit connection =>
      try {
        Ok(Json.toJson(TicketWrapper(ticketDAO.getTicket(ticketCode))))
      }
      catch {
        case e: Exception => NotFound
      }
    }
  }

  def sendImage(ticketCode : String) = Action { DB.withConnection { implicit connection =>
      try {
        val prizeId: Long = ticketDAO.getPrizeId(ticketCode)
        Ok.sendFile(FileUtil.imagePath(prizeId), inline = true)
      }
      catch {
        case e: Exception =>
          NotFound
      }
    }
  }

  def sendPdf(ticketCode : String) = Action { DB.withConnection { implicit connection =>
      try {
        val prizeId: Long = ticketDAO.getPrizeId(ticketCode)
        Ok.sendFile(FileUtil.pdfPath(prizeId), inline = false)
      }
      catch {
        case e: Exception =>
          NotFound
      }
    }
  }
}
