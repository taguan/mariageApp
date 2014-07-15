package models

import java.util.UUID

import org.joda.time.DateTime

abstract class Gift
(
  val code : String,
  val creationMoment : DateTime,
  val amount : Int, val message : Option[String],
  val contributorInfo : ContributorInfo) {
}

class ContributorInfo(val lastName : Option[String], val firstName : Option[String], val emailAddress : Option[String]) {

}

