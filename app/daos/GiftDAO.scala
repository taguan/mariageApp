package daos

import java.sql.Connection

import com.google.inject.{Inject, Singleton}
import models.{Gift, UnboundGift}
import anorm._
import JodaAnormHelper._

trait GiftDAO[A <: Gift] {
  def insert(gift : A)(implicit connection : Connection) : A
}

trait GenericGiftDAOImpl[A <: Gift] extends GiftDAO[A] {

  val contributorInfoDAO : ContributorInfoDAO

  override def insert(gift : A)(implicit connection : Connection) : A = {
    val insertedGift = insertGift(gift)
    contributorInfoDAO.insert(gift.contributorInfo, gift.code)
    insertedGift
  }

  def insertGift(gift : A)(implicit connection : Connection) : A
}

trait UnboundGiftDAO extends GiftDAO[UnboundGift]

@Singleton
class UnboundGiftDAOImpl @Inject() (override val contributorInfoDAO : ContributorInfoDAO) extends UnboundGiftDAO with GenericGiftDAOImpl[UnboundGift]{

  override def insertGift(unboundGift: UnboundGift)(implicit connection: Connection): UnboundGift = {
    SQL"""
     insert into Gifts(code, amount, message, creationMoment, isLottery)
     VALUES(${unboundGift.code}, ${unboundGift.amount}, ${unboundGift.message}, ${unboundGift.creationMoment}, ${false})""".
      executeInsert()

    unboundGift
  }
}
