package models

class WinningPrizeDefinition
(
  id : Long,
  val quantity : Int,
  val remainingQuantity : Int,
  name : String, imagePath : String,
  pdfPath : String
  ) extends PrizeDefinition(id, name, imagePath, pdfPath){

}
