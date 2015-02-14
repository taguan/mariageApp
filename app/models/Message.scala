package models

case class Message
(
  id : Long,
  name : String,
  message : String
  )  {

  override def toString = {
    s"Message : name $name, message $message"
  }

}
