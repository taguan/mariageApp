package daos

import java.sql.Connection

import anorm._
import com.google.inject.Singleton
import models.{LotteryParticipation, PrizeDefinition, Ticket}

trait TicketDAO {
  def getPrizeId(ticketCode: String)(implicit connection: Connection): Long
  def revealTickets(participationCode: String)(implicit connection : Connection) : Unit
  def tickets(participation : LotteryParticipation)(implicit connection : Connection) : List[Ticket]
  def create(ticket : Ticket, prize : PrizeDefinition)(implicit connection : Connection) : Unit
  def getTicket(ticketCode : String)(implicit connection : Connection) : Ticket
}

@Singleton
class TicketDAOImpl extends TicketDAO {
  
  override def tickets(participation: LotteryParticipation)(implicit connection: Connection): List[Ticket] = {
    SQL"""
          select code, revealed, name, isWinning from Tickets inner join PrizeDefinitions on prizeDefinitionId = id
          where giftCode = ${participation.code}
    """().map{ row =>
      new Ticket(participation, if(row[Boolean]("isWinning")) Some(row[String]("name")) else None, row[Boolean]("revealed"), row[String]("code"))
    }.toList
  }

  override def create(ticket: Ticket, prize: PrizeDefinition)(implicit connection: Connection): Unit = {
    SQL"""
          insert into Tickets(code, giftCode, prizeDefinitionId) values (${ticket.code}, ${ticket.lotteryParticipation.code}, ${prize.id})
    """.executeInsert()
  }

  override def revealTickets(participationCode: String)(implicit connection: Connection): Unit = {
    SQL"""
          update Tickets set revealed = ${true} where giftCode = $participationCode
    """.executeUpdate()
  }

  override def getPrizeId(ticketCode: String)(implicit connection: Connection): Long = {
    SQL"""
          select prizeDefinitionId from Tickets where code = $ticketCode
    """().map{ row =>
      row[Long]("prizeDefinitionId")
    }.toList.head
  }

  override def getTicket(ticketCode: String)(implicit connection: Connection): Ticket = {
    SQL"""
          select code, revealed, name, isWinning from Tickets
          where code = $ticketCode
    """().map{ row =>
      new Ticket(null, if(row[Boolean]("isWinning")) Some(row[String]("name")) else None, row[Boolean]("revealed"), row[String]("code"))
    }.toList.head
  }
}


