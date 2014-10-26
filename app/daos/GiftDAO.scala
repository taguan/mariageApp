package daos

import java.sql.Connection

import com.google.inject.{Inject, Singleton}
import models.{ContributorInfo, LotteryParticipation, Gift, UnboundGift}
import anorm._
import JodaAnormHelper._
import org.joda.time.DateTime

trait GiftDAO[A <: Gift] {
  def insert(gift : A)(implicit connection : Connection) : A
  def getAll()(implicit connection : Connection) : List[Gift]
}

trait GenericGiftDAOImpl[A <: Gift] extends GiftDAO[A] {

  val contributorInfoDAO : ContributorInfoDAO

  override def insert(gift : A)(implicit connection : Connection) : A = {
    val insertedGift = insertGift(gift)
    contributorInfoDAO.insert(gift.contributorInfo, gift.code)
    insertedGift
  }

  def insertGift(gift : A)(implicit connection : Connection) : A

  override def getAll()(implicit connection : Connection) : List[Gift] =  {
    SQL"""
      select * from Gifts g inner join ContributorInfo c on c.giftCode = g.code order by g.creationMoment
    """().map{ row =>
      val contributorInfo = ContributorInfo(row[Option[String]]("lastName"), row[Option[String]]("firstName"), row[Option[String]]("emailAddress"))
      if(row[Boolean]("isLottery")) new LotteryParticipation(row[Int]("amount"), row[String]("code"), row[DateTime]("creationMoment"),
        row[Option[String]]("message"), contributorInfo, row[Boolean]("confirmed"), row[Int]("nbrTickets"), row[Int]("nbrPacks"))
      else new UnboundGift(row[String]("code"), row[DateTime]("creationMoment"), row[Int]("amount"), row[Option[String]]("message"), contributorInfo)
    }.toList
  }

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

trait LotteryParticipationDAO extends GiftDAO[LotteryParticipation] {
  def unconfirmedParticipations()(implicit connection: Connection) : List[LotteryParticipation]
}

@Singleton
class LotteryParticipationDAOImpl @Inject() (override val contributorInfoDAO : ContributorInfoDAO) extends LotteryParticipationDAO with GenericGiftDAOImpl[LotteryParticipation] {
  
  override def insertGift(lotteryParticipation: LotteryParticipation)(implicit connection: Connection): LotteryParticipation = {
    SQL"""
          insert into Gifts(code, amount, message, creationMoment, isLottery, nbrTickets, nbrPacks)
          VALUES(${lotteryParticipation.code}, ${lotteryParticipation.amount}, ${lotteryParticipation.message},
          ${lotteryParticipation.creationMoment}, ${true}, ${lotteryParticipation.nbrTickets}, ${lotteryParticipation.nbrPacks})
    """.executeInsert()
    
    lotteryParticipation
  }

  override def unconfirmedParticipations()(implicit connection: Connection) : List[LotteryParticipation] = {
    SQL"""
      select * from Gifts g inner join ContributorInfo c on c.giftCode = g.code where g.isLottery = ${true}
      and g.confirmed = ${false} order by g.creationMoment
    """().map{ row =>
      val contributorInfo = ContributorInfo(row[Option[String]]("lastName"), row[Option[String]]("firstName"), row[Option[String]]("emailAddress"))
      new LotteryParticipation(row[Int]("amount"), row[String]("code"), row[DateTime]("creationMoment"),
        row[Option[String]]("message"), contributorInfo, row[Boolean]("confirmed"), row[Int]("nbrTickets"), row[Int]("nbrPacks"))
    }.toList
  }
}
