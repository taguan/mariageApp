package controllers

import com.google.inject.{Inject, Singleton}
import daos.{TicketDAO, UnboundGiftDAO, LotteryParticipationDAO}
import models._
import play.api.db.DB
import play.api.mvc.{Action, Controller}
import play.api.Play.current

@Singleton
class AdminController @Inject() (lotteryParticipationDAO : LotteryParticipationDAO, ticketDAO : TicketDAO) extends Controller{
  def viewGifts() = Action{
    val giftsAndTickets : List[Either[LotteryParticipationWithTickets,UnboundGift]]  = DB.withConnection {
      implicit connection => lotteryParticipationDAO.getAll().map {
        case g: UnboundGift => Right(g)
        case l: LotteryParticipation => Left(LotteryParticipationWithTickets(l, ticketDAO.tickets(l)))
      }
    }
    Ok(views.html.adminParticipations(giftsAndTickets.filter(_.isLeft).map(_.left.get), giftsAndTickets.filter(_.isRight).map(_.right.get)))
  }
}
