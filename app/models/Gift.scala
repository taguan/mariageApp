package models

import org.joda.time.DateTime

abstract class Gift
(
  val code : String,
  val creationMoment : DateTime,
  val amount : Int,
  val message : Option[String],
  val contributorInfo : ContributorInfo) {
}

case class ContributorInfo(lastName : Option[String], firstName : Option[String], emailAddress : Option[String]) {

}

