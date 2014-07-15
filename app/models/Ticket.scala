package models

import java.util.UUID

class Ticket
(
  val lotteryParticipation : LotteryParticipation,
  val prizeDefinition : PrizeDefinition,
  val revealed : Boolean = false,
  val code : String = UUID.randomUUID()){
}
