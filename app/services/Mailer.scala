package services

import com.typesafe.plugin._
import play.api.Play.current
import play.api.Play
import play.api.libs.concurrent.Execution.Implicits._

object Mailer {
  
  val SENDER = Play.current.configuration.getString("email.sender").getOrElse("Marie & Benoit")
  
  def sendMail(destinations : List[String], subject : String, template : play.twirl.api.Html) = scala.concurrent.Future{
    val mail = use[MailerPlugin].email
    mail.setSubject(subject)
    
    mail.setRecipient(destinations:_*)
    mail.setFrom(SENDER)

    mail.sendHtml(template.toString())
  }

}