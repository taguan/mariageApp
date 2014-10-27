package models

class LostDefinition
(
  id : Long,
  val probability : Int,
  name : String
  ) extends PrizeDefinition(id, name){

}
