package controllers

import com.google.inject.{Inject, Singleton}
import daos.{TicketDAO, LotteryParticipationDAO}
import models._
import play.api.db.DB
import play.api.mvc.{Action, Controller}
import play.api.Play.current
import services.LotteryService

@Singleton
class AdminController @Inject() (lotteryParticipationDAO : LotteryParticipationDAO, ticketDAO : TicketDAO, lotteryService : LotteryService) extends Controller{
  def viewGifts() = Action{
    val giftsAndTickets : List[Either[LotteryParticipationWithTickets,UnboundGift]]  = DB.withConnection {
      implicit connection => lotteryParticipationDAO.getAll().map {
        case g: UnboundGift => Right(g)
        case l: LotteryParticipation => Left(LotteryParticipationWithTickets(l, ticketDAO.tickets(l)))
      }
    }
    Ok(views.html.adminParticipations(giftsAndTickets.filter(_.isLeft).map(_.left.get), giftsAndTickets.filter(_.isRight).map(_.right.get)))
  }

  def viewUnconfirmedParticipations() = Action{
    DB.withConnection { implicit connection =>
      Ok(views.html.adminUnconfirmed(lotteryParticipationDAO.unconfirmedParticipations()))
    }
  }

  def confirmParticipation(code : String) = Action{
    DB.withTransaction{ implicit connection =>
      lotteryService.attributeTickets(lotteryParticipationDAO.getParticipation(code))
    }
    Redirect(routes.AdminController.viewUnconfirmedParticipations())
  }
}
