package daos

import java.sql.Connection
import anorm._
import JodaAnormHelper._
import com.google.inject.Singleton
import models.{Ticket, LotteryParticipation}

trait TicketDAO {
  def tickets(participation : LotteryParticipation)(implicit connection : Connection) : List[Ticket]
}

@Singleton
class TicketDAOImpl extends TicketDAO {
  
  override def tickets(participation: LotteryParticipation)(implicit connection: Connection): List[Ticket] = {
    SQL"""
          select revealed, name, isWinning from Tickets inner join PrizeDefinitions on prizeDefinitionId = id
          where giftCode = ${participation.code}
    """().map{ row =>
      new Ticket(participation, if(row[Boolean]("isWinning")) Some(row[String]("name")) else None, row[Boolean]("revelead"))
    }.toList
  }
}


