package daos

import java.sql.Connection

import com.google.inject.Singleton
import models.Confirmation
import anorm._

trait ConfirmationDAO {
  def insert(confirmation: Confirmation)(implicit connection: Connection): Confirmation
  def getAll()(implicit connection: Connection): List[Confirmation]
}


@Singleton
class ConfirmationDAOImpl extends ConfirmationDAO {
  override def insert(confirmation: Confirmation)(implicit connection: Connection): Confirmation = {
    SQL"""
     insert into Confirmations(lastName, firstName, isComing, nbrComing, comment)
     VALUES(${confirmation.lastName}, ${confirmation.firstName}, ${confirmation.isComing}, ${confirmation.nbrComing}, ${confirmation.comment})""".
      executeInsert()

    confirmation
  }

  override def getAll()(implicit connection: Connection): List[Confirmation] = {
    SQL"""
      select * from Confirmations
    """().map{ row =>
      Confirmation(row[Long]("id"), row[String]("lastName"), row[Option[String]]("firstName"), row[Boolean]("isComing"), row[Option[String]]("comment"), row[Int]("nbrComing"))
    }.toList
  }
}
