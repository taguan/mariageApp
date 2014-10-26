package models

class WinningPrizeDefinition
(
  id : Long,
  val quantity : Int,
  val remainingQuantity : Int,
  name : String, imagePath : String,
  pdfPath : String
  ) extends PrizeDefinition(id, name, imagePath, pdfPath){


  def canEqual(other: Any): Boolean = other.isInstanceOf[WinningPrizeDefinition]

  override def equals(other: Any): Boolean = other match {
    case that: WinningPrizeDefinition =>
      (that canEqual this) &&
        id == that.id
    case _ => false
  }

  override def hashCode(): Int = {
    id.toInt
  }
}
