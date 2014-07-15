package daos

import java.sql.Connection

import com.google.inject.Singleton
import models.ContributorInfo
import anorm._

trait ContributorInfoDAO {
  def insert(contributorInfo: ContributorInfo, giftCode : String)(implicit connection: Connection): ContributorInfo
}

@Singleton
class ContributorInfoDAOImpl extends ContributorInfoDAO {
  override def insert(contributorInfo: ContributorInfo, giftCode : String)(implicit connection: Connection): ContributorInfo = {
    SQL"""
     insert into ContributorInfo(giftCode, lastName, firstName, emailAddress)
     VALUES($giftCode, ${contributorInfo.lastName}, ${contributorInfo.firstName}, ${contributorInfo.emailAddress})""".
      executeInsert()

    contributorInfo
  }
}

