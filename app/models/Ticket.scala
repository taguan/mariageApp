package models

import java.util.UUID

class Ticket
(
  val lotteryParticipation : LotteryParticipation,
  val prizeName : Option[String],
  val revealed : Boolean = false,
  val code : String = UUID.randomUUID().toString){
}
