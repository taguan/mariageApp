package models

class LostDefinition
(
  id : Long,
  val probability : Int,
  name : String, imagePath : String,
  pdfPath : String
  ) extends PrizeDefinition(id, name, imagePath, pdfPath){

}
