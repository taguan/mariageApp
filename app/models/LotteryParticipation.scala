package models

import java.util.UUID

import org.joda.time.DateTime

class LotteryParticipation
(
  amount : Int,
  code : String,
  creationMoment : DateTime,
  message : Option[String],
  contributorInfo : ContributorInfo,
  val confirmed : Boolean = false,
  val nbrTickets : Int = 0,
  val nbrPacks : Int = 0
  ) extends Gift(code, creationMoment, amount, message, contributorInfo){

  def this(amount : Int, message : Option[String], contributorInfo : ContributorInfo, nbrTickets : Int, nbrPacks : Int){
    this(amount, UUID.randomUUID().toString, DateTime.now(), message, contributorInfo, false, nbrTickets, nbrPacks)
  }
}
