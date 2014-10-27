package daos

import java.sql.Connection
import anorm._
import JodaAnormHelper._
import com.google.inject.Singleton
import models.{PrizeDefinition, Ticket, LotteryParticipation}

trait TicketDAO {
  def tickets(participation : LotteryParticipation)(implicit connection : Connection) : List[Ticket]
  def create(ticket : Ticket, prize : PrizeDefinition)(implicit connection : Connection) : Unit
}

@Singleton
class TicketDAOImpl extends TicketDAO {
  
  override def tickets(participation: LotteryParticipation)(implicit connection: Connection): List[Ticket] = {
    SQL"""
          select revealed, name, isWinning from Tickets inner join PrizeDefinitions on prizeDefinitionId = id
          where giftCode = ${participation.code}
    """().map{ row =>
      new Ticket(participation, if(row[Boolean]("isWinning")) Some(row[String]("name")) else None, row[Boolean]("revealed"))
    }.toList
  }

  override def create(ticket: Ticket, prize: PrizeDefinition)(implicit connection: Connection): Unit = {
    SQL"""
          insert into Tickets(code, giftCode, prizeDefinitionId) values (${ticket.code}, ${ticket.lotteryParticipation.code}, ${prize.id})
    """.executeInsert()
  }
}


