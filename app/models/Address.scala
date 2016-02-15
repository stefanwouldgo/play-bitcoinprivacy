package models

import play.api.libs.json._
import play.api.libs.ws._

case class Address(address:String,balance:Long) 
case class AddressesSummary(count: Int, balance: Long)

object Address  
{
  implicit val addressReads = Json.reads[Address]

  def getRichList(blockHeight: Int, table: String) = 
    getFromApi("richlist", table, "1", "1000").map(_.json.as[List[Address]])

  def getWallet(hex:String, height: Int, from: Int, to: Int) =
    getFromApi("wallet", hex, from.toString, to.toString) map {response => (response.json).as[List[Address]]}

}

object AddressesSummary{

  implicit val adsumReads = Json.reads[AddressesSummary]

  def get(hex: String, height: Int) = getFromApi("wallet", hex, "summary") map (_.json.as[AddressesSummary])

  def getRichest(height: Int, table: String) = getFromApi("richlist",table,"summary") map (_.json.as[AddressesSummary])

}
