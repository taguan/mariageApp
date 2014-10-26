package services

import java.sql.Connection

import com.google.inject.{Inject, Singleton}
import daos.{PrizeDAO, TicketDAO, LotteryParticipationDAO}
import models.{Ticket, WinningPrizeDefinition, LostDefinition, LotteryParticipation}

trait LotteryService {
  def attributeTickets(participation : LotteryParticipation) (implicit connection : Connection)
}

@Singleton
class LotteryServiceImpl @Inject()(val lotteryParticipationDAO : LotteryParticipationDAO, val ticketDAO : TicketDAO, val prizeDAO : PrizeDAO) extends LotteryService{
  override def attributeTickets(participation: LotteryParticipation) (implicit connection : Connection): Unit = {
    this.synchronized{
      val lostDefinition = prizeDAO.getLostDefinition()
      val prizes = prizeDAO.getWinningDefinitions().flatMap(p => for (_ <- 0 until p.remainingQuantity) yield p)
      val wonPrizes : List[WinningPrizeDefinition] = attributeTickets(participation.nbrTickets, Nil)(participation, lostDefinition, prizes, connection)
      attributePacks(participation.nbrPacks, wonPrizes)(participation, lostDefinition, prizes, connection)

      lotteryParticipationDAO.confirmParticipation(participation)
    }
  }


  def attributePacks(nbrOfPacksToAttribute: Int, alreadyWonPrices: List[WinningPrizeDefinition])
                   (implicit participation: LotteryParticipation, lostDefinition : LostDefinition, prizes : List[WinningPrizeDefinition], connection:Connection) : List[WinningPrizeDefinition]= {
    attributeTickets(4, attributeWinningTicket(alreadyWonPrices) :: alreadyWonPrices)
  }

  def attributeTickets(nbrOfTicketsToAttribute: Int, alreadyWonPrices: List[WinningPrizeDefinition])
                      (implicit participation: LotteryParticipation, lostDefinition : LostDefinition, prizes : List[WinningPrizeDefinition], connection:Connection) : List[WinningPrizeDefinition]={
    attributeTickets(nbrOfTicketsToAttribute - 1, attributeTicket(alreadyWonPrices))
  }

  def attributeTicket(alreadyWonPrices: List[WinningPrizeDefinition])
                     (implicit participation: LotteryParticipation, lostDefinition : LostDefinition, prizes : List[WinningPrizeDefinition], connection:Connection) : List[WinningPrizeDefinition]={
    if(Math.random() * 100 < lostDefinition.probability){
      ticketDAO.create(new Ticket(participation, None), lostDefinition)
      alreadyWonPrices
    } else{
      attributeWinningTicket(alreadyWonPrices) :: alreadyWonPrices
    }
  }

  def attributeWinningTicket(alreadyWonPrices: List[WinningPrizeDefinition])
                            (implicit participation: LotteryParticipation, prizes : List[WinningPrizeDefinition], connection:Connection) : WinningPrizeDefinition = {
    val winningPrize: WinningPrizeDefinition = getNotAlreadyWonWinningPrize(alreadyWonPrices)
    ticketDAO.create(new Ticket(participation, Some(winningPrize.name)), winningPrize)
    prizeDAO.decrementRemainingPrizes(winningPrize)
    winningPrize
  }

  def getNotAlreadyWonWinningPrize(alreadyWonPrices: List[WinningPrizeDefinition])
                                  (implicit prizes : List[WinningPrizeDefinition]) : WinningPrizeDefinition = {
    val winningPrize: WinningPrizeDefinition = prizes((Math.random() * prizes.length).floor.toInt)
    if(alreadyWonPrices.contains(winningPrize)) getNotAlreadyWonWinningPrize(alreadyWonPrices)
    winningPrize
  }

}
