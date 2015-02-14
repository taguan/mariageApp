package daos

import java.sql.Connection

import com.google.inject.Singleton
import models.Message
import anorm._


trait MessageDAO {
  def insert(message: Message)(implicit connection: Connection): Message
  def getAll()(implicit connection: Connection): List[Message]
}


@Singleton
class MessageDAOImpl extends MessageDAO {
  override def insert(message: Message)(implicit connection: Connection): Message = {
    SQL"""
     insert into Messages(name, message)
     VALUES(${message.name}, ${message.message})""".
      executeInsert() match {
      case Some(id : Long) => message.copy(id = id)
    }
  }

  override def getAll()(implicit connection: Connection): List[Message] = {
    SQL"""
      select * from Messages
    """().map{ row =>
      Message(row[Long]("id"), row[String]("name"), row[String]("message"))
    }.toList
  }
}
