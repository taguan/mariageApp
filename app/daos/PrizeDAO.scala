package daos

import java.sql.Connection
import anorm._
import JodaAnormHelper._
import com.google.inject.Singleton
import models.{WinningPrizeDefinition, LostDefinition}

trait PrizeDAO {
  def getLostDefinition()(implicit connection:Connection) : LostDefinition
  def getWinningDefinitions()(implicit connection:Connection) : List[WinningPrizeDefinition]
  def decrementRemainingPrizes(prizeDefinition : WinningPrizeDefinition)(implicit  connection:Connection) : Unit
}

@Singleton
class PrizeDAOImpl extends PrizeDAO {

  override def getLostDefinition()(implicit connection: Connection): LostDefinition = {
    SQL"""
          select * from PrizeDefinitions where isWinning = ${false}
    """().map(row => {
      new LostDefinition(row[Long]("id"), row[Int]("probability"), row[String]("name"), row[String]("imagePath"), row[String]("pdfPath"))
    }).toList.head
  }

  override def getWinningDefinitions()(implicit connection: Connection): List[WinningPrizeDefinition] = {
    SQL"""
          select * from PrizeDefinitions where isWinning = ${true}
    """().map(row => {
      new WinningPrizeDefinition(row[Long]("id"), row[Int]("quantity"), row[Int]("remainingQuantity"), row[String]("name"), row[String]("imagePath"), row[String]("pdfPath"))
    }).toList
  }

  override def decrementRemainingPrizes(prizeDefinition: WinningPrizeDefinition)(implicit connection: Connection): Unit = {
    SQL"""
          update PrizeDefinitions set remainingQuantity = remainingQuantity - 1 where id = ${prizeDefinition.id}
    """.executeUpdate()
  }
}
