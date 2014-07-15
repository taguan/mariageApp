package models

import java.util.UUID

import org.joda.time.DateTime

class UnboundGift
(
  code : String,
  creationMoment : DateTime,
  amount : Int,
  message : Option[String],
  contributorInfo : ContributorInfo
  ) extends Gift(code, creationMoment, amount, message, contributorInfo){

  def this(amount : Int, message : Option[String], contributorInfo : ContributorInfo){
    this(UUID.randomUUID().toString, DateTime.now(), amount, message, contributorInfo)
  }
}
