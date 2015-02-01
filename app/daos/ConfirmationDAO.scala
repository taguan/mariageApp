package daos

import java.sql.Connection

import com.google.inject.Singleton
import models.Confirmation
import anorm._

trait ConfirmationDAO {
  def insert(confirmation: Confirmation)(implicit connection: Connection): Confirmation
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
}
