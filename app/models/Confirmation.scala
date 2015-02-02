package models

case class Confirmation
(
  id : Long,
  lastName : String,
  firstName : Option[String],
  isComing : Boolean,
  comment : Option[String],
  nbrComing : Int = 0
  )  {

  override def toString = {
    s"Confirmation : lastName $lastName, firstName $firstName, isComing $isComing, comment $comment"
  }

}
